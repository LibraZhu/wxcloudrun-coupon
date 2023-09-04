package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.DYProperties;
import com.tencent.wxcloudrun.config.properties.JTKProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.*;
import com.tencent.wxcloudrun.enums.OrderStatus;
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.DYService;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import com.tencent.wxcloudrun.utils.XLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DYServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements DYService {
  final Logger logger = LoggerFactory.getLogger(DYServiceImpl.class);

  @Resource private DYProperties dyProperties;

  @Resource private JTKProperties jtkProperties;

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
    String api = jtkProperties.getApiUrl() + "/douyin/orders";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", jtkProperties.getApiKey());
            put("query_type", 1);
            put("start_time", startTime);
            put("end_time", endTime);
            put("page", page);
            put("pageSize", 20);
          }
        };

    JTKDYOrderResponse response =
        RestTemplateUtil.getInstance().postForObject(api, searchParams, JTKDYOrderResponse.class);
    XLogger.log(
        logger,
        env,
        "Method:[{}],Request:{},Response:{}",
        "SyncDYOrder",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response == null) {
      return;
    }
    if (ObjectUtil.isNotNull(response.getData())
        && ObjectUtil.isNotEmpty(response.getData().getData())) {
      List<OmsOrder> list =
          response.getData().getData().stream()
              .map(
                  (item) -> {
                    OmsOrder order = new OmsOrder();
                    order.setOrderSource(ProductSource.DY.getCode());
                    order.setOrderId(item.getOrderId());
                    order.setOrderSn(item.getOrderId());
                    order.setOrderEmt(2);
                    if (ObjectUtil.isNotEmpty(item.getPaySuccessTime())) {
                      order.setOrderTime(
                          LocalDateTime.parse(
                              item.getPaySuccessTime(),
                              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    if (ObjectUtil.isNotEmpty(item.getUpdateTime())
                        && ObjectUtil.equals(item.getFlowPoint(), "CONFIRM")) {
                      order.setFinishTime(
                          LocalDateTime.parse(
                              item.getUpdateTime(),
                              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    if (ObjectUtil.isNotEmpty(item.getUpdateTime())) {
                      order.setModifyTime(
                          LocalDateTime.parse(
                              item.getUpdateTime(),
                              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    if (ObjectUtil.isNotEmpty(item.getSettleTime())) {
                      order.setSettleTime(
                          LocalDateTime.parse(
                              item.getSettleTime(),
                              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    order.setSkuId(item.getProductId());
                    order.setSkuName(item.getProductName());
                    order.setSkuNum(Long.valueOf(item.getItemNum()));
                    order.setImageUrl(item.getProductImg());
                    order.setPrice(item.getTotalPayAmount());
                    order.setCommissionRate(item.getJtkShareRate());
                    order.setActualCosPrice(item.getTotalPayAmount());
                    order.setActualFee(item.getJtkShareFee());
                    if (ObjectUtil.equals(item.getFlowPoint(), "PAY_SUCC")) {
                      order.setStatus(OrderStatus.DELIVER.getCode());
                    } else if (ObjectUtil.equals(item.getFlowPoint(), "CONFIRM")) {
                      order.setStatus(OrderStatus.COMPLETE.getCode());
                    } else if (ObjectUtil.equals(item.getFlowPoint(), "REFUND")) {
                      order.setStatus(OrderStatus.INVALID.getCode());
                      order.setStatusDes("退款");
                    }
                    order.setUid(item.getSid());
                    order.setRate(dyProperties.getRate());
                    // 金额小于0.02不算返利
                    order.setRebate(
                        new BigDecimal(item.getJtkShareFee()).compareTo(new BigDecimal("0.02")) >= 1
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
          "同步抖音订单:[{}~{}],第{}页,{}",
          startTime,
          endTime,
          page,
          JsonUtil.transferToJson(list));
      if (list.size() > 0) {
        baseMapper.saveOrUpdateList(list);
      }
    }
    if (response.getData() != null
        && response.getData().getLastPage() > response.getData().getCurrentPage()) {
      // 下一页
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      syncOrderPage(page + 1, startTime, endTime);
    }
  }

  @Override
  public Object wxMessage(WxMessageRequest request) {
    Matcher matcher =
        Pattern.compile(Pattern.quote("【") + "(.*?)" + Pattern.quote("】"))
            .matcher(request.getContent());
    if (matcher.find()) {
      String keyword = matcher.group(1).trim();
      ProductQueryParam param = new ProductQueryParam();
      param.setKeyword(keyword);
      param.setUid("dds");
      CommonPage<HJKJDProduct> page = searchProduct(param);
      if (ObjectUtil.isNotEmpty(page.getList())) {
        HJKJDProduct product = page.getList().get(0);
        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        wxMessage.setMsgType("text");
        String content = "券后价:" + product.getPrice_after() + " 约返:" + product.getRebate() + "\n";
        content = content + "◇ " + product.getGoods_name() + "\n";
        content =
            content
                + String.format(
                    "<a data-miniprogram-appid=\"wxd612e795c9823faa\" "
                        + "data-miniprogram-path=\"pages/product/index?id=%s&type=4\">点我马上购买</a>",
                    product.getGoods_id());
        if (page.getList().size() > 1) {
          content =
              content
                  + "\n\n"
                  + String.format(
                      "<a data-miniprogram-appid=\"wxd612e795c9823faa\" "
                          + "data-miniprogram-path=\"pages/search/list/index?keyword=%s&type=4\">查看相似商品</a>",
                      keyword);
        }
        wxMessage.setContent(content);
        return wxMessage;
      }
      return page.getList();
    }
    return "success";
  }

  @Override
  public CommonPage<HJKJDProduct> rankProduct(ProductQueryParam param) {
    String api = jtkProperties.getApiActUrl() + "/union/jingxuan";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("pub_id", jtkProperties.getPubId());
            put("source", "douyin");
            put("page", param.getPage());
            put("pageSize", param.getPageSize());
            put("type", 1);
          }
        };
    JTKProductListResponse response =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, JTKProductListResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音榜单商品列表",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotEmpty(response.getData())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          0L,
          response.getData().stream().map(this::toProduct).collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public CommonPage<HJKJDProduct> listProduct(ProductQueryParam param) {
    String api = jtkProperties.getApiActUrl() + "/union/query_goods";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("pub_id", jtkProperties.getPubId());
            put("source", "douyin");
            put("page", param.getPage());
            put("pageSize", param.getPageSize());
            put("cat", param.getOptId());
          }
        };
    JTKProductListResponse response =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, JTKProductListResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音商品列表",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotEmpty(response.getData())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          0L,
          response.getData().stream().map(this::toProduct).collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public CommonPage<HJKJDProduct> searchProduct(ProductQueryParam param) {
    String api = jtkProperties.getApiActUrl() + "/union/search";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("pub_id", jtkProperties.getPubId());
            put("source", "douyin");
            put("page", param.getPage());
            put("pageSize", param.getPageSize());
            put("keyword", param.getKeyword());
            put("sid", param.getUid());
          }
        };
    if (param.getSortType() == 6) {
      searchParams.put("sort", 3);
    } else if (param.getSortType() == 9) {
      searchParams.put("sort", 2);
    } else {
      searchParams.put("sort", 1);
    }
    JTKProductListResponse response =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, JTKProductListResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音商品列表",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotEmpty(response.getData())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          0L,
          response.getData().stream().map(this::toProduct).collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public HJKJDProduct getProductDetail(String id, String uid) {
    // 获取详情
    String api = jtkProperties.getApiActUrl() + "/union/convert";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("pub_id", jtkProperties.getPubId());
            put("source", "douyin");
            put("goodsId", id);
            put("sid", uid);
          }
        };
    JTKProductDetailResponse response =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, JTKProductDetailResponse.class);

    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音搜索商品",
        JsonUtil.transferToJson(searchParams),
        JsonUtil.transferToJson(response));
    if (response != null && ObjectUtil.isNotNull(response.getData())) {
      return toProduct(response.getData());
    }
    return null;
  }

  @Override
  public Object getUnionUrl(String id, String uid) {
    // 获取推广链接
    HJKJDProduct product = getProductDetail(id, uid);
    if (product != null) {
      return product.getTkl();
    }
    return null;
  }

  private HJKJDProduct toProduct(JTKProduct item) {
    HJKJDProduct product = new HJKJDProduct();
    product.setGoods_id(item.getGoodsId());
    product.setGoods_name(item.getGoodsName());

    product.setPrice(item.getMarketPrice());
    product.setPrice_after(item.getPrice());
    product.setDiscount(item.getDiscount().toString());
    product.setRebate(getRebate(new BigDecimal(item.getCommission())));
    if (StrUtil.equals(env, "dev")) {
      product.setCommissionshare(item.getCommissionRate());
      product.setCommission(item.getCommission());
    }
    product.setPicurl(item.getGoodsThumbUrl());
    if (ObjectUtil.isNotEmpty(item.getGoodsCarouselPictures())) {
      product.setPicurls(String.join(",", item.getGoodsCarouselPictures()));
      product.setPicurl(item.getGoodsCarouselPictures().get(0));
    }
    product.setSales(item.getSalesTip().longValue());
    if (product.getSales() >= 10000) {
      product.setSalesTip(
          String.format(
              "已售%s万+件",
              new BigDecimal(product.getSales())
                  .divide(new BigDecimal("10000"), 1, RoundingMode.DOWN)
                  .stripTrailingZeros()
                  .toPlainString()));
    } else {
      product.setSalesTip(String.format("已售%s件", product.getSales()));
    }
    product.setTkl(item.getTkl());
    product.setSource(ProductSource.DY.getCode());
    return product;
  }

  private String getRebate(BigDecimal commission) {
    if (commission.compareTo(new BigDecimal("0.02")) < 1) {
      return "0";
    }
    return commission
        .multiply(new BigDecimal(dyProperties.getRate()))
        .setScale(2, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString();
  }
}
