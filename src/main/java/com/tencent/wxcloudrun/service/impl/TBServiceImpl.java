package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.common.util.JsonUtil;
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
import com.tencent.wxcloudrun.enums.ProductSource;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.service.TBService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TBServiceImpl extends ServiceImpl<OmsOrderMapper, OmsOrder> implements TBService {

  final Logger logger = LoggerFactory.getLogger(TBServiceImpl.class);

  @Resource private TBProperties taobaoProperties;

  @Resource private HJKProperties hjkProperties;

  @Value("${spring.profiles.active}")
  private String env;

  private DefaultTaobaoClient getTBClient() {
    return new DefaultTaobaoClient(
        taobaoProperties.getUrl(), taobaoProperties.getAppKey(), taobaoProperties.getAppSecret());
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
    syncOrderPage(1, startTime, oneHourEndTime, null);
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
      ResponseEntity<HJKOrderResponse> responseEntity =
          RestTemplateUtil.getInstance().postForEntity(api, map, HJKOrderResponse.class);
      if (StrUtil.equals(env, "dev")) {
        logger.info(
            "Method:[{}],Request:{},Response:{}",
            "SyncPDDOrder",
            JsonUtil.transferToJson(map),
            JsonUtil.transferToJson(responseEntity));
      }
      if (responseEntity.getBody() != null
          && responseEntity.getBody().getData() != null
          && responseEntity.getBody().getData().getResults() != null
          && responseEntity.getBody().getData().getResults().getPublisher_order_dto() != null) {

        if (ObjectUtil.equals(responseEntity.getBody().getData().getHas_next(), "true")) {
          // 是否还有更多
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          syncOrderPage(
              page + 1, startTime, endTime, responseEntity.getBody().getData().getPosition_index());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Object wxMessage(WxMessageRequest request) {
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
          wxMessage.setMsgType("text");
          String content = "券后价:" + product.getPrice_after() + " 约返:" + product.getRebate() + "\n";
          content = content + "◇ " + product.getGoods_name() + "\n";
          content =
              content
                  + String.format(
                      "<a data-miniprogram-appid=\"wxd612e795c9823faa\" "
                          + "data-miniprogram-path=\"pages/product/index?id=%s&type=3\">点我马上购买</a>",
                      product.getGoods_id());
          if (page.getList().size() > 1) {
            content =
                content
                    + "\n\n"
                    + String.format(
                        "<a data-miniprogram-appid=\"wxd612e795c9823faa\" "
                            + "data-miniprogram-path=\"pages/search/list/index?keyword=%s&type=3\">查看相似商品</a>",
                        keyword);
          }
          wxMessage.setContent(content);
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
        wxMessage.setMsgType("text");
        String content = "券后价:" + product.getPrice_after() + " 约返:" + product.getRebate() + "\n";
        content = content + "◇ " + product.getGoods_name() + "\n";
        content =
            content
                + String.format(
                    "<a data-miniprogram-appid=\"wxd612e795c9823faa\" "
                        + "data-miniprogram-path=\"pages/product/index?id=%s&type=3\">点我马上购买</a>",
                    product.getGoods_id());
        wxMessage.setContent(content);
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
      request.setAdzoneId(taobaoProperties.getPid());
      TbkDgOptimusMaterialResponse response = getTBClient().execute(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "淘宝商品列表",
          JsonUtil.transferToJson(request),
          JsonUtil.transferToJson(response));
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
      request.setAdzoneId(taobaoProperties.getPid());
      request.setPageResultKey(param.getListId());
      TbkDgMaterialOptionalResponse response = getTBClient().execute(request);
      XLogger.log(
          logger,
          env,
          "[{}],Request:{},Response:{}",
          "淘宝商品搜索",
          JsonUtil.transferToJson(request),
          JsonUtil.transferToJson(response));
      if (response.getResultList() != null) {
        return CommonPage.page(
            param.getPage(),
            param.getPageSize(),
            response.getTotalResults(),
            response.getResultList().stream()
                .flatMap(
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
                      product.setSource(ProductSource.TB.getCode());
                      return Arrays.stream(new HJKJDProduct[] {product});
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
        JsonUtil.transferToJson(map),
        JsonUtil.transferToJson(response));
    if (response != null && response.getData() != null) {
      HJKTBLinkResponse.TbLink item = response.getData();
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
            put("pid", taobaoProperties.getXpid());
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
        JsonUtil.transferToJson(map),
        JsonUtil.transferToJson(response));
    if (response != null && response.getData() != null) {
      return response.getData().getTkl();
    }
    return null;
  }

  @Override
  public HJKTBInviterResponse.TbMapData getUidBySpecialId(String specialId) {
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
    ResponseEntity<HJKTBInviterResponse> response =
        RestTemplateUtil.getInstance().postForEntity(api, map, HJKTBInviterResponse.class);
    XLogger.log(
        logger,
        env,
        "Method:[{}],Request:{},Response:{}",
        "根据会员id获取uid",
        JsonUtil.transferToJson(map),
        JsonUtil.transferToJson(response.getBody()));
    if (response.getBody() != null
        && response.getBody().getData() != null
        && response.getBody().getData().getInviter_list() != null) {
      return response.getBody().getData().getInviter_list().getMap_data();
    }

    return null;
  }

  private String getRebate(BigDecimal commission) {
    if (commission.compareTo(new BigDecimal("0.02")) < 1) {
      return "0";
    }
    return commission
        .multiply(new BigDecimal(taobaoProperties.getRate()))
        .setScale(2, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString();
  }
}
