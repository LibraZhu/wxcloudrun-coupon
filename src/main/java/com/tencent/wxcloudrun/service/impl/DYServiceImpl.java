package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.tencent.wxcloudrun.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    String api =
        "https://openapiv2.dataoke.com/open-api/tiktok/order-list"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&order_type={order_type}&date_type={date_type}&start_time={start_time}&end_time={end_time}&page={page}&pageSize={pageSize}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("order_type", 1);
            put("date_type", 4);
            put("start_time", startTime);
            put("end_time", endTime);
            put("page", page);
            put("pageSize", 50);
          }
        };

    DTKDYOrderResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYOrderResponse.class, DTKUtil.sign(searchParams));
    XLogger.log(
        logger,
        env,
        "Method:[{}],Request:{},Response:{}",
        "SyncDYOrder",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response == null || response.getData() == null) {
      return;
    }
    DTKDYOrderResponse.DataDTO dataDTO = response.getData();
    if (ObjectUtil.isNotEmpty(dataDTO.getList())) {
      List<OmsOrder> list =
          dataDTO.getList().stream()
              .map(
                  item -> {
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
                    order.setPrice(item.getTotalPayAmount().toString());
                    order.setCommissionRate(item.getCommissionRate().toString());
                    order.setEstimateCosPrice(item.getPayGoodsAmount().toString());
                    order.setEstimateFee(item.getEstimatedTotalCommission().toString());
                    order.setActualCosPrice(item.getSettledGoodsAmount().toString());
                    order.setActualFee(item.getRealCommission().toString());
                    if (ObjectUtil.equals(item.getFlowPoint(), "PAY_SUCC")) {
                      order.setStatus(OrderStatus.DELIVER.getCode());
                    } else if (ObjectUtil.equals(item.getFlowPoint(), "CONFIRM")) {
                      order.setStatus(OrderStatus.COMPLETE.getCode());
                    } else if (ObjectUtil.equals(item.getFlowPoint(), "REFUND")) {
                      order.setStatus(OrderStatus.INVALID.getCode());
                      order.setStatusDes("退款");
                    }
                    String[] infos = item.getExternalInfo().split("_");
                    if (infos.length > 2) {
                      order.setUid(infos[1]);
                    } else {
                      order.setUid(item.getExternalInfo());
                    }
                    // 一分购的订单不计算返利
                    if (order.getUid().startsWith("F")) {
                      order.setOrderSource(ProductSource.ONE.getCode());
                      order.setUid(order.getUid().substring(1));
                    } else {
                      order.setRate(dyProperties.getRate());
                      // 先显示预估佣金
                      BigDecimal commission =
                          new BigDecimal(item.getEstimatedTotalCommission().toString());
                      if (ObjectUtil.equals(item.getFlowPoint(), "SETTLE")) {
                        commission = new BigDecimal(item.getRealCommission().toString());
                      }
                      // 金额小于0.02不算返利
                      order.setRebate(
                          commission.compareTo(new BigDecimal("0.025")) >= 1
                              ? commission
                                  .multiply(new BigDecimal(order.getRate()))
                                  .setScale(2, RoundingMode.DOWN)
                                  .toString()
                              : "0.00");
                    }
                    return order;
                  })
              .collect(Collectors.toList());
      XLogger.log(
          logger, env, "同步抖音订单:[{}~{}],第{}页,{}", startTime, endTime, page, JsonUtil.toJson(list));
      if (list.size() > 0) {
        baseMapper.saveOrUpdateList(list);
      }
    }
    if (dataDTO.getPage() * 50 < dataDTO.getTotal()) {
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
  public Object wxMessage(WxMessageRequest request, Long uid) {
    ProductQueryParam param = new ProductQueryParam();
    param.setKeyword(request.getContent());
    param.setUid(RequestHolder.getUid());
    CommonPage<HJKJDProduct> page = searchProduct(param);

    WxMessage wxMessage = new WxMessage();
    wxMessage.setFromUserName(request.getToUserName());
    wxMessage.setToUserName(request.getFromUserName());
    wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
    if (ObjectUtil.isEmpty(page.getList())) {
      wxMessage.setMsgType("text");
      wxMessage.setContent("很遗憾，没有找到该商品的优惠");
    }
    Optional<HJKJDProduct> p =
        page.getList().stream()
            .filter(item -> request.getContent().contains(item.getGoods_name()))
            .findFirst();
    if (p.isPresent()) {
      HJKJDProduct product = p.get();
      wxMessage.setMsgType("news");
      wxMessage.setArticleCount(1);
      WxMessage.Articles articles = new WxMessage.Articles();
      articles.setTitle("券后价:" + product.getPrice_after() + " 约返:" + product.getRebate());
      articles.setDescription(product.getGoods_name());
      articles.setPicUrl(product.getPicurl());
      if (page.getList().size() > 1) {
        articles.setUrl(
            "https://prod-2glx9khga5692d1f-1314654459.tcloudbaseapp.com/#/search/list?type=4&uid="
                + uid
                + "&keyword="
                + product.getGoods_name());
      } else {
        articles.setUrl(
            "https://prod-2glx9khga5692d1f-1314654459.tcloudbaseapp.com/#/product/detail?type=4&uid="
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

  @Override
  public CommonPage<HJKJDProduct> oneFenProduct(ProductQueryParam param) {
    String api =
        "https://openapi.dataoke.com/api/tiktok/tiktok-one-fen-goods-list"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&page={page}&size={size}&search_type={search_type}&sort_type={sort_type}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("page", param.getPage());
            put("size", param.getPageSize());
          }
        };
    if (param.getSortType() == 5) {
      searchParams.put("search_type", 1);
      searchParams.put("sort_type", 0);
    } else if (param.getSortType() == 6) {
      searchParams.put("search_type", 1);
      searchParams.put("sort_type", 1);
    } else if (param.getSortType() == 9) {
      searchParams.put("search_type", 2);
      searchParams.put("sort_type", 0);
    } else if (param.getSortType() == 10) {
      searchParams.put("search_type", 2);
      searchParams.put("sort_type", 1);
    } else {
      searchParams.put("search_type", 0);
      searchParams.put("sort_type", 1);
    }
    DTKDYProductListResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYProductListResponse.class, DTKUtil.sign(searchParams));
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音一分购商品列表",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotNull(response.getData())
        && ObjectUtil.isNotEmpty(response.getData().getList())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          response.getData().getTotal(),
          response.getData().getList().stream()
              .map(this::toFenProduct)
              .collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public CommonPage<HJKJDProduct> oneYuanProduct(ProductQueryParam param) {
    String api =
        "https://openapi.dataoke.com/api/tiktok/tiktok-one-yuan-goods-list"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&page={page}&size={size}&search_type={search_type}&sort_type={sort_type}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("page", param.getPage());
            put("size", param.getPageSize());
          }
        };
    if (param.getSortType() == 5) {
      searchParams.put("search_type", 1);
      searchParams.put("sort_type", 0);
    } else if (param.getSortType() == 6) {
      searchParams.put("search_type", 1);
      searchParams.put("sort_type", 1);
    } else if (param.getSortType() == 9) {
      searchParams.put("search_type", 2);
      searchParams.put("sort_type", 0);
    } else if (param.getSortType() == 10) {
      searchParams.put("search_type", 2);
      searchParams.put("sort_type", 1);
    } else {
      searchParams.put("search_type", 0);
      searchParams.put("sort_type", 1);
    }
    DTKDYProductListResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYProductListResponse.class, DTKUtil.sign(searchParams));
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音一元购商品列表",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotNull(response.getData())
        && ObjectUtil.isNotEmpty(response.getData().getList())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          response.getData().getTotal(),
          response.getData().getList().stream()
              .map(this::toFenProduct)
              .collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public CommonPage<HJKJDProduct> rankProduct(ProductQueryParam param) {
    String api =
        "https://openapi.dataoke.com/api/tiktok/tiktok-sx-goods-rank"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&sort_key={sort_key}&sort={sort}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("sort_key", "comprehensive_score");
            put("sort", 1);
          }
        };
    DTKDYProductListResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYProductListResponse.class, DTKUtil.sign(searchParams));
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音榜单商品列表",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotNull(response.getData())
        && ObjectUtil.isNotEmpty(response.getData().getList())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          response.getData().getTotal(),
          response.getData().getList().stream().map(this::toProduct).collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public CommonPage<HJKJDProduct> listProduct(ProductQueryParam param) {
    String api =
        "https://openapi.dataoke.com/api/tiktok/tiktok-sx-goods-list"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&page={page}&size={size}&first_cate_ids={first_cate_ids}&sort={sort}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("page", param.getPage());
            put("size", param.getPageSize());
            put("first_cate_ids", param.getOptId());
          }
        };
    if (param.getSortType() == 6) {
      searchParams.put("sort", 6);
    } else if (param.getSortType() == 9) {
      searchParams.put("sort", 11);
    } else {
      searchParams.put("sort", 0);
    }
    DTKDYProductListResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYProductListResponse.class, DTKUtil.sign(searchParams));
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音商品列表",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotNull(response.getData())
        && ObjectUtil.isNotEmpty(response.getData().getList())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          response.getData().getTotal(),
          response.getData().getList().stream().map(this::toProduct).collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public CommonPage<HJKJDProduct> searchProduct(ProductQueryParam param) {
    String api =
        "https://openapiv2.dataoke.com/tiktok/tiktok-materials-products-search"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&sortType={sortType}&sort={sort}&title={title}&page={page}&pageSize={pageSize}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("page", param.getPage());
            put("pageSize", param.getPageSize());
            put("title", param.getKeyword());
          }
        };
    if (param.getSortType() == 6) {
      searchParams.put("sort", 1);
      searchParams.put("sortType", 1);
    } else if (param.getSortType() == 9) {
      searchParams.put("sort", 2);
      searchParams.put("sortType", 0);
    } else {
      searchParams.put("sort", 0);
      searchParams.put("sortType", 1);
    }
    DTKDYProductListResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYProductListResponse.class, DTKUtil.sign(searchParams));
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音商品列表",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response == null) {
      Asserts.fail("服务器异常");
    }
    if (ObjectUtil.isNotNull(response.getData())
        && ObjectUtil.isNotEmpty(response.getData().getList())) {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          response.getData().getTotal(),
          response.getData().getList().stream().map(this::toProduct).collect(Collectors.toList()));
    }
    return CommonPage.page(param.getPage(), param.getPageSize(), 0L, null);
  }

  @Override
  public HJKJDProduct getProductDetail(String id, String uid) {
    // 获取详情
    String api =
        "https://openapiv2.dataoke.com/tiktok/tiktok-materials-products-details"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&productIds={productIds}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("productIds", id);
          }
        };
    DTKDYProductListResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYProductListResponse.class, DTKUtil.sign(searchParams));

    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音商品详情",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response != null
        && ObjectUtil.isNotNull(response.getData())
        && ObjectUtil.isNotEmpty(response.getData().getList())) {
      return toProduct(response.getData().getList().get(0));
    }
    return null;
  }

  @Override
  public Object getUnionUrl(String id, String uid) {
    String api =
        "https://openapiv2.dataoke.com/open-api/tiktok-kol-product-share"
            + "?appKey={appKey}&nonce={nonce}&timer={timer}&version={version}&signRan={signRan}"
            + "&productUrl={productUrl}&externalInfo={externalInfo}";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("productUrl", id);
            put("externalInfo", uid);
          }
        };
    DTKDYProductLinkResponse response =
        RestTemplateUtil.getInstance()
            .getForObject(api, DTKDYProductLinkResponse.class, DTKUtil.sign(searchParams));

    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "抖音商品转链",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response != null && ObjectUtil.isNotEmpty(response.getData())) {
      return response.getData();
    }
    return null;
  }

  private HJKJDProduct toProduct(DTKDYProduct item) {
    HJKJDProduct product = new HJKJDProduct();
    product.setGoods_id(item.getProductId());
    product.setGoods_name(item.getTitle());
    product.setGoods_url(item.getDetailUrl());

    product.setPrice(item.getPrice());
    if (ObjectUtil.isNull(item.getCouponPrice()) || ObjectUtil.equals(item.getCouponPrice(), "0")) {
      product.setPrice_after(item.getPrice());
      product.setDiscount("0");
    } else {
      product.setPrice_after(item.getCouponPrice());
      product.setDiscount(
          new BigDecimal(item.getPrice())
              .subtract(new BigDecimal(item.getCouponPrice()))
              .stripTrailingZeros()
              .toPlainString());
    }
    product.setRebate(
        getRebate(
            new BigDecimal(
                Optional.ofNullable(
                        Optional.ofNullable(item.getCosFee()).orElse(item.getKolCosFee()))
                    .map(Object::toString)
                    .orElse("0"))));
    if (StrUtil.equals(env, "dev")) {
      product.setCommissionshare(
          Optional.ofNullable(Optional.ofNullable(item.getCosRatio()).orElse(item.getKolCosRatio()))
              .map(Object::toString)
              .orElse("0"));
      product.setCommission(
          Optional.ofNullable(Optional.ofNullable(item.getCosFee()).orElse(item.getKolCosFee()))
              .map(Object::toString)
              .orElse("0"));
    }
    product.setPicurl(item.getCover());
    if (ObjectUtil.isNotEmpty(item.getImgs())) {
      product.setPicurls(String.join(",", item.getImgs()));
    }
    product.setSales(item.getSales().longValue());
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
    product.setShopname(item.getShopName());
    product.setSource(ProductSource.DY.getCode());
    return product;
  }

  private HJKJDProduct toFenProduct(DTKDYProduct item) {
    HJKJDProduct product = new HJKJDProduct();
    product.setIs_fen(true);
    product.setGoods_id(item.getProductId());
    product.setGoods_name(item.getTitle());
    product.setGoods_url(item.getDetailUrl());

    product.setPrice(item.getPrice());
    if (ObjectUtil.isNotNull(item.getNewUserPrice())) {
      product.setPrice_after(item.getNewUserPrice());
      product.setDiscount("0");
    } else if (ObjectUtil.isNull(item.getCouponPrice())
        || ObjectUtil.equals(item.getCouponPrice(), "0")) {
      product.setPrice_after(item.getPrice());
      product.setDiscount("0");
    } else {
      product.setPrice_after(item.getCouponPrice());
      product.setDiscount(
          new BigDecimal(item.getPrice())
              .subtract(new BigDecimal(item.getCouponPrice()))
              .stripTrailingZeros()
              .toPlainString());
    }
    product.setPicurl(item.getCover());
    if (ObjectUtil.isNotEmpty(item.getImgs())) {
      product.setPicurls(String.join(",", item.getImgs()));
    }
    product.setSales(item.getSales().longValue());
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
    product.setShopname(item.getShopName());
    product.setSource(ProductSource.DY.getCode());
    return product;
  }

  private String getRebate(BigDecimal commission) {
    if (commission.compareTo(new BigDecimal("0.025")) < 1) {
      return "0";
    }
    return commission
        .multiply(new BigDecimal(dyProperties.getRate()))
        .setScale(2, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString();
  }
}
