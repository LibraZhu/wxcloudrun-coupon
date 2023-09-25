package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalRequest;
import com.taobao.api.request.TbkDgOptimusMaterialRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import com.taobao.api.response.TbkDgOptimusMaterialResponse;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.HJKProperties;
import com.tencent.wxcloudrun.config.properties.TBProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.*;
import com.tencent.wxcloudrun.enums.OrderStatus;
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.model.UmsUserTb;
import com.tencent.wxcloudrun.service.TBService;
import com.tencent.wxcloudrun.service.UmsUserTbService;
import com.tencent.wxcloudrun.utils.JsonUtil;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import com.tencent.wxcloudrun.utils.XLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TBServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements TBService {

  final Logger logger = LoggerFactory.getLogger(TBServiceImpl.class);

  @Resource private TBProperties tbProperties;

  @Resource private HJKProperties hjkProperties;

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

        if (ObjectUtil.isNotNull(orderPage.getResults())
            && ObjectUtil.isNotEmpty(orderPage.getResults().getPublisher_order_dto())) {
          List<String> sidList = new ArrayList<>();
          List<OmsOrder> list =
              orderPage.getResults().getPublisher_order_dto().stream()
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
                            Optional.ofNullable(item.getPubId()).map(Object::toString).orElse(""));
                        order.setSkuId(item.getItemId());
                        order.setSkuName(item.getItemTitle());
                        order.setSkuNum(Long.valueOf(item.getItemNum()));
                        order.setImageUrl(item.getItemImg());
                        order.setPrice(item.getItemPrice());
                        order.setCommissionRate(item.getTotalCommissionRate());
                        order.setActualCosPrice(item.getPayPrice());
                        order.setActualFee(item.getTotalCommissionFee());
                        if (ObjectUtil.equals(item.getTkStatus(), 12)) {
                          order.setStatus(OrderStatus.DELIVER.getCode());
                        } else if (ObjectUtil.equals(item.getTkStatus(), 14)) {
                          order.setStatus(OrderStatus.COMPLETE.getCode());
                        } else if (ObjectUtil.equals(item.getTkEarningTime(), 13)) {
                          order.setStatus(OrderStatus.INVALID.getCode());
                        }
                        String sid =
                            Optional.ofNullable(item.getSpecialId())
                                .map(Object::toString)
                                .orElse("");
                        order.setUid(sid);

                        // 缓存所有的sid，后续获取uid
                        if (ObjectUtil.isNotEmpty(sid) && !sidList.contains(sid)) {
                          sidList.add(sid);
                        }
                        order.setRate(tbProperties.getRate());
                        // 金额小于0.02不算返利
                        order.setRebate(
                            new BigDecimal(item.getTotalCommissionFee())
                                        .compareTo(new BigDecimal("0.02"))
                                    >= 1
                                ? new BigDecimal(order.getActualFee())
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

          getUidBySpecialId(sidList);
        }
        if (ObjectUtil.equals(orderPage.getHas_next(), "true")) {
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

  /**
   * 根据sid获取uid
   *
   * @param sidList sid列表
   */
  private void getUidBySpecialId(List<String> sidList) {
    if (ObjectUtil.isEmpty(sidList)) {
      return;
    }
    umsUserTbService.lambdaQuery().in(UmsUserTb::getSpecialId, sidList).select().list().stream()
        .map(UmsUserTb::getSpecialId)
        .forEach(sidList::remove);
    if (ObjectUtil.isEmpty(sidList)) {
      return;
    }
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.execute(
        () -> {
          sidList.forEach(
              sid -> {
                String uid = getUidBySpecialId(sid);
                if (ObjectUtil.isNotEmpty(uid)) {
                  UmsUserTb umsUserTb = new UmsUserTb();
                  umsUserTb.setUid(Long.valueOf(uid));
                  umsUserTb.setSpecialId(sid);
                  umsUserTbService.getBaseMapper().insert(umsUserTb);
                }
              });
        });
    executorService.shutdown();
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
        if (ObjectUtil.isNotEmpty(page.getList())) {
          HJKJDProduct product = page.getList().get(0);
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
          HJKTBLinkResponse.TbLink item =
              (HJKTBLinkResponse.TbLink) getUnionUrl(product.getGoods_id(), uid.toString());
          articles.setUrl(
              Optional.ofNullable(item)
                  .map(HJKTBLinkResponse.TbLink::getCouponClickUrl)
                  .orElse(""));
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
        HJKTBLinkResponse.TbLink item =
            (HJKTBLinkResponse.TbLink) getUnionUrl(product.getGoods_id(), uid.toString());
        articles.setUrl(
            Optional.ofNullable(item).map(HJKTBLinkResponse.TbLink::getCouponClickUrl).orElse(""));
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
      if (response.getResultList() != null) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            response.getTotalCount(),
            response.getResultList().stream()
                .map(
                    (item) -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getItemId());
                      product.setGoods_name(item.getTitle());
                      product.setGoods_desc(item.getItemDescription());
                      BigDecimal commission =
                          new BigDecimal(item.getZkFinalPrice())
                              .multiply(new BigDecimal(item.getCommissionRate()))
                              .divide(new BigDecimal(10000), 2, RoundingMode.DOWN); // 返佣
                      product.setPrice(item.getReservePrice());
                      product.setPrice_after(item.getZkFinalPrice());
                      product.setDiscount(item.getCouponAmount().toString());
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
    try {
      TbkDgMaterialOptionalRequest request = new TbkDgMaterialOptionalRequest();
      request.setPageNo(param.getPage());
      request.setPageSize(param.getPageSize());
      request.setPlatform(2L);
      request.setIsTmall(param.getIsTmall());
      if (ObjectUtil.isNotEmpty(param.getOptId())) {
        switch (param.getOptId().intValue()) {
          case 1:
            request.setCat("35,50006004,50008165,50067081,5002925350024576");
            break;
          case 2:
            request.setCat("21");
            break;
          case 3:
            request.setCat("50002766");
            break;
          case 4:
            request.setCat("16");
            break;
          case 5:
            request.setCat("1801,50026391");
            break;
          case 6:
            request.setCat("50006842,50006843,50010388,50011740,50016853,");
            break;
          case 7:
            request.setCat("30");
            break;
          case 8:
            request.setCat("14");
            break;
          default:
            request.setCat("50005998,21");
            break;
        }
      }
      if (param.getSortType() == 6) {
        request.setSort("total_sales_des");
      } else if (param.getSortType() == 9) {
        request.setSort("price_asc");
      }
      request.setQ(param.getKeyword());
      request.setAdzoneId(tbProperties.getPid());
      request.setPageResultKey(param.getListId());
      TbkDgMaterialOptionalResponse response = getTBClient().execute(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "淘宝商品搜索",
          JsonUtil.toJson(request),
          JsonUtil.toJson(response));
      if (response.getResultList() != null) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            response.getTotalResults(),
            response.getResultList().stream()
                .map(
                    (item) -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getItemId());
                      product.setGoods_name(item.getTitle());
                      product.setGoods_desc(item.getItemDescription());
                      BigDecimal commission =
                          new BigDecimal(item.getZkFinalPrice())
                              .multiply(new BigDecimal(item.getCommissionRate()))
                              .divide(new BigDecimal(10000), 2, RoundingMode.DOWN); // 返佣
                      product.setPrice(item.getReservePrice());
                      product.setPrice_after(item.getZkFinalPrice());
                      product.setDiscount(item.getCouponAmount());
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
            response.getPageResultKey());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Asserts.fail(e.getLocalizedMessage());
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
      BigDecimal commission =
          new BigDecimal(item.getZkFinalPrice())
              .multiply(new BigDecimal(item.getCommissionRate()))
              .divide(new BigDecimal(10000), 2, RoundingMode.DOWN); // 返佣
      product.setPrice(item.getReservePrice());
      product.setPrice_after(item.getZkFinalPrice());
      product.setDiscount(
          new BigDecimal(item.getReservePrice())
              .subtract(new BigDecimal(item.getZkFinalPrice()))
              .stripTrailingZeros()
              .toPlainString());
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
    // 获取推广链接
    String api = hjkProperties.getApiUrl() + "/tb/getunionurl";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("item_id", id);
            put("pid", tbProperties.getXpid());
            put("external_id", uid);
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
      return response.getData();
    }
    return null;
  }

  @Override
  public String getUidBySpecialId(String specialId) {
    // 获取推广链接
    String api = hjkProperties.getApiUrl() + "/tb/publisherget";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("info_type", 2);
            put("special_id", specialId);
          }
        };
    HJKTBInviterResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, HJKTBInviterResponse.class);
    XLogger.log(
        logger,
        env,
        "Method:[{}],Request:{},Response:{}",
        "根据会员id获取uid",
        JsonUtil.toJson(map),
        JsonUtil.toJson(response));

    return Optional.ofNullable(response)
        .map(HJKTBInviterResponse::getData)
        .map(HJKTBInviterResponse.TbInviterData::getInviter_list)
        .map(HJKTBInviterResponse.TbInviterList::getMap_data)
        .map(HJKTBInviterResponse.TbMapData::getExternal_id)
        .orElse("");
  }

  @Override
  public String getSpecialIdByUid(String uid) {
    // 获取推广链接
    String api = hjkProperties.getApiUrl() + "/tb/publisherget";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("info_type", 2);
            put("external_id", uid);
          }
        };
    HJKTBInviterResponse response =
        RestTemplateUtil.getInstance().postForObject(api, map, HJKTBInviterResponse.class);
    XLogger.log(
        logger,
        env,
        "Method:[{}],Request:{},Response:{}",
        "根据uid获取淘宝会员id",
        JsonUtil.toJson(map),
        JsonUtil.toJson(response));
    return Optional.ofNullable(response)
        .map(HJKTBInviterResponse::getData)
        .map(HJKTBInviterResponse.TbInviterData::getInviter_list)
        .map(HJKTBInviterResponse.TbInviterList::getMap_data)
        .map(HJKTBInviterResponse.TbMapData::getSpecial_id)
        .orElse("");
  }

  private String getRebate(BigDecimal commission) {
    if (commission.compareTo(new BigDecimal("0.02")) < 1) {
      return "0";
    }
    return commission
        .multiply(new BigDecimal(tbProperties.getRate()))
        .setScale(2, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString();
  }
}
