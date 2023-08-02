package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.request.query.GoodsReq;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.request.query.JFGoodsReq;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.UrlInfo;
import com.jd.open.api.sdk.domain.kplunion.OrderService.request.query.OrderRowReq;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsJingfenQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenOrderRowQueryRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsJingfenQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenOrderRowQueryResponse;
import com.pdd.pop.sdk.common.util.DigestUtil;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.HDKProperties;
import com.tencent.wxcloudrun.config.properties.HJKProperties;
import com.tencent.wxcloudrun.config.properties.JDProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.*;
import com.tencent.wxcloudrun.enums.OrderSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.JDService;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JDServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements JDService {
  final Logger logger = LoggerFactory.getLogger(JDServiceImpl.class);

  @Resource private JDProperties jdProperties;

  @Resource private HJKProperties hjkProperties;

  @Resource private HDKProperties hdkProperties;

  @Value("${spring.profiles.active}")
  private String env;

  private DefaultJdClient getJDClient() {
    return new DefaultJdClient(
        jdProperties.getServerUrl(), null, jdProperties.getAppKey(), jdProperties.getAppSecret());
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
    syncOrderPage(1, startTime, oneHourEndTime);
    // 如果一个小时后时间在结束时间之前，继续查询
    if (DateUtil.parseDateTime(oneHourEndTime).isBefore(DateUtil.parseDateTime(endTime))) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      syncOrder(oneHourEndTime, endTime);
    }
  }

  @Override
  public void syncTask(String startTime, String endTime) {
    syncOrderPage(1, startTime, endTime);
  }

  private void syncOrderPage(Integer page, String startTime, String endTime) {
    try {
      UnionOpenOrderRowQueryRequest orderRowQueryRequest = new UnionOpenOrderRowQueryRequest();
      OrderRowReq orderReq = new OrderRowReq();
      orderReq.setType(3);
      orderReq.setPageSize(50);
      orderReq.setPageIndex(page);
      orderReq.setStartTime(startTime);
      orderReq.setEndTime(endTime);
      orderRowQueryRequest.setOrderReq(orderReq);
      orderRowQueryRequest.setVersion("1.0");
      UnionOpenOrderRowQueryResponse response = getJDClient().execute(orderRowQueryRequest);
      if (StrUtil.equals(env, "dev")) {
        logger.info(
            "Method:[{}],Request:{},Response:{}",
            "SyncJDOrder",
            JsonUtil.transferToJson(orderReq),
            JsonUtil.transferToJson(response));
      }
      if (response.getQueryResult() != null && response.getQueryResult().getData() != null) {
        List<OmsOrder> list =
            Arrays.stream(response.getQueryResult().getData())
                .flatMap(
                    (item) -> {
                      OmsOrder order = new OmsOrder();
                      order.setOrderSource(OrderSource.JD.getCode());
                      order.setOrderId(item.getOrderId().toString());
                      order.setOrderSn(item.getId());
                      order.setOrderEmt(item.getOrderEmt());
                      if (ObjectUtil.isNotEmpty(item.getOrderTime())) {
                        order.setOrderTime(
                            LocalDateTime.parse(
                                item.getOrderTime(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                      }
                      if (ObjectUtil.isNotEmpty(item.getFinishTime())) {
                        order.setFinishTime(
                            LocalDateTime.parse(
                                item.getFinishTime(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                      }
                      if (ObjectUtil.isNotEmpty(item.getModifyTime())) {
                        order.setModifyTime(
                            LocalDateTime.parse(
                                item.getModifyTime(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                      }
                      order.setPlus(item.getPlus());
                      order.setUnionId(item.getUnionId().toString());
                      order.setPositionId(item.getPositionId().toString());
                      order.setPid(item.getPid());
                      order.setSkuId(item.getSkuId().toString());
                      order.setSkuName(item.getSkuName());
                      order.setSkuNum(Long.valueOf(item.getSkuNum()));
                      order.setImageUrl(item.getGoodsInfo().getImageUrl());
                      order.setPrice(item.getPrice().toString());
                      order.setCommissionRate(item.getCommissionRate().toString());
                      order.setFinalRate(item.getFinalRate().toString());
                      order.setEstimateCosPrice(item.getEstimateCosPrice().toString());
                      order.setEstimateFee(item.getEstimateFee().toString());
                      order.setActualCosPrice(item.getActualCosPrice().toString());
                      order.setActualFee(item.getActualFee().toString());
                      order.setStatus(item.getValidCode());
                      order.setUid(item.getSubUnionId());
                      return Arrays.stream(new OmsOrder[] {order});
                    })
                .collect(Collectors.toList());
        if (list.size() > 0) {
          baseMapper.saveOrUpdateList(list);
        }
        if (StrUtil.equals(env, "dev")) {
          logger.info(
              "同步订单:[{}~{}],第{}页,{}", startTime, endTime, page, JsonUtil.transferToJson(list));
        }
        if (response.getQueryResult() != null && response.getQueryResult().getHasMore()) {
          // 是否还有更多
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          syncOrderPage(page + 1, startTime, endTime);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Object wxMessage(WxMessageRequest request) {
    try {
      String url = request.getContent().split("\\?")[0];
      String[] splits = url.split("/");
      String id = splits[splits.length - 1].replace(".html", "");

      HJKJDProduct productDetail = getHJKProductDetail(id);
      if (productDetail != null) {
        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        wxMessage.setMsgType("news");
        wxMessage.setArticleCount(1);
        List<WxMessage.Articles> articlesList = new ArrayList<>();
        WxMessage.Articles articles = new WxMessage.Articles();
        BigDecimal rebate =
            new BigDecimal(productDetail.getCommission())
                .multiply(new BigDecimal(jdProperties.getRate()));

        if (productDetail.getPlusCommissionShare() != null) {
          rebate =
              rebate.multiply(
                  new BigDecimal(productDetail.getPlusCommissionShare())
                      .divide(
                          new BigDecimal(productDetail.getCommissionshare()),
                          2,
                          RoundingMode.HALF_DOWN));
        }
        String rebateStr =
            rebate.setScale(2, RoundingMode.HALF_DOWN).stripTrailingZeros().toPlainString();
        articles.setTitle("券后价:" + productDetail.getPrice_after() + " 约返:" + rebateStr);
        articles.setDescription("【领券下单拿返现】" + productDetail.getGoods_name());
        articles.setPicUrl(productDetail.getPicurl().replace("/jfs", "/s200x200_jfs"));
        String unionUrl =
            getHDKUnionUrl(
                id, productDetail.getCouponurl(), DigestUtil.md5(request.getFromUserName()));
        if (StrUtil.equals(env, "dev")) {
          logger.info(
              "[{}],[{}],[{}],[{}],[{}]",
              "JD价格" + productDetail.getPrice(),
              "券后价" + productDetail.getPrice_after(),
              "返佣" + productDetail.getCommission(),
              "返利" + rebateStr,
              unionUrl);
        }
        if (unionUrl != null) {
          articles.setUrl(unionUrl);
          articlesList.add(articles);
          wxMessage.setArticles(articlesList);
          return wxMessage;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "success";
  }

  @Override
  public CommonPage<HJKJDProduct> listProduct(JDProductListParam param) {
    try {
      UnionOpenGoodsJingfenQueryRequest request = new UnionOpenGoodsJingfenQueryRequest();
      JFGoodsReq goodsReq = new JFGoodsReq();
      goodsReq.setEliteId(param.getEliteId());
      goodsReq.setPageIndex(param.getPage().intValue());
      goodsReq.setPageSize(param.getPageSize().intValue());
      goodsReq.setSortName(param.getSortName());
      goodsReq.setSort(param.getSort());
      goodsReq.setPid(jdProperties.getPid());
      request.setGoodsReq(goodsReq);
      request.setVersion("1.0");
      UnionOpenGoodsJingfenQueryResponse response = getJDClient().execute(request);
      if (response.getQueryResult().getCode() != 200) {
        Asserts.fail(response.getQueryResult().getMessage());
      }

      if (response.getQueryResult().getData() != null) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            response.getQueryResult().getTotalCount(),
            Arrays.stream(response.getQueryResult().getData())
                .flatMap(
                    (item) -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getSkuId().toString());
                      product.setGoods_name(item.getSkuName());
                      product.setGoods_desc(
                          item.getDocumentInfo() != null
                              ? item.getDocumentInfo().getDocument()
                              : "");
                      product.setPrice(item.getPriceInfo().getLowestPrice().toString());
                      product.setPrice_after(item.getPriceInfo().getLowestCouponPrice().toString());
                      BigDecimal discount =
                          new BigDecimal(item.getPriceInfo().getLowestPrice().toString())
                              .subtract(
                                  new BigDecimal(
                                      item.getPriceInfo().getLowestCouponPrice().toString()));
                      product.setDiscount(discount.stripTrailingZeros().toPlainString());
                      if (item.getCouponInfo().getCouponList() != null) {
                        Arrays.stream(item.getCouponInfo().getCouponList())
                            .filter(i -> i.getDiscount() == discount.doubleValue())
                            .findFirst()
                            .ifPresent(coupon -> product.setCouponurl(coupon.getLink()));
                      }
                      if (item.getImageInfo().getImageList().length > 0) {
                        product.setPicurl(item.getImageInfo().getImageList()[0].getUrl());
                      }
                      product.setPicurls(
                          Arrays.stream(item.getImageInfo().getImageList())
                              .map(UrlInfo::getUrl)
                              .collect(Collectors.joining(",")));
                      product.setSales(item.getInOrderCount30DaysSku());
                      product.setIspg(
                          item.getPinGouInfo() != null
                                  && item.getPinGouInfo().getPingouTmCount() != null
                                  && item.getPinGouInfo().getPingouTmCount() > 1
                              ? 1
                              : 0);
                      product.setOwner(item.getOwner());
                      product.setComments(item.getComments());
                      if (item.getShopInfo() != null) {
                        product.setShopname(item.getShopInfo().getShopName());
                      }
                      if (item.getCommissionInfo() != null) {
                        if (StrUtil.equals(env, "dev")) {
                          product.setCommission(
                              item.getCommissionInfo().getCouponCommission().toString());
                          product.setCommissionshare(
                              item.getCommissionInfo().getCommissionShare().toString());
                          product.setPlusCommissionShare(
                              item.getCommissionInfo().getPlusCommissionShare().toString());
                        }
                        BigDecimal rebate =
                            new BigDecimal(
                                    item.getCommissionInfo().getCouponCommission().toString())
                                .multiply(new BigDecimal(jdProperties.getRate()));
                        if (item.getCommissionInfo().getPlusCommissionShare() != null) {
                          rebate =
                              rebate.multiply(
                                  new BigDecimal(
                                          item.getCommissionInfo()
                                              .getPlusCommissionShare()
                                              .toString())
                                      .divide(
                                          new BigDecimal(
                                              item.getCommissionInfo()
                                                  .getCommissionShare()
                                                  .toString()),
                                          2,
                                          RoundingMode.HALF_DOWN));
                        }
                        product.setRebate(
                            rebate
                                .setScale(2, RoundingMode.HALF_DOWN)
                                .stripTrailingZeros()
                                .toPlainString());
                      }
                      return Arrays.stream(new HJKJDProduct[] {product});
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
  public CommonPage<HJKJDProduct> searchHJKProduct(HJKJDProductQueryParam param) {
    String api = hjkProperties.getApiUrl() + "/jd/goodslist";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("pageindex", param.getPage());
            put("pagesize", param.getPageSize());
            put("keyword", param.getKeyword());
            put("sortName", param.getSortName());
            put("sort", param.getSort());
            put("owner", param.getOwner());
          }
        };
    HJKJDProductListResponse hjkjdProductListResponse =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, HJKJDProductListResponse.class);
    if (StrUtil.equals(env, "dev")) {
      logger.info(
          "[{}],Request:{},Response:{}",
          "hjk京东商品列表",
          JsonUtil.transferToJson(searchParams),
          JsonUtil.transferToJson(hjkjdProductListResponse));
    }
    if (hjkjdProductListResponse == null) {
      Asserts.fail("服务器异常");
    }
    if (hjkjdProductListResponse.getStatus_code() != 200) {
      Asserts.fail(hjkjdProductListResponse.getMessage());
    }
    // 计算返利
    hjkjdProductListResponse
        .getData()
        .getData()
        .forEach(
            product -> {
              BigDecimal rebate =
                  new BigDecimal(product.getCommission())
                      .multiply(new BigDecimal(jdProperties.getRate()));
              if (product.getPlusCommissionShare() != null) {
                rebate =
                    rebate.multiply(
                        new BigDecimal(product.getPlusCommissionShare())
                            .divide(
                                new BigDecimal(product.getCommissionshare()),
                                2,
                                RoundingMode.HALF_DOWN));
              }
              product.setRebate(
                  rebate.setScale(2, RoundingMode.HALF_DOWN).stripTrailingZeros().toPlainString());
            });

    return CommonPage.page(
        param.getPage(),
        param.getPageSize(),
        hjkjdProductListResponse.getData().getTotal(),
        hjkjdProductListResponse.getData().getData());
  }

  @Override
  public HJKJDProduct getHJKProductDetail(String id) {
    // 获取详情
    String api = hjkProperties.getApiUrl() + "/jd/goodsdetail";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("goods_id", Long.valueOf(id));
          }
        };
    ResponseEntity<HJKJDProductDetailResponse> detailResponseEntity =
        RestTemplateUtil.getInstance()
            .postForEntity(api, searchParams, HJKJDProductDetailResponse.class);
    if (StrUtil.equals(env, "dev")) {
      logger.info(
          "[{}],Request:{},Response:{}",
          "hjk京东商品详情",
          JsonUtil.transferToJson(searchParams),
          JsonUtil.transferToJson(detailResponseEntity));
    }
    if (detailResponseEntity.getBody() != null
        && detailResponseEntity.getBody().getData() != null) {
      // 计算返利
      HJKJDProduct product = detailResponseEntity.getBody().getData();
      BigDecimal rebate =
          new BigDecimal(product.getCommission()).multiply(new BigDecimal(jdProperties.getRate()));
      if (product.getPlusCommissionShare() != null) {
        rebate =
            rebate.multiply(
                new BigDecimal(product.getPlusCommissionShare())
                    .divide(
                        new BigDecimal(product.getCommissionshare()), 2, RoundingMode.HALF_DOWN));
      }
      product.setRebate(
          rebate.setScale(2, RoundingMode.HALF_DOWN).stripTrailingZeros().toPlainString());
      return product;
    }
    return null;
  }

  @Override
  public String getHJKUnionUrl(String id) {
    // 获取推广链接
    String hjkApi = hjkProperties.getApiUrl() + "/jd/getunionurl";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("goods_id", Long.valueOf(id));
            put("positionid", jdProperties.getPositionId());
            put("type", 1);
            put("giftCouponKey", "");
          }
        };
    ResponseEntity<HJKJDProductLinkResponse> productLinkResponse =
        RestTemplateUtil.getInstance().postForEntity(hjkApi, map, HJKJDProductLinkResponse.class);
    if (StrUtil.equals(env, "dev")) {
      logger.info(
          "[{}],Request:{},Response:{}",
          "hjk京东转链",
          JsonUtil.transferToJson(map),
          JsonUtil.transferToJson(productLinkResponse));
    }
    if (productLinkResponse.getBody() != null && productLinkResponse.getBody().getData() != null) {
      return productLinkResponse.getBody().getData();
    }
    return null;
  }

  @Override
  public String getHDKUnionUrl(String id, String coupon_url, String subUnionId) {
    // 获取推广链接
    String hdkApi = hdkProperties.getApiUrl() + "/get_jditems_link";
    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    map.add("apikey", hdkProperties.getApiKey());
    try {
      map.add("material_id", Long.valueOf(id));
    } catch (Exception e) {
      e.printStackTrace();
      Asserts.fail("商品ID不正确");
    }
    map.add("coupon_url", coupon_url);
    map.add("union_id", jdProperties.getUnionId());
    map.add("pid", jdProperties.getPid());
    map.add("subUnionId", subUnionId);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, httpHeaders);
    HDKJDProductLinkResponse productLinkResponse =
        RestTemplateUtil.getInstance().postForObject(hdkApi, param, HDKJDProductLinkResponse.class);
    if (StrUtil.equals(env, "dev")) {
      logger.info(
          "[{}],Request:{},Response:{}",
          "hdk京东转链",
          JsonUtil.transferToJson(map),
          JsonUtil.transferToJson(productLinkResponse));
    }
    if (productLinkResponse != null && productLinkResponse.getData() != null) {
      return productLinkResponse.getData().getShort_url();
    }
    return null;
  }

  public static void main(String[] args) {
    JdClient client =
        new DefaultJdClient(
            "https://api.jd.com/routerjson",
            null,
            "fd07e7373553e152016b787fba46c182",
            "eaf58fbf234c406186632d3dcf17771a");
    UnionOpenGoodsQueryRequest request = new UnionOpenGoodsQueryRequest();
    GoodsReq goodsReqDTO = new GoodsReq();
    request.setGoodsReqDTO(goodsReqDTO);
    request.setVersion("1.0");
    try {
      UnionOpenGoodsQueryResponse response = client.execute(request);
      System.out.println(JsonUtil.transferToJson(response));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
