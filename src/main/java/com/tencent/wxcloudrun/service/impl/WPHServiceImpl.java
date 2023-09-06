package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.ext.fasterxml.jackson.core.type.TypeReference;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.HJKProperties;
import com.tencent.wxcloudrun.config.properties.WPHProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.*;
import com.tencent.wxcloudrun.enums.OrderStatus;
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.WPHService;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import com.tencent.wxcloudrun.utils.XLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WPHServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements WPHService {
  final Logger logger = LoggerFactory.getLogger(WPHServiceImpl.class);

  @Resource private WPHProperties wphProperties;

  @Resource private HJKProperties hjkProperties;

  @Value("${spring.profiles.active}")
  private String env;

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
    String api = hjkProperties.getApiUrl() + "/vip/getorder";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("pageindex", page);
            put("pagesize", 50);
            put("updateTimeStart", DateUtil.parseDateTime(startTime).getTime());
            put("updateTimeEnd", DateUtil.parseDateTime(endTime).getTime());
            put("chanTag", wphProperties.getPid());
          }
        };

    HJKOrderResponse response =
        RestTemplateUtil.getInstance().postForObject(api, searchParams, HJKOrderResponse.class);
    XLogger.log(
        logger,
        env,
        "Method:[{}],Request:{},Response:{}",
        "SyncWPHOrder",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response != null
        && response.getData() != null
        && response.getData().getOrderInfoList() != null
        && response.getData().getOrderInfoList() instanceof List) {

      List<HJKWPHOrder> orderList =
          JsonUtil.transferToObj(
              JsonUtil.transferToJson(response.getData().getOrderInfoList()),
              new TypeReference<List<HJKWPHOrder>>() {});
      List<OmsOrder> list =
          orderList.stream()
              .map(
                  (item) -> {
                    OmsOrder order = new OmsOrder();
                    order.setOrderSource(ProductSource.WPH.getCode());
                    order.setOrderId(item.getOrderSn());
                    order.setOrderSn(item.getOrderSn());
                    order.setOrderEmt(2);
                    if (ObjectUtil.isNotEmpty(item.getOrderTime())) {
                      order.setOrderTime(
                          LocalDateTime.ofInstant(
                              Instant.ofEpochMilli(item.getOrderTime()), ZoneId.systemDefault()));
                    }
                    if (ObjectUtil.isNotEmpty(item.getSignTime())) {
                      order.setFinishTime(
                          LocalDateTime.ofInstant(
                              Instant.ofEpochMilli(item.getSignTime()), ZoneId.systemDefault()));
                    }
                    if (ObjectUtil.isNotEmpty(item.getLastUpdateTime())) {
                      order.setModifyTime(
                          LocalDateTime.ofInstant(
                              Instant.ofEpochMilli(item.getLastUpdateTime()),
                              ZoneId.systemDefault()));
                    }
                    order.setPid(item.getPid());
                    HJKWPHOrder.DetailListDTO detail = item.getDetailList().get(0);
                    order.setSkuId(detail.getGoodsId());
                    order.setSkuName(detail.getGoodsName());
                    order.setSkuNum(Long.valueOf(detail.getGoodsCount()));
                    order.setImageUrl(detail.getGoodsThumb());
                    order.setPrice(detail.getCommissionTotalCost());
                    order.setCommissionRate(detail.getCommissionRate());
                    order.setActualCosPrice(detail.getCommissionTotalCost());
                    order.setActualFee(detail.getCommission());
                    if (ObjectUtil.equals(item.getOrderSubStatusName(), "已付款")) {
                      order.setStatus(OrderStatus.DELIVER.getCode());
                    } else if (ObjectUtil.equals(item.getOrderSubStatusName(), "已签收")) {
                      order.setStatus(OrderStatus.COMPLETE.getCode());
                    } else if (ObjectUtil.equals(item.getOrderSubStatusName(), "已失效")) {
                      order.setStatus(OrderStatus.INVALID.getCode());
                    }
                    order.setStatusDes(item.getOrderSubStatusName());
                    order.setUid(item.getStatParam());
                    order.setRate(wphProperties.getRate());
                    // 金额小于0.02不算返利
                    order.setRebate(
                        new BigDecimal(detail.getCommission()).compareTo(new BigDecimal("0.02"))
                                >= 1
                            ? new BigDecimal(order.getActualFee())
                                .multiply(new BigDecimal(order.getRate()))
                                .setScale(2, RoundingMode.DOWN)
                                .toString()
                            : "0.00");
                    return order;
                  })
              .collect(Collectors.toList());
      XLogger.log(
          logger,
          env,
          "同步唯品会订单:[{}~{}],第{}页,{}",
          startTime,
          endTime,
          page,
          JsonUtil.transferToJson(list));
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
  }

  @Override
  public Object wxMessage(WxMessageRequest request) {
    String reqContent = URLDecoder.decode(request.getContent(), StandardCharsets.UTF_8);
    String goodsId = reqContent.substring(reqContent.indexOf("goodsId=") + 8).split("&")[0];
    if (ObjectUtil.isNotEmpty(goodsId)) {
      HJKJDProduct product = getProductDetail(goodsId);
      if (product != null) {
        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        wxMessage.setMsgType("text");
        String content = "折扣价:" + product.getPrice_after() + " 约返:" + product.getRebate() + "\n";
        content = content + "◇ " + product.getGoods_name() + "\n";
        content =
            content
                + String.format(
                    "<a data-miniprogram-appid=\"wxd612e795c9823faa\" " +
                            "data-miniprogram-path=\"pages/product/index?id=%s&type=5\">点我马上购买</a>",
                    product.getGoods_id());
        wxMessage.setContent(content);
        return wxMessage;
      }
    }
    return "success";
  }

  @Override
  public CommonPage<HJKJDProduct> listProduct(ProductQueryParam param) {
    String api = hjkProperties.getApiUrl() + "/vip/goodslist";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("pageindex", param.getPage());
            put("pagesize", param.getPageSize());
            put("realCall", true);
            put("chanTag", wphProperties.getPid());
            put("openId", "default_open_id");
          }
        };
    searchParams.put("jxCode", param.getJxCode());
    searchParams.put("sourceType", 1);

    if (param.getSortType() == 9) {
      searchParams.put("fieldName", "PRICE");
      searchParams.put("order", 0);
    } else if (param.getSortType() == 6) {
      searchParams.put("fieldName", "DISCOUNT");
      searchParams.put("order", 1);
    }
    HJKProductListResponse response =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, HJKProductListResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "唯品会商品列表",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (response.getStatus_code() != 200) {
      Asserts.fail(response.getMessage());
    }
    if (response.getData().getGoodsInfoList() instanceof List) {
      List<HJKWPHProduct> list =
          JsonUtil.transferToObj(
              JsonUtil.transferToJson(response.getData().getGoodsInfoList()),
              new TypeReference<List<HJKWPHProduct>>() {});
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          response.getData().getTotal(),
          list.stream()
              .map(
                  item -> {
                    HJKJDProduct product = new HJKJDProduct();
                    product.setGoods_id(item.getGoodsId());
                    product.setGoods_name(item.getGoodsName());
                    product.setGoods_desc(item.getGoodsDesc());

                    BigDecimal marketPrice = new BigDecimal(item.getMarketPrice());
                    BigDecimal price = new BigDecimal(item.getVipPrice());
                    product.setPrice(marketPrice.stripTrailingZeros().toPlainString());
                    product.setPrice_after(price.stripTrailingZeros().toPlainString());
                    if (item.getCouponInfo() == null) {
                      product.setDiscount("0");
                    } else {
                      BigDecimal fav = new BigDecimal(item.getCouponInfo().getFav());
                      product.setDiscount(fav.stripTrailingZeros().toPlainString());
                    }
                    product.setRebate(getRebate(new BigDecimal(item.getCommission())));
                    if (StrUtil.equals(env, "dev")) {
                      product.setCommissionshare(item.getCommissionRate());
                      product.setCommission(item.getCommission());
                    }
                    if (ObjectUtil.notEqual(item.getMarketPrice(), item.getVipPrice())) {
                      product.setDiscountWph(
                          price
                              .divide(marketPrice, 2, RoundingMode.HALF_UP)
                              .multiply(new BigDecimal("10"))
                              .stripTrailingZeros()
                              .toPlainString());
                    }
                    product.setPicurl(item.getGoodsThumbUrl());
                    product.setSalesTip(
                        String.format(
                            "已售%s件", Optional.ofNullable(item.getProductSales()).orElse("0")));
                    product.setSource(ProductSource.WPH.getCode());
                    return product;
                  })
              .collect(Collectors.toList()));
    }
    return CommonPage.page(
        param.getPage(), param.getPageSize(), response.getData().getTotal(), null);
  }

  @Override
  public CommonPage<HJKJDProduct> searchProduct(ProductQueryParam param) {
    String api = hjkProperties.getApiUrl() + "/vip/goodsquery";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("pageindex", param.getPage());
            put("pagesize", param.getPageSize());
            put("keyword", param.getKeyword());
            put("realCall", true);
            put("chanTag", wphProperties.getPid());
            put("openId", "default_open_id");
          }
        };

    if (param.getSortType() == 9) {
      searchParams.put("fieldName", "PRICE");
      searchParams.put("order", 0);
    } else if (param.getSortType() == 6) {
      searchParams.put("fieldName", "DISCOUNT");
      searchParams.put("order", 1);
    }
    HJKWPHProductListResponse response =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, HJKWPHProductListResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "唯品会搜索商品",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (response.getStatus_code() != 200) {
      Asserts.fail(response.getMessage());
    }
    if (response.getData() instanceof Map) {
      List goodInfoList = (List) ((Map<?, ?>) response.getData()).get("goodsInfoList");
      Integer total = (Integer) ((Map<?, ?>) response.getData()).get("total");

      if (ObjectUtil.isNotEmpty(goodInfoList)) {
        List<HJKWPHProduct> list =
            JsonUtil.transferToObj(
                JsonUtil.transferToJson(goodInfoList), new TypeReference<List<HJKWPHProduct>>() {});
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            total.longValue(),
            list.stream()
                .map(
                    item -> {
                      HJKJDProduct product = new HJKJDProduct();
                      product.setGoods_id(item.getGoodsId());
                      product.setGoods_name(item.getGoodsName());
                      product.setGoods_desc(item.getGoodsDesc());

                      BigDecimal marketPrice = new BigDecimal(item.getMarketPrice());
                      BigDecimal price = new BigDecimal(item.getVipPrice());
                      product.setPrice(marketPrice.stripTrailingZeros().toPlainString());
                      product.setPrice_after(price.stripTrailingZeros().toPlainString());
                      if (item.getCouponInfo() == null) {
                        product.setDiscount("0");
                      } else {
                        BigDecimal fav = new BigDecimal(item.getCouponInfo().getFav());
                        product.setDiscount(fav.stripTrailingZeros().toPlainString());
                      }
                      product.setRebate(getRebate(new BigDecimal(item.getCommission())));
                      if (StrUtil.equals(env, "dev")) {
                        product.setCommissionshare(item.getCommissionRate());
                        product.setCommission(item.getCommission());
                      }
                      if (ObjectUtil.notEqual(item.getMarketPrice(), item.getVipPrice())) {
                        product.setDiscountWph(
                            price
                                .divide(marketPrice, 2, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("10"))
                                .stripTrailingZeros()
                                .toPlainString());
                      }
                      product.setPicurl(item.getGoodsThumbUrl());
                      product.setSalesTip(
                          String.format(
                              "已售%s件", Optional.ofNullable(item.getProductSales()).orElse("0")));
                      product.setSource(ProductSource.WPH.getCode());
                      return product;
                    })
                .collect(Collectors.toList()));
      }
    }

    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public HJKJDProduct getProductDetail(String id) {
    // 获取详情
    String api = hjkProperties.getApiUrl() + "/vip/goodsdetail";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("goods_id", Long.valueOf(id));
            put("realCall", true);
            put("chanTag", wphProperties.getPid());
            put("openId", "default_open_id");
          }
        };
    ResponseEntity<HJKWPHProductDetailResponse> response =
        RestTemplateUtil.getInstance()
            .postForEntity(api, searchParams, HJKWPHProductDetailResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "唯品会搜索商品",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response.getBody() != null && response.getBody().getData() != null) {
      List<HJKWPHProduct> list =
          JsonUtil.transferToObj(
              JsonUtil.transferToJson(response.getBody().getData()),
              new TypeReference<List<HJKWPHProduct>>() {});
      HJKWPHProduct item = list.get(0);
      HJKJDProduct product = new HJKJDProduct();
      product.setGoods_id(item.getGoodsId());
      product.setGoods_name(item.getGoodsName());
      product.setGoods_desc(item.getGoodsDesc());

      BigDecimal marketPrice = new BigDecimal(item.getMarketPrice());
      BigDecimal price = new BigDecimal(item.getVipPrice());
      product.setPrice(marketPrice.stripTrailingZeros().toPlainString());
      product.setPrice_after(price.stripTrailingZeros().toPlainString());
      if (item.getCouponInfo() == null) {
        product.setDiscount("0");
      } else {
        BigDecimal fav = new BigDecimal(item.getCouponInfo().getFav());
        product.setDiscount(fav.stripTrailingZeros().toPlainString());
      }
      product.setRebate(getRebate(new BigDecimal(item.getCommission())));
      if (StrUtil.equals(env, "dev")) {
        product.setCommissionshare(item.getCommissionRate());
        product.setCommission(item.getCommission());
      }
      if (ObjectUtil.notEqual(item.getMarketPrice(), item.getVipPrice())) {
        product.setDiscountWph(
            price
                .divide(marketPrice, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("10"))
                .stripTrailingZeros()
                .toPlainString());
      }
      product.setPicurl(item.getGoodsThumbUrl());
      if (ObjectUtil.isEmpty(item.getGoodsCarouselPictures())) {
        product.setPicurls(item.getGoodsThumbUrl());
      } else {
        product.setPicurls(String.join(",", item.getGoodsCarouselPictures()));
      }
      product.setSalesTip(
          String.format("已售%s件", Optional.ofNullable(item.getProductSales()).orElse("0")));
      product.setSource(ProductSource.WPH.getCode());
      return product;
    }
    return null;
  }

  @Override
  public Object getUnionUrl(String id, String uid) {
    // 获取推广链接
    String api = hjkProperties.getApiUrl() + "/vip/getunionurl";
    Map<String, Object> map =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("goods_id", id);
            put("chanTag", wphProperties.getPid());
            put("statParam", uid);
            put("type", 1);
            put("openId", "default_open_id");
            put("realCall", true);
          }
        };
    ResponseEntity<HJKWPHLinkResponse> response =
        RestTemplateUtil.getInstance().postForEntity(api, map, HJKWPHLinkResponse.class);
    if (response.getBody() != null && response.getBody().getData() != null) {
      return response.getBody().getData().getUrlInfoList().get(0);
    }
    return null;
  }

  private String getRebate(BigDecimal commission) {
    if (commission.compareTo(new BigDecimal("0.02")) < 1) {
      return "0";
    }
    return commission
        .multiply(new BigDecimal(wphProperties.getRate()))
        .setScale(2, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString();
  }
}
