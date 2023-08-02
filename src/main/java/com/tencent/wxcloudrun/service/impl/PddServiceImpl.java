package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.common.util.DigestUtil;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsPromotionUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsSearchRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkOrderListIncrementGetRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkRpPromUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsPromotionUrlGenerateResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsSearchResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkOrderListIncrementGetResponse;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.ClientProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.dto.PddProductQueryParam;
import com.tencent.wxcloudrun.dto.WxMessage;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.enums.OrderSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.PddService;
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
        JsonUtil.transferToJson(
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
      PddDdkOrderListIncrementGetRequest request = new PddDdkOrderListIncrementGetRequest();
      request.setCashGiftOrder(false);
      request.setStartUpdateTime(DateUtil.parseDateTime(startTime).getTime() / 1000);
      request.setEndUpdateTime(DateUtil.parseDateTime(endTime).getTime() / 1000);
      request.setPage(page);
      request.setPageSize(50);
      request.setReturnCount(false);
      PddDdkOrderListIncrementGetResponse response = getPddClient().syncInvoke(request);
      if (StrUtil.equals(env, "dev")) {
        logger.info(
            "Method:[{}],Request:{},Response:{}",
            "SyncPddOrder",
            JsonUtil.transferToJson(request),
            JsonUtil.transferToJson(response));
      }
      if (response.getOrderListGetResponse() != null
          && response.getOrderListGetResponse().getOrderList() != null) {
        List<OmsOrder> list =
            response.getOrderListGetResponse().getOrderList().stream()
                .flatMap(
                    (item) -> {
                      OmsOrder order = new OmsOrder();
                      order.setOrderSource(OrderSource.PDD.getCode());
                      order.setOrderId(item.getOrderId());
                      order.setOrderSn(item.getOrderSn());
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
                      order.setCompared(item.getPriceCompareStatus());
                      order.setPid(item.getPId());
                      order.setSkuId(item.getGoodsId().toString());
                      order.setSkuName(item.getGoodsName());
                      order.setSkuNum(item.getGoodsQuantity());
                      order.setImageUrl(item.getGoodsThumbnailUrl());
                      order.setPrice(
                          new BigDecimal(item.getGoodsPrice().toString())
                              .divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN)
                              .toString());
                      order.setCommissionRate(
                          new BigDecimal(item.getPromotionRate().toString())
                              .divide(new BigDecimal(10), 1, RoundingMode.HALF_DOWN)
                              .toString());
                      order.setActualCosPrice(
                          new BigDecimal(item.getOrderAmount().toString())
                              .divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN)
                              .toString());
                      order.setActualFee(
                          new BigDecimal(item.getPromotionAmount().toString())
                              .divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN)
                              .toString());
                      order.setStatus(item.getOrderStatus());
                      try {
                        JSONObject customParameters = JSONUtil.parseObj(item.getCustomParameters());
                        order.setUid(customParameters.getStr("sid"));
                      } catch (Exception ee) {
                        ee.printStackTrace();
                      }
                      return Arrays.stream(new OmsOrder[] {order});
                    })
                .collect(Collectors.toList());
        logger.info(
            "同步订单:[{}~{}],第{}页,{}", startTime, endTime, page, JsonUtil.transferToJson(list));
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
  public Object wxMessage(WxMessageRequest request) {
    PddDdkGoodsSearchRequest searchRequest = new PddDdkGoodsSearchRequest();
    searchRequest.setKeyword(request.getContent());
    searchRequest.setPid(clientProperties.getPid());
    try {
      // 同步调用
      PddDdkGoodsSearchResponse searchResponse = getPddClient().syncInvoke(searchRequest);
      if (StrUtil.equals(env, "dev")) {
        logger.info(
            "[{}],Request:{},Response:{}",
            "PDD商详",
            JsonUtil.transferToJson(searchRequest),
            JsonUtil.transferToJson(searchResponse));
      }
      if (searchResponse.getGoodsSearchResponse().getGoodsList() != null
          && searchResponse.getGoodsSearchResponse().getGoodsList().size() > 0) {
        PddDdkGoodsSearchResponse.GoodsSearchResponseGoodsListItem goodsListItem =
            searchResponse.getGoodsSearchResponse().getGoodsList().get(0);

        PddDdkGoodsPromotionUrlGenerateRequest urlGenerateRequest =
            new PddDdkGoodsPromotionUrlGenerateRequest();
        urlGenerateRequest.setGoodsSignList(
            Collections.singletonList(goodsListItem.getGoodsSign()));
        urlGenerateRequest.setPId(clientProperties.getPid());
        urlGenerateRequest.setSearchId(goodsListItem.getSearchId());
        urlGenerateRequest.setCustomParameters(
            JsonUtil.transferToJson(
                new HashMap<String, Object>() {
                  {
                    put("uid", clientProperties.getUid());
                    put("sid", DigestUtil.md5(request.getFromUserName()));
                  }
                }));
        PddDdkGoodsPromotionUrlGenerateResponse urlGenerateResponse =
            getPddClient().syncInvoke(urlGenerateRequest);
        if (StrUtil.equals(env, "dev")) {
          logger.info(
              "[{}],Request:{},Response:{}",
              "PDD转链",
              JsonUtil.transferToJson(urlGenerateRequest),
              JsonUtil.transferToJson(urlGenerateResponse));
        }
        if (urlGenerateResponse.getGoodsPromotionUrlGenerateResponse() != null
            && urlGenerateResponse.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList()
                != null
            && urlGenerateResponse
                    .getGoodsPromotionUrlGenerateResponse()
                    .getGoodsPromotionUrlList()
                    .size()
                > 0) {
          PddDdkGoodsPromotionUrlGenerateResponse
                  .GoodsPromotionUrlGenerateResponseGoodsPromotionUrlListItem
              urlListItem =
                  urlGenerateResponse
                      .getGoodsPromotionUrlGenerateResponse()
                      .getGoodsPromotionUrlList()
                      .get(0);

          WxMessage wxMessage = new WxMessage();
          wxMessage.setFromUserName(request.getToUserName());
          wxMessage.setToUserName(request.getFromUserName());
          wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
          wxMessage.setMsgType("news");
          List<WxMessage.Articles> articlesList = new ArrayList<>();
          if (goodsListItem.getGoodsName() != null) {
            BigDecimal coupon = new BigDecimal(goodsListItem.getCouponDiscount().toString());
            BigDecimal price = new BigDecimal(goodsListItem.getMinGroupPrice().toString());
            BigDecimal priceEnd =
                price
                    .subtract(coupon)
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN); // 券后价
            BigDecimal commission =
                priceEnd
                    .multiply(new BigDecimal(goodsListItem.getPromotionRate()))
                    .divide(new BigDecimal(1000), 2, RoundingMode.HALF_DOWN); // 返佣
            BigDecimal rebate =
                commission.multiply(new BigDecimal(clientProperties.getRate())); // 返利
            if (StrUtil.equals(env, "dev")) {
              logger.info(
                  "[{}],[{}],[{}],[{}],[{}]",
                  "PDD价格" + price.divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN),
                  "券后价" + priceEnd,
                  "返佣" + commission,
                  "返利" + String.format("%.2f", rebate),
                  urlListItem.getMobileUrl());
            }
            WxMessage.Articles articles = new WxMessage.Articles();
            articles.setTitle("券后价:" + priceEnd + " 约返:" + String.format("%.2f", rebate));
            articles.setDescription("【领券下单拿返现】" + goodsListItem.getGoodsName());
            articles.setPicUrl(goodsListItem.getGoodsThumbnailUrl());
            articles.setUrl(urlListItem.getMobileUrl());
            articlesList.add(articles);
          } else {
            WxMessage.Articles articles = new WxMessage.Articles();
            articles.setTitle("约返:0");
            articles.setDescription("【点击下单】");
            articles.setUrl(urlListItem.getMobileUrl());
            articlesList.add(articles);
          }
          wxMessage.setArticles(articlesList);
          wxMessage.setArticleCount(articlesList.size());
          return wxMessage;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "success";
  }

  @Override
  public CommonPage<HJKJDProduct> searchProduct(PddProductQueryParam param) {
    try {
      PddDdkGoodsSearchRequest request = new PddDdkGoodsSearchRequest();
      request.setPage(param.getPage().intValue());
      request.setPageSize(param.getPageSize().intValue());
      request.setListId(param.getListId());
      request.setKeyword(param.getKeyword());
      request.setOptId(param.getOptId());
      request.setSortType(param.getSortType());
      request.setWithCoupon(param.getWithCoupon());
      request.setPid(clientProperties.getPid());
      PddDdkGoodsSearchResponse searchResponse = getPddClient().syncInvoke(request);
      if (searchResponse.getGoodsSearchResponse() != null
          && searchResponse.getGoodsSearchResponse().getGoodsList() != null
          && searchResponse.getGoodsSearchResponse().getGoodsList().size() > 0) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            searchResponse.getGoodsSearchResponse().getTotalCount().longValue(),
            searchResponse.getGoodsSearchResponse().getGoodsList().stream()
                .flatMap(
                    (item) -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getGoodsSign());
                      product.setGoods_name(item.getGoodsName());
                      product.setGoods_desc(item.getGoodsDesc());

                      BigDecimal coupon = new BigDecimal(item.getCouponDiscount().toString());
                      BigDecimal price = new BigDecimal(item.getMinGroupPrice().toString());
                      BigDecimal priceEnd =
                          price
                              .subtract(coupon)
                              .divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN); // 券后价
                      BigDecimal commission =
                          priceEnd
                              .multiply(new BigDecimal(item.getPromotionRate()))
                              .divide(new BigDecimal(1000), 2, RoundingMode.HALF_DOWN); // 返佣
                      BigDecimal rebate =
                          commission.multiply(new BigDecimal(clientProperties.getRate())); // 返利
                      product.setPrice(
                          price
                              .divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN)
                              .stripTrailingZeros()
                              .toPlainString());
                      product.setPrice_after(priceEnd.stripTrailingZeros().toPlainString());
                      product.setDiscount(
                          coupon
                              .divide(new BigDecimal(100), 0, RoundingMode.HALF_DOWN)
                              .stripTrailingZeros()
                              .toPlainString());
                      product.setRebate(String.format("%.2f", rebate));
                      if (StrUtil.equals(env, "dev")) {
                        product.setCommissionshare(
                            new BigDecimal(item.getPromotionRate().toString())
                                .divide(new BigDecimal("10"), 1, RoundingMode.HALF_DOWN)
                                .toString());
                        product.setCommission(String.format("%.2f", commission));
                      }
                      product.setPicurl(item.getGoodsThumbnailUrl());
                      product.setSalesTip(item.getSalesTip());
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
}
