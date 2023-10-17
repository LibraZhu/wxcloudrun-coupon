package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.*;
import com.pdd.pop.sdk.http.api.pop.response.*;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.ClientProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.dto.ProductQueryParam;
import com.tencent.wxcloudrun.dto.WxMessage;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.enums.OrderStatus;
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.PddService;
import com.tencent.wxcloudrun.utils.JsonUtil;
import com.tencent.wxcloudrun.utils.XLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PddServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements PddService {

  final Logger logger = LoggerFactory.getLogger(PddServiceImpl.class);

  @Resource private ClientProperties clientProperties;

  @Value("${spring.profiles.active}")
  private String env;

  private PopClient getPddClient() {
    return new PopHttpClient(clientProperties.getClientId(), clientProperties.getClientSecret());
  }

  @Override
  public Object pddAuthUrl() {
    PddDdkRpPromUrlGenerateRequest request = new PddDdkRpPromUrlGenerateRequest();
    request.setChannelType(10);
    List<String> pIdList = new ArrayList<>();
    pIdList.add(clientProperties.getPid());
    request.setPIdList(pIdList);
    request.setCustomParameters(
        JsonUtil.toJson(
            new HashMap<String, Object>() {
              {
                put("uid", clientProperties.getUid());
              }
            }));
    try {
      // 同步调用
      return getPddClient().syncInvoke(request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
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
      syncOrderPage(1, startTime, oneHourEndTime);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      syncOrder(oneHourEndTime, endTime);
    } else {
      syncOrderPage(1, startTime, endTime);
    }
  }

  @Override
  public void syncTask(String startTime, String endTime) {
    syncOrderPage(1, startTime, endTime);
  }

  private void syncOrderPage(Integer page, String startTime, String endTime) {
    try {
      PddDdkOrderListIncrementGetRequest request = new PddDdkOrderListIncrementGetRequest();
      request.setCashGiftOrder(false);
      request.setStartUpdateTime(DateUtil.parseDateTime(startTime).getTime() / 1000);
      request.setEndUpdateTime(DateUtil.parseDateTime(endTime).getTime() / 1000);
      request.setPage(page);
      request.setPageSize(50);
      request.setReturnCount(false);
      PddDdkOrderListIncrementGetResponse response = getPddClient().syncInvoke(request);
      XLogger.log(
          logger,
          env,
          "Method:[{}],Request:{},Response:{}",
          "SyncPddOrder",
          JsonUtil.toJson(request),
          JsonUtil.toJson(response));
      if (response.getOrderListGetResponse() != null
          && ObjectUtil.isNotEmpty(response.getOrderListGetResponse().getOrderList())) {
        List<OmsOrder> list =
            response.getOrderListGetResponse().getOrderList().stream()
                .map(
                    (item) -> {
                      OmsOrder order = new OmsOrder();
                      order.setOrderSource(ProductSource.PDD.getCode());
                      order.setOrderId(item.getOrderSn());
                      order.setOrderSn(item.getOrderId());
                      if (ObjectUtil.isNotEmpty(item.getOrderPayTime())) {
                        order.setOrderTime(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(item.getOrderPayTime()),
                                ZoneId.systemDefault()));
                      }
                      if (ObjectUtil.isNotEmpty(item.getOrderReceiveTime())) {
                        order.setFinishTime(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(item.getOrderReceiveTime()),
                                ZoneId.systemDefault()));
                      }
                      if (ObjectUtil.isNotEmpty(item.getOrderModifyAt())) {
                        order.setModifyTime(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(item.getOrderModifyAt()),
                                ZoneId.systemDefault()));
                      }
                      if (ObjectUtil.isNotEmpty(item.getOrderSettleTime())) {
                        order.setSettleTime(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(item.getOrderSettleTime()),
                                ZoneId.systemDefault()));
                      }
                      order.setCompared(item.getPriceCompareStatus());
                      order.setPid(item.getPId());
                      order.setSkuId(item.getGoodsSign());
                      order.setSkuName(item.getGoodsName());
                      order.setSkuNum(item.getGoodsQuantity());
                      order.setImageUrl(item.getGoodsThumbnailUrl());
                      order.setPrice(
                          new BigDecimal(item.getOrderAmount().toString())
                              .divide(new BigDecimal(100), 2, RoundingMode.DOWN)
                              .toString());
                      order.setCommissionRate(
                          new BigDecimal(item.getPromotionRate().toString())
                              .divide(new BigDecimal(10), 1, RoundingMode.DOWN)
                              .toString());
                      order.setActualCosPrice(
                          new BigDecimal(item.getOrderAmount().toString())
                              .divide(new BigDecimal(100), 2, RoundingMode.DOWN)
                              .toString());
                      order.setActualFee(
                          new BigDecimal(item.getPromotionAmount().toString())
                              .divide(new BigDecimal(100), 2, RoundingMode.DOWN)
                              .toString());
                      order.setStatus(item.getOrderStatus());
                      if (item.getOrderStatus() == 0 || item.getOrderStatus() == 1) {
                        order.setStatus(OrderStatus.DELIVER.getCode());
                      } else if (item.getOrderStatus() == 2) {
                        order.setStatus(OrderStatus.COMPLETE.getCode());
                      } else if (item.getOrderStatus() == 4 || item.getOrderStatus() == 10) {
                        order.setStatus(OrderStatus.INVALID.getCode());
                        order.setStatusDes(item.getOrderStatus() + "-" + item.getOrderStatusDesc());
                      }
                      try {
                        JSONObject customParameters = JSONUtil.parseObj(item.getCustomParameters());
                        order.setUid(customParameters.getStr("sid"));
                      } catch (Exception ee) {
                        ee.printStackTrace();
                      }
                      order.setRate(clientProperties.getRate());
                      // 金额小于0.02不算返利
                      order.setRebate(
                          item.getPromotionAmount() >= 2
                              ? new BigDecimal(order.getActualFee())
                                  .multiply(new BigDecimal(order.getRate()))
                                  .setScale(2, RoundingMode.DOWN)
                                  .stripTrailingZeros()
                                  .toPlainString()
                              : "0.00");
                      return order;
                    })
                .collect(Collectors.toList());
        XLogger.log(
            logger,
            env,
            "同步拼多多订单:[{}~{}],第{}页,{}",
            startTime,
            endTime,
            page,
            JsonUtil.toJson(list));
        if (list.size() > 0) {
          baseMapper.saveOrUpdateList(list);
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
  public Object wxMessage(WxMessageRequest request, Long uid) {
    ProductQueryParam param = new ProductQueryParam();
    param.setKeyword(request.getContent());
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
      PddDdkGoodsPromotionUrlGenerateResponse
              .GoodsPromotionUrlGenerateResponseGoodsPromotionUrlListItem
          item =
              (PddDdkGoodsPromotionUrlGenerateResponse
                      .GoodsPromotionUrlGenerateResponseGoodsPromotionUrlListItem)
                  getUnionUrl(product.getGoods_id(), product.getSearchId(), uid.toString());
      articles.setUrl(
          Optional.ofNullable(item)
              .map(
                  PddDdkGoodsPromotionUrlGenerateResponse
                          .GoodsPromotionUrlGenerateResponseGoodsPromotionUrlListItem
                      ::getMobileShortUrl)
              .orElse(""));
      wxMessage.setArticles(Collections.singletonList(articles));
      return wxMessage;
    }
    return "success";
  }

  @Override
  public CommonPage<HJKJDProduct> listProduct(ProductQueryParam param) {
    try {
      PddDdkGoodsRecommendGetRequest request = new PddDdkGoodsRecommendGetRequest();
      request.setCatId(param.getOptId());
      request.setChannelType(param.getChannelType());
      request.setListId(param.getListId());
      request.setLimit(param.getPageSize().intValue());
      request.setOffset((param.getPageSize().intValue() * param.getPage().intValue()));
      request.setPid(clientProperties.getPid());
      PddDdkGoodsRecommendGetResponse response = getPddClient().syncInvoke(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "拼夕夕商品推荐",
          JsonUtil.toJson(request),
          JsonUtil.toJson(response));

      if (response.getGoodsBasicDetailResponse() != null
          && response.getGoodsBasicDetailResponse().getList() != null
          && response.getGoodsBasicDetailResponse().getList().size() > 0) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            response.getGoodsBasicDetailResponse().getTotal().longValue(),
            response.getGoodsBasicDetailResponse().getList().stream()
                .map(
                    (item) -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getGoodsSign());
                      product.setGoods_name(item.getGoodsName());
                      product.setGoods_desc(item.getGoodsDesc());

                      BigDecimal coupon =
                          new BigDecimal(
                              Optional.ofNullable(item.getCouponDiscount())
                                  .map(Object::toString)
                                  .orElse("0"));
                      BigDecimal price = new BigDecimal(item.getMinGroupPrice().toString());
                      BigDecimal priceEnd =
                          price
                              .subtract(coupon)
                              .divide(new BigDecimal(100), 2, RoundingMode.DOWN); // 券后价
                      BigDecimal commission =
                          priceEnd
                              .multiply(new BigDecimal(item.getPromotionRate()))
                              .divide(new BigDecimal(1000), 2, RoundingMode.DOWN); // 返佣
                      product.setPrice(
                          price
                              .divide(new BigDecimal(100), 2, RoundingMode.DOWN)
                              .stripTrailingZeros()
                              .toPlainString());
                      product.setPrice_after(priceEnd.stripTrailingZeros().toPlainString());
                      product.setDiscount(
                          coupon
                              .divide(new BigDecimal(100), 0, RoundingMode.DOWN)
                              .stripTrailingZeros()
                              .toPlainString());
                      product.setRebate(getRebate(commission));
                      if (StrUtil.equals(env, "dev")) {
                        product.setCommissionshare(
                            new BigDecimal(item.getPromotionRate().toString())
                                .divide(new BigDecimal("10"), 1, RoundingMode.DOWN)
                                .toString());
                        product.setCommission(String.format("%.2f", commission));
                      }
                      product.setPicurl(item.getGoodsThumbnailUrl());
                      product.setSalesTip(
                          String.format(
                              "已售%s件", Optional.ofNullable(item.getSalesTip()).orElse("0")));
                      product.setSource(ProductSource.PDD.getCode());
                      product.setSearchId(item.getSearchId());
                      return product;
                    })
                .collect(Collectors.toList()),
            response.getGoodsBasicDetailResponse().getListId());
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
      PddDdkGoodsSearchRequest request = new PddDdkGoodsSearchRequest();
      request.setPage(param.getPage().intValue());
      request.setPageSize(param.getPageSize().intValue());
      request.setListId(param.getListId());
      request.setKeyword(param.getKeyword());
      if (ObjectUtil.equals(param.getOptId(), 0L)) {
        request.setActivityTags(Collections.singletonList(10564));
      } else if (ObjectUtil.equals(param.getOptId(), -1L)) {
        request.setActivityTags(Collections.singletonList(24));
      } else {
        request.setOptId(param.getOptId());
      }
      request.setSortType(param.getSortType());
      request.setWithCoupon(param.getWithCoupon());
      request.setPid(clientProperties.getPid());
      if (ObjectUtil.isNotEmpty(param.getGoodsIds())) {
        request.setGoodsSignList(
            Arrays.stream(param.getGoodsIds().split(",")).collect(Collectors.toList()));
      }
      PddDdkGoodsSearchResponse searchResponse = getPddClient().syncInvoke(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "拼夕夕商品搜索",
          JsonUtil.toJson(request),
          JsonUtil.toJson(searchResponse));
      if (searchResponse.getGoodsSearchResponse() != null
          && searchResponse.getGoodsSearchResponse().getGoodsList() != null
          && searchResponse.getGoodsSearchResponse().getGoodsList().size() > 0) {
        if (searchResponse.getGoodsSearchResponse().getGoodsList().size() == 1
            && searchResponse.getGoodsSearchResponse().getGoodsList().get(0).getGoodsName()
                == null) {
          return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
        }
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            searchResponse.getGoodsSearchResponse().getTotalCount().longValue(),
            searchResponse.getGoodsSearchResponse().getGoodsList().stream()
                .map(
                    (item) -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getGoodsSign());
                      product.setGoods_name(item.getGoodsName());
                      product.setGoods_desc(item.getGoodsDesc());

                      BigDecimal coupon =
                          new BigDecimal(
                              Optional.ofNullable(item.getCouponDiscount())
                                  .map(Object::toString)
                                  .orElse("0"));
                      BigDecimal price = new BigDecimal(item.getMinGroupPrice().toString());
                      BigDecimal priceEnd =
                          price
                              .subtract(coupon)
                              .divide(new BigDecimal(100), 2, RoundingMode.DOWN); // 券后价
                      BigDecimal commission =
                          priceEnd
                              .multiply(new BigDecimal(item.getPromotionRate()))
                              .divide(new BigDecimal(1000), 2, RoundingMode.DOWN); // 返佣
                      product.setPrice(
                          price
                              .divide(new BigDecimal(100), 2, RoundingMode.DOWN)
                              .stripTrailingZeros()
                              .toPlainString());
                      product.setPrice_after(priceEnd.stripTrailingZeros().toPlainString());
                      product.setDiscount(
                          coupon
                              .divide(new BigDecimal(100), 0, RoundingMode.DOWN)
                              .stripTrailingZeros()
                              .toPlainString());
                      product.setRebate(getRebate(commission));
                      if (StrUtil.equals(env, "dev")) {
                        product.setCommissionshare(
                            new BigDecimal(item.getPromotionRate().toString())
                                .divide(new BigDecimal("10"), 1, RoundingMode.DOWN)
                                .toString());
                        product.setCommission(String.format("%.2f", commission));
                      }
                      product.setPicurl(item.getGoodsThumbnailUrl());
                      product.setSalesTip(
                          String.format(
                              "已售%s件", Optional.ofNullable(item.getSalesTip()).orElse("0")));
                      product.setSource(ProductSource.PDD.getCode());
                      product.setSearchId(item.getSearchId());
                      return product;
                    })
                .collect(Collectors.toList()),
            searchResponse.getGoodsSearchResponse().getListId());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Asserts.fail(e.getLocalizedMessage());
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public HJKJDProduct getProductDetail(String id, String searchId) {
    try {
      PddDdkGoodsDetailRequest request = new PddDdkGoodsDetailRequest();
      request.setGoodsImgType(0);
      request.setGoodsSign(id);
      request.setNeedSkuInfo(false);
      request.setPid(clientProperties.getPid());
      request.setSearchId(searchId);
      PddDdkGoodsDetailResponse response = getPddClient().syncInvoke(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "拼夕夕商品详情",
          JsonUtil.toJson(request),
          JsonUtil.toJson(response));
      if (response.getGoodsDetailResponse() != null
          && ObjectUtil.isNotEmpty(response.getGoodsDetailResponse().getGoodsDetails())) {
        PddDdkGoodsDetailResponse.GoodsDetailResponseGoodsDetailsItem item =
            response.getGoodsDetailResponse().getGoodsDetails().get(0);
        HJKJDProduct product = new HJKJDProduct();
        product.setGoods_id(item.getGoodsSign());
        product.setGoods_name(item.getGoodsName());
        product.setGoods_desc(item.getGoodsDesc());

        BigDecimal coupon =
            new BigDecimal(
                Optional.ofNullable(item.getCouponDiscount()).map(Object::toString).orElse("0"));
        BigDecimal price = new BigDecimal(item.getMinGroupPrice().toString());
        BigDecimal priceEnd =
            price.subtract(coupon).divide(new BigDecimal(100), 2, RoundingMode.DOWN); // 券后价
        BigDecimal commission =
            priceEnd
                .multiply(new BigDecimal(item.getPromotionRate()))
                .divide(new BigDecimal(1000), 2, RoundingMode.DOWN); // 返佣
        product.setPrice(
            price
                .divide(new BigDecimal(100), 2, RoundingMode.DOWN)
                .stripTrailingZeros()
                .toPlainString());
        product.setPrice_after(priceEnd.stripTrailingZeros().toPlainString());
        product.setDiscount(
            coupon
                .divide(new BigDecimal(100), 0, RoundingMode.DOWN)
                .stripTrailingZeros()
                .toPlainString());
        product.setRebate(getRebate(commission));
        if (StrUtil.equals(env, "dev")) {
          product.setCommissionshare(
              new BigDecimal(item.getPromotionRate().toString())
                  .divide(new BigDecimal("10"), 1, RoundingMode.DOWN)
                  .toString());
          product.setCommission(String.format("%.2f", commission));
        }
        product.setPicurl(item.getGoodsThumbnailUrl());
        product.setPicurls(String.join(",", item.getGoodsGalleryUrls()));
        product.setSalesTip(item.getSalesTip());
        product.setSource(ProductSource.PDD.getCode());
        product.setSearchId(searchId);
        product.setShopname(item.getMallName());
        return product;
      }
    } catch (Exception e) {
      e.printStackTrace();
      Asserts.fail(e.getLocalizedMessage());
    }
    return null;
  }

  @Override
  public Object getUnionUrl(String id, String searchId, String uid) {
    try {
      PddDdkGoodsPromotionUrlGenerateRequest request = new PddDdkGoodsPromotionUrlGenerateRequest();
      request.setCustomParameters(
          JsonUtil.toJson(
              new HashMap<String, Object>() {
                {
                  put("uid", clientProperties.getUid());
                  put("sid", uid);
                }
              }));
      request.setGenerateAuthorityUrl(false);
      request.setGenerateMallCollectCoupon(false);
      request.setGenerateQqApp(false);
      request.setGenerateSchemaUrl(false);
      request.setGenerateShortUrl(true);
      request.setGenerateWeApp(true);
      List<String> goodsSignList = new ArrayList<>();
      goodsSignList.add(id);
      request.setGoodsSignList(goodsSignList);
      request.setMultiGroup(false);
      request.setPId(clientProperties.getPid());
      request.setSearchId(searchId);
      request.setGenerateShortLink(false);
      request.setGenerateWeixinCode(true);
      PddDdkGoodsPromotionUrlGenerateResponse response = getPddClient().syncInvoke(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "拼夕夕转链",
          JsonUtil.toJson(request),
          JsonUtil.toJson(response));

      if (response.getGoodsPromotionUrlGenerateResponse() != null
          && ObjectUtil.isNotEmpty(
              response.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList())) {
        return response.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList().get(0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getRebate(BigDecimal commission) {
    if (commission.compareTo(new BigDecimal("0.025")) < 1) {
      return "0";
    }
    return commission
        .multiply(new BigDecimal(clientProperties.getRate()))
        .setScale(2, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString();
  }
}
