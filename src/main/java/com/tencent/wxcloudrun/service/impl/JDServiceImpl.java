package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.request.query.JFGoodsReq;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.UrlInfo;
import com.jd.open.api.sdk.domain.kplunion.OrderService.request.query.OrderRowReq;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsJingfenQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenOrderRowQueryRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsJingfenQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenOrderRowQueryResponse;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.config.properties.HDKProperties;
import com.tencent.wxcloudrun.config.properties.HJKProperties;
import com.tencent.wxcloudrun.config.properties.JDProperties;
import com.tencent.wxcloudrun.dao.OmsOrderMapper;
import com.tencent.wxcloudrun.dto.*;
import com.tencent.wxcloudrun.enums.OrderStatus;
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.JDService;
import com.tencent.wxcloudrun.utils.JsonUtil;
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

  private final Map<Integer, String> statusMap =
      new HashMap<Integer, String>() {
        {
          put(-1, "未知");
          put(2, "无效-拆单");
          put(3, "无效-取消  ");
          put(4, "无效-京东帮帮主订单");
          put(5, "无效-账号异常");
          put(6, "无效-赠品类目不返佣");
          put(7, "无效-赠品类目不返佣");
          put(8, "无效-企业订单");
          put(9, "无效-团购订单");
          put(11, "无效-乡村推广员下单");
          put(13, "违规订单-其他");
          put(14, "无效-来源与备案网址不符,");
          put(15, "待付款");
          put(19, "无效-佣金比例为0");
          put(20, "无效-此复购订单对应的首购订单无效");
          put(21, "无效-云店订单");
          put(22, "无效-PLUS会员佣金比例为0");
          put(23, "无效-支付有礼");
          put(24, "已付定金");
          put(25, "违规订单-流量劫持");
          put(26, "违规订单-流量异常");
          put(27, "违规订单-违反京东平台规则");
          put(28, "违规订单-多笔交易异常");
          put(29, "无效-跨屏跨店");
          put(30, "无效-累计件数超出类目上限");
          put(31, "无效-黑名单sku");
        }
      };

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
      UnionOpenOrderRowQueryRequest orderRowQueryRequest = new UnionOpenOrderRowQueryRequest();
      OrderRowReq orderReq = new OrderRowReq();
      orderReq.setType(3);
      orderReq.setPageSize(50);
      orderReq.setPageIndex(page);
      orderReq.setStartTime(startTime);
      orderReq.setEndTime(endTime);
      orderReq.setFields("goodsInfo");
      orderRowQueryRequest.setOrderReq(orderReq);
      orderRowQueryRequest.setVersion("1.0");
      UnionOpenOrderRowQueryResponse response = getJDClient().execute(orderRowQueryRequest);
      XLogger.log(
          logger,
          env,
          "Method:[{}],Request:{},Response:{}",
          "SyncJDOrder",
          JsonUtil.toJson(orderReq),
          JsonUtil.toJson(response));
      if (response.getQueryResult() != null
          && ObjectUtil.isNotEmpty(response.getQueryResult().getData())) {
        List<OmsOrder> list =
            Arrays.stream(response.getQueryResult().getData())
                .map(
                    (item) -> {
                      OmsOrder order = new OmsOrder();
                      order.setOrderSource(ProductSource.JD.getCode());
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
                      order.setPrice(
                          new BigDecimal(item.getPrice().toString())
                              .multiply(new BigDecimal(item.getSkuNum()))
                              .toString());
                      order.setCommissionRate(item.getCommissionRate().toString());
                      order.setFinalRate(item.getFinalRate().toString());
                      order.setEstimateCosPrice(item.getEstimateCosPrice().toString());
                      order.setEstimateFee(item.getEstimateFee().toString());
                      order.setActualCosPrice(item.getActualCosPrice().toString());
                      order.setActualFee(item.getActualFee().toString());
                      order.setStatus(item.getValidCode());
                      if (item.getValidCode() == 16) {
                        order.setStatus(OrderStatus.DELIVER.getCode());
                      } else if (item.getValidCode() == 17) {
                        order.setStatus(OrderStatus.COMPLETE.getCode());
                      } else {
                        order.setStatus(OrderStatus.INVALID.getCode());
                        order.setStatusDes(
                            order.getStatus() + "-" + statusMap.get(order.getStatus()));
                      }
                      order.setUid(item.getSubUnionId());
                      order.setRate(jdProperties.getRate());
                      // 先显示预估佣金
                      BigDecimal commission = new BigDecimal(item.getEstimateFee().toString());
                      if (ObjectUtil.equals(item.getActualFee(), 0.0)) {
                        commission = new BigDecimal(item.getActualFee().toString());
                      }
                      // 金额小于0.02不算返利
                      order.setRebate(
                          commission.compareTo(new BigDecimal("0.02")) >= 1
                              ? commission
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
            logger, env, "同步京东订单:[{}~{}],第{}页,{}", startTime, endTime, page, JsonUtil.toJson(list));
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
  public Object wxMessage(WxMessageRequest request, Long uid) {
    try {
      String url = request.getContent().split("\\?")[0];
      String[] splits = url.split("/");
      String id = splits[splits.length - 1].replace(".html", "");

      HJKJDProduct productDetail = getProductDetail(id);
      if (productDetail != null) {
        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        wxMessage.setMsgType("news");
        wxMessage.setArticleCount(1);
        WxMessage.Articles articles = new WxMessage.Articles();
        articles.setTitle(
            "券后价:" + productDetail.getPrice_after() + " 约返:" + productDetail.getRebate());
        articles.setDescription(productDetail.getGoods_name());
        articles.setPicUrl(productDetail.getPicurl().replace("/jfs", "/s200x200_jfs"));
        articles.setUrl(
            getUnionUrl(productDetail.getGoods_id(), productDetail.getCouponurl(), uid.toString()));
        wxMessage.setArticles(Collections.singletonList(articles));
        //        wxMessage.setMsgType("text");
        //        String content =
        //            "券后价:" + productDetail.getPrice_after() + " 约返:" + productDetail.getRebate() +
        // "\n";
        //        content = content + "◇ " + productDetail.getGoods_name() + "\n";
        //        content =
        //            content
        //                + String.format(
        //                    "<a data-miniprogram-appid=\"wxd612e795c9823faa\" "
        //                        +
        // "data-miniprogram-path=\"pages/product/index?id=%s&type=2\">点我马上购买</a>",
        //                    productDetail.getGoods_id());
        //        wxMessage.setContent(content);
        return wxMessage;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "success";
  }

  @Override
  public CommonPage<HJKJDProduct> listProduct(ProductQueryParam param) {
    try {
      UnionOpenGoodsJingfenQueryRequest request = new UnionOpenGoodsJingfenQueryRequest();
      JFGoodsReq goodsReq = new JFGoodsReq();
      goodsReq.setEliteId(param.getOptId().intValue());
      goodsReq.setPageIndex(param.getPage().intValue());
      goodsReq.setPageSize(param.getPageSize().intValue());
      if (param.getSortType() == 5) {
        goodsReq.setSortName("inOrderCount30DaysSku");
        goodsReq.setSort("asc");
      } else if (param.getSortType() == 6) {
        goodsReq.setSortName("inOrderCount30DaysSku");
        goodsReq.setSort("desc");
      } else if (param.getSortType() == 9) {
        goodsReq.setSortName("price");
        goodsReq.setSort("asc");
      } else if (param.getSortType() == 10) {
        goodsReq.setSortName("price");
        goodsReq.setSort("desc");
      }
      goodsReq.setPid(jdProperties.getPid());
      request.setGoodsReq(goodsReq);
      request.setVersion("1.0");
      UnionOpenGoodsJingfenQueryResponse response = getJDClient().execute(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "京东商品列表",
          JsonUtil.toJson(request),
          JsonUtil.toJson(response));
      if (response.getQueryResult().getCode() != 200) {
        Asserts.fail(response.getQueryResult().getMessage());
      }

      if (response.getQueryResult().getData() != null) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            response.getQueryResult().getTotalCount(),
            Arrays.stream(response.getQueryResult().getData())
                .map(
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
                      if (item.getInOrderCount30DaysSku() >= 10000) {
                        product.setSalesTip(
                            String.format(
                                "月售%s万+件",
                                new BigDecimal(item.getInOrderCount30DaysSku())
                                    .divide(new BigDecimal("10000"), 1, RoundingMode.DOWN)
                                    .stripTrailingZeros()
                                    .toPlainString()));
                      } else {
                        product.setSalesTip(
                            String.format("月售%s件", item.getInOrderCount30DaysSku()));
                      }
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
                              item.getCommissionInfo().getPlusCommissionShare() == null
                                  ? null
                                  : item.getCommissionInfo().getPlusCommissionShare().toString());
                        }
                        product.setRebate(
                            getRebate(
                                item.getCommissionInfo().getCouponCommission().toString(),
                                item.getCommissionInfo().getCommissionShare().toString(),
                                item.getCommissionInfo().getPlusCommissionShare() == null
                                    ? null
                                    : item.getCommissionInfo()
                                        .getPlusCommissionShare()
                                        .toString()));
                      }
                      product.setOwner(item.getOwner());
                      product.setSource(ProductSource.JD.getCode());
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
    String api = hjkProperties.getApiUrl() + "/jd/goodslist";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("pageindex", param.getPage());
            put("pagesize", param.getPageSize());
            put("keyword", param.getKeyword());
            put("goods_ids", param.getGoodsIds());
          }
        };

    if (param.getSortType() == 5) {
      searchParams.put("sortname", "4");
      searchParams.put("sort", "asc");
    } else if (param.getSortType() == 6) {
      searchParams.put("sortname", "4");
      searchParams.put("sort", "desc");
    } else if (param.getSortType() == 9) {
      searchParams.put("sortname", "1");
      searchParams.put("sort", "asc");
    } else if (param.getSortType() == 10) {
      searchParams.put("sortname", "1");
      searchParams.put("sort", "desc");
    }
    HJKProductListResponse hjkjdProductListResponse =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, HJKProductListResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "hjk京东商品列表",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(hjkjdProductListResponse));
    if (hjkjdProductListResponse == null) {
      Asserts.fail("服务器异常");
    }
    if (hjkjdProductListResponse.getStatus_code() != 200) {
      Asserts.fail(hjkjdProductListResponse.getMessage());
    }
    if (hjkjdProductListResponse.getData().getData() instanceof List) {
      List<HJKJDProduct> list =
          JsonUtil.toList(
              JsonUtil.toJson(hjkjdProductListResponse.getData().getData()),
              new TypeReference<List<HJKJDProduct>>() {});
      // 计算返利
      list.forEach(
          product -> {
            product.setRebate(
                getRebate(
                    product.getCommission(),
                    product.getCommissionshare(),
                    product.getPlusCommissionShare()));
            product.setSource(ProductSource.JD.getCode());
            if (product.getSales() >= 10000) {
              product.setSalesTip(
                  String.format(
                      "月售%s万+件",
                      new BigDecimal(product.getSales())
                          .divide(new BigDecimal("10000"), 1, RoundingMode.DOWN)
                          .stripTrailingZeros()
                          .toPlainString()));
            } else {
              product.setSalesTip(String.format("月售%s件", product.getSales()));
            }
          });
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          hjkjdProductListResponse.getData().getTotal(),
          list);
    } else {
      return CommonPage.page(
          param.getPage(),
          param.getPageSize(),
          hjkjdProductListResponse.getData().getTotal(),
          null);
    }
  }

  @Override
  public HJKJDProduct getProductDetail(String id) {
    // 获取详情
    String api = hjkProperties.getApiUrl() + "/jd/goodsdetail";
    Map<String, Object> searchParams =
        new HashMap<String, Object>() {
          {
            put("apikey", hjkProperties.getApiKey());
            put("goods_id", Long.valueOf(id));
          }
        };
    HJKJDProductDetailResponse response =
        RestTemplateUtil.getInstance()
            .postForObject(api, searchParams, HJKJDProductDetailResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "hjk京东商品详情",
        JsonUtil.toJson(searchParams),
        JsonUtil.toJson(response));
    if (response != null && response.getData() instanceof Map) {
      HJKJDProduct product =
          JsonUtil.toObj(JsonUtil.toJson(response.getData()), HJKJDProduct.class);
      // 计算返利
      product.setRebate(
          getRebate(
              product.getCommission(),
              product.getCommissionshare(),
              product.getPlusCommissionShare()));
      product.setSource(ProductSource.JD.getCode());
      if (product.getSales() >= 10000) {
        product.setSalesTip(
            String.format(
                "月售%s万+件",
                new BigDecimal(product.getSales())
                    .divide(new BigDecimal("10000"), 1, RoundingMode.DOWN)
                    .stripTrailingZeros()
                    .toPlainString()));
      } else {
        product.setSalesTip(String.format("月售%s件", product.getSales()));
      }
      return product;
    }
    return null;
  }

  @Override
  public String getUnionUrl(String id, String coupon_url, String uid) {
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
    map.add("subUnionId", uid);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, httpHeaders);
    HDKJDProductLinkResponse productLinkResponse =
        RestTemplateUtil.getInstance().postForObject(hdkApi, param, HDKJDProductLinkResponse.class);
    XLogger.log(
        logger,
        env,
        "[{}],Request:{},Response:{}",
        "hdk京东转链",
        JsonUtil.toJson(map),
        JsonUtil.toJson(productLinkResponse));
    if (productLinkResponse != null && productLinkResponse.getData() != null) {
      return productLinkResponse.getData().getShort_url();
    }
    return null;
  }

  /**
   * 获取返利金额
   *
   * @param commission 预估佣金
   * @param commissionShare 佣金比例
   * @param plusCommissionShare plus佣金比例，plus用户购买推广者能获取到的佣金比例
   * @return 返利金额
   */
  private String getRebate(String commission, String commissionShare, String plusCommissionShare) {
    if (new BigDecimal(commission).compareTo(new BigDecimal("0.02")) < 1) {
      return "0";
    }
    BigDecimal rebate = new BigDecimal(commission).multiply(new BigDecimal(jdProperties.getRate()));
    if (ObjectUtil.isNotEmpty(plusCommissionShare)) {
      rebate =
          rebate.multiply(
              new BigDecimal(plusCommissionShare)
                  .divide(new BigDecimal(commissionShare), 2, RoundingMode.DOWN));
    }
    return rebate.setScale(2, RoundingMode.DOWN).stripTrailingZeros().toPlainString();
  }
}
