package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.request.TbkDgOptimusMaterialRequest;
import com.taobao.api.response.TbkDgOptimusMaterialResponse;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.api.ResultCode;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.HJKProperties;
import com.tencent.wxcloudrun.config.properties.TBProperties;
import com.tencent.wxcloudrun.config.properties.WYKProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.*;
import com.tencent.wxcloudrun.enums.OrderStatus;
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.model.UmsUserTb;
import com.tencent.wxcloudrun.service.TBService;
import com.tencent.wxcloudrun.service.UmsUserTbService;
import com.tencent.wxcloudrun.utils.JsonUtil;
import com.tencent.wxcloudrun.utils.RequestHolder;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import com.tencent.wxcloudrun.utils.XLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TBServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements TBService {

  final Logger logger = LoggerFactory.getLogger(TBServiceImpl.class);

  @Resource private TBProperties tbProperties;

  @Resource private HJKProperties hjkProperties;

  @Resource private WYKProperties wykProperties;

  @Resource private UmsUserTbService umsUserTbService;

  @Value("${spring.profiles.active}")
  private String env;

  private DefaultTaobaoClient getTBClient() {
    return new DefaultTaobaoClient(
        tbProperties.getUrl(), tbProperties.getAppKey(), tbProperties.getAppSecret());
  }

  @Override
  public void syncOrder(String startTime, String endTime) {
    if (ObjectUtil.isEmpty(startTime)) {
      return;
    }
    if (ObjectUtil.isEmpty(endTime)) {
      endTime = DateUtil.now();
    }
    String oneHourEndTime =
        DateUtil.formatDateTime(DateUtil.offsetHour(DateUtil.parseDateTime(startTime), 1));
    // 如果一个小时后时间在结束时间之前，继续查询
    if (DateUtil.parseDateTime(oneHourEndTime).isBefore(DateUtil.parseDateTime(endTime))) {
      syncOrderPage(1, startTime, oneHourEndTime, null);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      syncOrder(oneHourEndTime, endTime);
    } else {
      syncOrderPage(1, startTime, endTime, null);
    }
  }

  @Override
  public void syncTask(String startTime, String endTime) {
    syncOrderPage(1, startTime, endTime, null);
  }

  private void syncOrderPage(Integer page, String startTime, String endTime, String positionIndex) {
    try {
      // 获取推广链接
      String api = hjkProperties.getApiUrl() + "/tb/getorder";
      Map<String, Object> map =
          new HashMap<String, Object>() {
            {
              put("apikey", hjkProperties.getApiKey());
              put("query_type", 4);
              put("start_time", startTime);
              put("end_time", endTime);
              put("position_index", positionIndex);
              put("page_no", page);
              put("page_size", 50);
            }
          };
      HJKTBOrderResponse response =
          RestTemplateUtil.getInstance().postForObject(api, map, HJKTBOrderResponse.class);
      XLogger.log(
          logger,
          env,
          "Method:[{}],Request:{},Response:{}",
          "SyncTBOrder",
          JsonUtil.toJson(map),
          JsonUtil.toJson(response));
      if (response == null || response.getData() == null) {
        return;
      }
      if (response.getData() instanceof Map) {
        HJKTBOrderResponse.OrderPage orderPage =
            JsonUtil.toObj(JsonUtil.toJson(response.getData()), HJKTBOrderResponse.OrderPage.class);
        if (orderPage != null && orderPage.getResults() != null) {
          List<HJKTBOrderResponse.OrderDto> orderList = new ArrayList<>();
          Object data = orderPage.getResults().getPublisher_order_dto();
          if (data instanceof Map) {
            orderList.add(JsonUtil.toObj(JsonUtil.toJson(data), HJKTBOrderResponse.OrderDto.class));
          } else if (data instanceof List) {
            orderList =
                JsonUtil.toList(
                    JsonUtil.toJson(data),
                    new TypeReference<List<HJKTBOrderResponse.OrderDto>>() {});
          }
          if (orderList != null && orderList.size() > 0) {
            List<OmsOrder> list =
                orderList.stream()
                    .map(
                        item -> {
                          OmsOrder order = new OmsOrder();
                          order.setOrderSource(ProductSource.TB.getCode());
                          order.setOrderId(item.getTradeId());
                          order.setOrderSn(item.getTradeParentId());
                          order.setOrderEmt(2);
                          if (ObjectUtil.isNotEmpty(item.getTkPaidTime())) {
                            order.setOrderTime(
                                LocalDateTime.parse(
                                    item.getTkPaidTime(),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                          }
                          if (ObjectUtil.isNotEmpty(item.getModifiedTime())
                              && ObjectUtil.equals(item.getTkStatus(), 14)) {
                            order.setFinishTime(
                                LocalDateTime.parse(
                                    item.getModifiedTime(),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                          }
                          if (ObjectUtil.isNotEmpty(item.getModifiedTime())) {
                            order.setModifyTime(
                                LocalDateTime.parse(
                                    item.getModifiedTime(),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                          }
                          if (ObjectUtil.isNotEmpty(item.getTkEarningTime())) {
                            order.setSettleTime(
                                LocalDateTime.parse(
                                    item.getTkEarningTime(),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                          }
                          order.setPid(
                              Optional.ofNullable(item.getPubId())
                                  .map(Object::toString)
                                  .orElse(""));
                          order.setSkuId(item.getItemId());
                          order.setSkuName(item.getItemTitle());
                          order.setSkuNum(Long.valueOf(item.getItemNum()));
                          order.setImageUrl(item.getItemImg());
                          order.setPrice(Optional.ofNullable(item.getPayPrice()).orElse("0"));
                          order.setCommissionRate(item.getTotalCommissionRate());
                          order.setActualCosPrice(
                              Optional.ofNullable(item.getPayPrice()).orElse("0"));
                          order.setActualFee(item.getTotalCommissionFee());
                          if (ObjectUtil.equals(item.getTkStatus(), "12")) {
                            order.setStatus(OrderStatus.DELIVER.getCode());
                          } else if (ObjectUtil.equals(item.getTkStatus(), "14")) {
                            order.setStatus(OrderStatus.COMPLETE.getCode());
                          } else if (ObjectUtil.equals(item.getTkEarningTime(), "13")) {
                            order.setStatus(OrderStatus.INVALID.getCode());
                          }
                          String rid = Optional.ofNullable(item.getRelationId()).orElse("");
                          order.setUid(rid);
                          order.setRate(tbProperties.getRate());
                          // 金额小于0.02不算返利
                          order.setRebate(
                              new BigDecimal(item.getTotalCommissionFee())
                                          .compareTo(new BigDecimal("0.025"))
                                      >= 1
                                  ? new BigDecimal(item.getTotalCommissionFee())
                                      .multiply(new BigDecimal(order.getRate()))
                                      .setScale(2, RoundingMode.DOWN)
                                      .toString()
                                  : "0.00");
                          return order;
                        })
                    .collect(Collectors.toList());
            if (list.size() > 0) {
              baseMapper.saveOrUpdateList(list);
            }
            XLogger.log(
                logger,
                env,
                "同步淘宝订单:[{}~{}],第{}页,{}",
                startTime,
                endTime,
                page,
                JsonUtil.toJson(list));
          }
        }
        if (orderPage != null && ObjectUtil.equals(orderPage.getHas_next(), "true")) {
          // 是否还有更多
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          syncOrderPage(page + 1, startTime, endTime, orderPage.getPosition_index());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Object wxMessage(WxMessageRequest request, Long uid) {
    if (request.getContent().contains("m.tb.cn")) {
      Matcher matcher =
          Pattern.compile(Pattern.quote("「") + "(.*?)" + Pattern.quote("」"))
              .matcher(request.getContent());
      if (matcher.find()) {
        String keyword = matcher.group(1).trim();
        ProductQueryParam param = new ProductQueryParam();
        param.setKeyword(keyword);
        CommonPage<HJKJDProduct> page = searchProduct(param);

        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        if (ObjectUtil.isEmpty(page.getList())) {
          wxMessage.setMsgType("text");
          wxMessage.setContent("很遗憾，没有找到该商品的优惠");
        }
        if (page.getList().stream()
            .anyMatch(item -> request.getContent().contains(item.getGoods_name()))) {
          HJKJDProduct product = page.getList().get(0);
          wxMessage.setMsgType("news");
          wxMessage.setArticleCount(1);
          WxMessage.Articles articles = new WxMessage.Articles();
          articles.setTitle("券后价:" + product.getPrice_after() + " 约返:" + product.getRebate());
          articles.setDescription(product.getGoods_name());
          articles.setPicUrl(product.getPicurl());
          if (page.getList().size() > 1) {
            articles.setUrl(
                "https://prod-2glx9khga5692d1f-1314654459.tcloudbaseapp.com/#/search/list?type=3&uid="
                    + uid
                    + "&keyword="
                    + product.getGoods_name());
          } else {
            articles.setUrl(
                "https://prod-2glx9khga5692d1f-1314654459.tcloudbaseapp.com/#/product/detail?type=3&uid="
                    + uid
                    + "&id="
                    + product.getGoods_id());
          }
          wxMessage.setArticles(Collections.singletonList(articles));
          return wxMessage;
        } else {
          HJKJDProduct product = page.getList().get(0);
          wxMessage.setMsgType("news");
          wxMessage.setArticleCount(1);
          WxMessage.Articles articles = new WxMessage.Articles();
          articles.setTitle("没有找到该商品");
          articles.setDescription("查看类似商品");
          articles.setPicUrl(product.getPicurl());
          articles.setUrl(
              "https://prod-2glx9khga5692d1f-1314654459.tcloudbaseapp.com/#/search/list?type=4&uid="
                  + uid
                  + "&keyword="
                  + product.getGoods_name());
          wxMessage.setArticles(Collections.singletonList(articles));
          return wxMessage;
        }
      }
    } else if (request.getContent().contains("item.taobao.com")
        || request.getContent().contains("detail.tmall.com")) {
      String reqContent = URLDecoder.decode(request.getContent(), StandardCharsets.UTF_8);
      String id = reqContent.substring(reqContent.indexOf("id=") + 3).split("&")[0];
      HJKJDProduct product = getProductDetail(id);
      if (product != null) {
        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        wxMessage.setMsgType("news");
        wxMessage.setArticleCount(1);
        WxMessage.Articles articles = new WxMessage.Articles();
        articles.setTitle("券后价:" + product.getPrice_after() + " 约返:" + product.getRebate());
        articles.setDescription(product.getGoods_name());
        articles.setPicUrl(product.getPicurl());
        articles.setUrl(
            "https://prod-2glx9khga5692d1f-1314654459.tcloudbaseapp.com/#/product/detail?type=3&uid="
                + uid
                + "&id="
                + product.getGoods_id());
        wxMessage.setArticles(Collections.singletonList(articles));
        return wxMessage;
      }
    }
    return "success";
  }

  @Override
  public CommonPage<HJKJDProduct> listProduct(ProductQueryParam param) {
    try {
      TbkDgOptimusMaterialRequest request = new TbkDgOptimusMaterialRequest();
      request.setPageNo(param.getPage());
      request.setPageSize(param.getPageSize());
      request.setMaterialId(param.getOptId());
      request.setAdzoneId(tbProperties.getPid());
      TbkDgOptimusMaterialResponse response = getTBClient().execute(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "淘宝商品列表",
          JsonUtil.toJson(request),
          JsonUtil.toJson(response));
      if (response.getResultList() != null && response.getResultList().size() > 0) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            Optional.ofNullable(response.getTotalCount()).orElse(500L),
            response.getResultList().stream()
                .map(
                    (item) -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getItemId());
                      product.setGoods_name(item.getTitle());
                      product.setGoods_desc(item.getItemDescription());
                      product.setPrice(item.getZkFinalPrice());
                      product.setDiscount(item.getCouponAmount().toString());
                      if (ObjectUtil.isNotNull(item.getCouponAmount())) {
                        product.setPrice_after(
                            new BigDecimal(item.getZkFinalPrice())
                                .subtract(new BigDecimal(item.getCouponAmount()))
                                .toString());
                      } else {
                        product.setPrice_after(item.getZkFinalPrice());
                      }
                      BigDecimal commission =
                          new BigDecimal(product.getPrice_after())
                              .multiply(new BigDecimal(item.getCommissionRate()))
                              .divide(new BigDecimal(10000), 2, RoundingMode.DOWN); // 返佣
                      product.setRebate(getRebate(commission));
                      if (StrUtil.equals(env, "dev")) {
                        product.setCommissionshare(
                            new BigDecimal(item.getCommissionRate())
                                .divide(new BigDecimal("100"), 1, RoundingMode.DOWN)
                                .toString());
                        product.setCommission(commission.stripTrailingZeros().toPlainString());
                      }
                      product.setPicurl(item.getPictUrl());
                      if (ObjectUtil.isEmpty(item.getSmallImages())) {
                        product.setPicurls(item.getPictUrl());
                      } else {
                        product.setPicurls(String.join(",", item.getSmallImages()));
                      }
                      try {
                        if (item.getVolume() >= 10000) {
                          product.setSalesTip(
                              String.format(
                                  "月售%s万+件",
                                  new BigDecimal(item.getVolume())
                                      .divide(new BigDecimal("10000"), 1, RoundingMode.DOWN)
                                      .stripTrailingZeros()
                                      .toPlainString()));
                        } else {
                          product.setSalesTip(String.format("月售%d件", item.getVolume()));
                        }
                      } catch (Exception e) {
                        product.setSalesTip(String.format("月售%d件", item.getVolume()));
                      }
                      product.setIs_tmall(ObjectUtil.equals(item.getUserType(), 1L));
                      product.setSource(ProductSource.TB.getCode());
                      return product;
                    })
                .collect(Collectors.toList()));
      }
    } catch (Exception e) {
      e.printStackTrace();
      Asserts.fail(e.getLocalizedMessage());
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public CommonPage<HJKJDProduct> searchProduct(ProductQueryParam param) {
    String api = wykProperties.getApiUrl() + "/tbk/tb_search";
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("vekey", wykProperties.getApiKey());
    map.add("page", param.getPage());
    map.add("pagesize", param.getPageSize());
    map.add("cat", param.getOptId());
    map.add("para", param.getKeyword());
    if (param.getIsTmall() != null) {
      map.add("is_tmall", param.getIsTmall());
    }
    if (param.getSortType() == 6) {
      map.add("sort", "total_sales_des");
    } else if (param.getSortType() == 9) {
      map.add("sort", "price_asc");
    }
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, httpHeaders);

    TBProductListResponse response =
        RestTemplateUtil.getInstance().postForObject(api, request, TBProductListResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "淘宝商品搜索",
        JsonUtil.toJson(map),
        JsonUtil.toJson(response));
    if (response != null
        && response.getResultList() != null
        && response.getResultList().size() > 0) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          response.getTotalResults().longValue(),
          response.getResultList().stream()
              .map(
                  (item) -> {
                    HJKJDProduct product = new HJKJDProduct();
                    product.setGoods_id(item.getItemId());
                    product.setGoods_name(item.getTitle());
                    product.setGoods_desc(item.getItemDescription());
                    product.setPrice(item.getZkFinalPrice());
                    product.setDiscount(item.getCouponAmount());
                    if (ObjectUtil.isNotNull(item.getCouponAmount())) {
                      product.setPrice_after(
                          new BigDecimal(item.getZkFinalPrice())
                              .subtract(new BigDecimal(item.getCouponAmount()))
                              .toString());
                    } else {
                      product.setPrice_after(item.getZkFinalPrice());
                    }
                    BigDecimal commission =
                        new BigDecimal(product.getPrice_after())
                            .multiply(new BigDecimal(item.getCommissionRate()))
                            .divide(new BigDecimal(10000), 2, RoundingMode.DOWN); // 返佣
                    product.setRebate(getRebate(commission));
                    if (StrUtil.equals(env, "dev")) {
                      product.setCommissionshare(
                          new BigDecimal(item.getCommissionRate())
                              .divide(new BigDecimal("100"), 1, RoundingMode.DOWN)
                              .toString());
                      product.setCommission(commission.stripTrailingZeros().toPlainString());
                    }
                    product.setPicurl(item.getPictUrl());
                    if (ObjectUtil.isEmpty(item.getSmallImages())) {
                      product.setPicurls(item.getPictUrl());
                    } else {
                      product.setPicurls(String.join(",", item.getSmallImages()));
                    }
                    try {
                      if (Long.parseLong(item.getTkTotalSales()) >= 10000) {
                        product.setSalesTip(
                            String.format(
                                "月售%s万+件",
                                new BigDecimal(item.getTkTotalSales())
                                    .divide(new BigDecimal("10000"), 1, RoundingMode.DOWN)
                                    .stripTrailingZeros()
                                    .toPlainString()));
                      } else {
                        product.setSalesTip(String.format("月售%s件", item.getTkTotalSales()));
                      }
                    } catch (Exception e) {
                      product.setSalesTip(String.format("月售%s件", item.getTkTotalSales()));
                    }
                    product.setIs_tmall(ObjectUtil.equals(item.getUserType(), 1L));
                    product.setSource(ProductSource.TB.getCode());
                    return product;
                  })
              .collect(Collectors.toList()),
          null);
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public HJKJDProduct getProductDetail(String id) {
    String api = hjkProperties.getApiUrl() + "/tb/getunionurl";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("item_id", id);
          }
        };
    HJKTBLinkResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, HJKTBLinkResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "淘宝商品详情",
        JsonUtil.toJson(map),
        JsonUtil.toJson(response));
    if (response != null && response.getData() != null && response.getData() instanceof Map) {
      HJKTBLinkResponse.TbLink item =
          JsonUtil.toObj(JsonUtil.toJson(response.getData()), HJKTBLinkResponse.TbLink.class);
      HJKJDProduct product = new HJKJDProduct();
      product.setGoods_id(item.getItemId());
      product.setGoods_name(item.getTitle());
      product.setPrice(item.getZkFinalPrice());
      product.setDiscount(item.getCouponAmount());
      if (ObjectUtil.isNotNull(item.getCouponAmount())) {
        product.setPrice_after(
            new BigDecimal(item.getZkFinalPrice())
                .subtract(new BigDecimal(item.getCouponAmount()))
                .toString());
      } else {
        product.setPrice_after(item.getZkFinalPrice());
      }
      BigDecimal commission =
          new BigDecimal(product.getPrice_after())
              .multiply(new BigDecimal(item.getCommissionRate()))
              .divide(new BigDecimal(10000), 2, RoundingMode.DOWN); // 返佣
      product.setRebate(getRebate(commission));
      if (StrUtil.equals(env, "dev")) {
        product.setCommissionshare(
            new BigDecimal(item.getCommissionRate())
                .divide(new BigDecimal("100"), 1, RoundingMode.DOWN)
                .toString());
        product.setCommission(commission.stripTrailingZeros().toPlainString());
      }
      product.setPicurl(item.getPictUrl());
      if (ObjectUtil.isEmpty(item.getSmallImages())) {
        product.setPicurls(item.getPictUrl());
      } else {
        product.setPicurls(String.join(",", item.getSmallImages().getString()));
      }
      try {
        if (Long.parseLong(item.getVolume()) >= 10000) {
          product.setSalesTip(
              String.format(
                  "月售%s万+件",
                  new BigDecimal(item.getVolume())
                      .divide(new BigDecimal("10000"), 1, RoundingMode.DOWN)
                      .stripTrailingZeros()
                      .toPlainString()));
        } else {
          product.setSalesTip(String.format("月售%s件", item.getVolume()));
        }
      } catch (Exception e) {
        product.setSalesTip(String.format("月售%s件", item.getVolume()));
      }
      product.setIs_tmall(ObjectUtil.equals(item.getUserType(), "1"));
      product.setSource(ProductSource.TB.getCode());
      return product;
    }
    return null;
  }

  @Override
  public Object getUnionUrl(String id, String uid) {
    UmsUserTb userTb =
        umsUserTbService.lambdaQuery().eq(UmsUserTb::getUid, RequestHolder.getUid()).one();
    if (ObjectUtil.isNull(userTb)) {
      Asserts.fail(ResultCode.NO_TB_AUTH);
    }

    // 获取推广链接
    String api = hjkProperties.getApiUrl() + "/tb/getunionurl";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("item_id", id);
            put("pid", tbProperties.getXpid());
            put("relation_id", userTb.getRelationId());
            put("get_tkl", 1);
          }
        };
    HJKTBLinkResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, HJKTBLinkResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "淘宝商品转链",
        JsonUtil.toJson(map),
        JsonUtil.toJson(response));
    if (response != null && response.getData() != null) {
      return JsonUtil.toObj(JsonUtil.toJson(response.getData()), HJKTBLinkResponse.TbLink.class);
    }
    return null;
  }

  @Override
  public String getRelation(String uid) {
    String api = wykProperties.getApiUrl() + "/tbk/publisherget";
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("vekey", wykProperties.getApiKey());
    map.add("page_no", 0);
    map.add("page_size", 100000);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, httpHeaders);

    TBRelationResponse response =
        RestTemplateUtil.getInstance().postForObject(api, param, TBRelationResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "淘宝渠道获取",
        JsonUtil.toJson(map),
        JsonUtil.toJson(response));
    if (response != null
        && response.getData() != null
        && response.getData().getInviterList() != null
        && ObjectUtil.isNotEmpty(response.getData().getInviterList().getMapData())) {
      TBRelationResponse.DataDTO.InviterListDTO.MapDataDTO mapDataDTO =
          response.getData().getInviterList().getMapData().stream()
              .filter(item -> ObjectUtil.equals(item.getRtag(), uid))
              .findFirst()
              .orElse(null);
      if (mapDataDTO != null) {
        UmsUserTb userTb = new UmsUserTb();
        userTb.setUid(Long.parseLong(uid));
        userTb.setRelationId(mapDataDTO.getRelationId().toString());
        userTb.setAccountName(mapDataDTO.getAccountName());
        umsUserTbService.save(userTb);
        return mapDataDTO.getRelationId().toString();
      }
    }

    return null;
  }

  private String getRebate(BigDecimal commission) {
    if (commission.compareTo(new BigDecimal("0.025")) < 1) {
      return "0";
    }
    return commission
        .multiply(new BigDecimal(tbProperties.getRate()))
        .setScale(2, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString();
  }
}
