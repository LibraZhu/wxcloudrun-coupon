package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.request.query.GoodsReq;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.response.query.GoodsResp;
import com.jd.open.api.sdk.domain.kplunion.promotionbysubunioni.PromotionService.request.get.PromotionCodeReq;
import com.jd.open.api.sdk.domain.kplunion.promotionbysubunioni.PromotionService.response.get.PromotionCodeResp;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsQueryRequest;
import com.jd.open.api.sdk.request.kplunion.UnionOpenPromotionBysubunionidGetRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsQueryResponse;
import com.jd.open.api.sdk.response.kplunion.UnionOpenPromotionBysubunionidGetResponse;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsPromotionUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsSearchRequest;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsPromotionUrlGenerateResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsSearchResponse;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import com.tencent.wxcloudrun.config.properties.ClientProperties;
import com.tencent.wxcloudrun.config.properties.JDProperties;
import com.tencent.wxcloudrun.config.properties.TaobaoProperties;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.WxMessage;
import com.tencent.wxcloudrun.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private ClientProperties clientProperties;
    @Resource
    private TaobaoProperties taobaoProperties;
    @Resource
    private JDProperties jdProperties;

    @Value("${spring.profiles.active}")
    private String env;

    @Override
    public Object userMessage(WxMessageRequest request) {
        if (request != null && StrUtil.equals("text", request.getMsgType())) {
            if (request.getContent().contains("yangkeduo.com")) {
                return pddMessage(request);
            } else {
                return taobaoMessage(request);
            }
        }
        return "";
    }

    private PopClient getPddClient() {
        return new PopHttpClient(
                clientProperties.getClientId(),
                clientProperties.getClientSecret());
    }

    private Object pddMessage(WxMessageRequest request) {
        PddDdkGoodsSearchRequest searchRequest = new PddDdkGoodsSearchRequest();
        searchRequest.setKeyword(request.getContent());
        searchRequest.setActivityTags(Collections.singletonList(24));//24-品牌高佣
        searchRequest.setPid(clientProperties.getPid());
        try {
            //同步调用
            PddDdkGoodsSearchResponse searchResponse = getPddClient().syncInvoke(searchRequest);
            if (StrUtil.equals(env, "dev")) {
                logger.info("Method:[{}],Request:{},Response:{}", "PddDdkGoodsSearch", JsonUtil.transferToJson(searchRequest), JsonUtil.transferToJson(searchResponse));
            }
            if (searchResponse.getGoodsSearchResponse().getGoodsList() != null && searchResponse.getGoodsSearchResponse().getGoodsList().size() > 0) {
                PddDdkGoodsSearchResponse.GoodsSearchResponseGoodsListItem goodsListItem = searchResponse.getGoodsSearchResponse().getGoodsList().get(0);

                PddDdkGoodsPromotionUrlGenerateRequest urlGenerateRequest = new PddDdkGoodsPromotionUrlGenerateRequest();
                urlGenerateRequest.setGoodsSignList(Collections.singletonList(goodsListItem.getGoodsSign()));
                urlGenerateRequest.setPId(clientProperties.getPid());
                urlGenerateRequest.setSearchId(goodsListItem.getSearchId());
                PddDdkGoodsPromotionUrlGenerateResponse urlGenerateResponse = getPddClient().syncInvoke(urlGenerateRequest);
                if (StrUtil.equals(env, "dev")) {
                    logger.info("Method:[{}],Request:{},Response:{}", "PddDdkGoodsPromotionUrlGenerate", JsonUtil.transferToJson(urlGenerateRequest), JsonUtil.transferToJson(urlGenerateResponse));
                }
                if (urlGenerateResponse.getGoodsPromotionUrlGenerateResponse() != null &&
                        urlGenerateResponse.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList() != null &&
                        urlGenerateResponse.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList().size() > 0) {
                    PddDdkGoodsPromotionUrlGenerateResponse.GoodsPromotionUrlGenerateResponseGoodsPromotionUrlListItem urlListItem =
                            urlGenerateResponse.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList().get(0);

                    WxMessage wxMessage = new WxMessage();
                    wxMessage.setFromUserName(request.getToUserName());
                    wxMessage.setToUserName(request.getFromUserName());
                    wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                    wxMessage.setMsgType("news");
                    wxMessage.setArticleCount(1);
                    List<WxMessage.Articles> articlesList = new ArrayList<>();
                    WxMessage.Articles articles = new WxMessage.Articles();
                    if (StrUtil.isEmpty(goodsListItem.getGoodsName())) {
                        articles.setTitle("优惠券：0");
                    } else {
                        BigDecimal coupon = new BigDecimal(goodsListItem.getCouponDiscount());
                        BigDecimal price = new BigDecimal(goodsListItem.getMinGroupPrice());
                        String couponStr = coupon.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).toString();
                        String newPriceStr = price.subtract(coupon).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).toString();
                        articles.setTitle("优惠券:" + couponStr + " 券后价:" + newPriceStr);
                    }
                    if (StrUtil.isEmpty(goodsListItem.getGoodsName())) {
                        articles.setDescription("点击直接购买");
                    } else {
                        articles.setDescription(goodsListItem.getGoodsDesc());
                    }
                    articles.setPicUrl(goodsListItem.getGoodsThumbnailUrl());
                    articles.setUrl(urlListItem.getMobileUrl());
                    articlesList.add(articles);
                    wxMessage.setArticles(articlesList);
                    return wxMessage;
                } else {

                }
            } else {
                // 没有搜索到
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private DefaultTaobaoClient getTaobaoClient() {
        return new DefaultTaobaoClient(taobaoProperties.getUrl(), taobaoProperties.getAppKey(), taobaoProperties.getAppSecret());
    }

    private Object taobaoMessage(WxMessageRequest request) {
        try {
            TaobaoClient client = getTaobaoClient();
            String content = request.getContent();
            TbkDgMaterialOptionalRequest req = new TbkDgMaterialOptionalRequest();
            req.setAdzoneId(taobaoProperties.getPid());
            req.setQ(request.getContent());
            req.setSort("tk_rate_des");
            TbkDgMaterialOptionalResponse rsp = client.execute(req);
            if (StrUtil.equals(env, "dev")) {
                logger.info("Method:[{}],Request:{},Response:{}", "Taobao Search", JsonUtil.transferToJson(req), JsonUtil.transferToJson(rsp));
            }
            if (rsp.getResultList() != null && rsp.getResultList().size() > 0) {
                TbkDgMaterialOptionalResponse.MapData data = rsp.getResultList().get(0);

                WxMessage wxMessage = new WxMessage();
                wxMessage.setFromUserName(request.getToUserName());
                wxMessage.setToUserName(request.getFromUserName());
                wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                wxMessage.setMsgType("news");
                wxMessage.setArticleCount(1);
                List<WxMessage.Articles> articlesList = new ArrayList<>();
                WxMessage.Articles articles = new WxMessage.Articles();
                if (StrUtil.isEmpty(data.getTitle())) {
                    articles.setTitle("优惠券：0");
                } else {
                    articles.setTitle("优惠券:" + data.getCouponAmount() + " 券后价:" + new BigDecimal(data.getZkFinalPrice()).subtract(new BigDecimal(data.getCouponAmount())));
                }
                if (StrUtil.isEmpty(data.getTitle())) {
                    articles.setDescription("点击直接购买");
                } else {
                    articles.setDescription(data.getTitle());
                }
                articles.setPicUrl(data.getPictUrl());
                articles.setUrl(data.getUrl().startsWith("//") ? ("https:" + data.getUrl()) : data.getUrl());
                articlesList.add(articles);
                wxMessage.setArticles(articlesList);

//                RestTemplate restTemplate = new RestTemplate();
//                String url = "http://api.weixin.qq.com/cgi-bin/message/custom/send";
//                WxSendMessage wxSendMessage = new WxSendMessage();
//                wxSendMessage.setToUser(request.getFromUserName());
//                wxSendMessage.setMsgType("link");
//                WxSendMessage.Link link = new WxSendMessage.Link();
//                if (StrUtil.isEmpty(data.getTitle())) {
//                    link.setTitle("优惠券：0");
//                } else {
//                    link.setTitle("优惠券:" + data.getCouponAmount() + " 券后价:" + new BigDecimal(data.getZkFinalPrice()).subtract(new BigDecimal(data.getCouponAmount())));
//                }
//                if (StrUtil.isEmpty(data.getTitle())) {
//                    link.setDescription("点击直接购买");
//                } else {
//                    link.setDescription(data.getTitle());
//                }
//                link.setThumbUrl(data.getPictUrl());
//                link.setUrl(data.getUrl().startsWith("//") ? ("https:" + data.getUrl()) : data.getUrl());
//                wxSendMessage.setLink(link);
//                Object result = restTemplate.postForObject(url, wxSendMessage, Object.class);
//                logger.info("发送消息:[{}],Request:{},Response:{}", "cgi-bin/message/custom/send", JsonUtil.transferToJson(wxSendMessage), JsonUtil.transferToJson(result));
                return wxMessage;
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return "";
    }

    private DefaultJdClient getJDClient() {
        return new DefaultJdClient(jdProperties.getServerUrl(), null, jdProperties.getAppKey(), jdProperties.getAppSecret());
    }

    private Object jdMessage(WxMessageRequest request) {
        UnionOpenGoodsQueryRequest queryRequest = new UnionOpenGoodsQueryRequest();
        GoodsReq goodsReqDTO = new GoodsReq();
        goodsReqDTO.setSortName("commission");
        goodsReqDTO.setPid(jdProperties.getPid());
        goodsReqDTO.setKeyword(request.getContent());
        queryRequest.setGoodsReqDTO(goodsReqDTO);
        queryRequest.setVersion("1.0");
        try {
            JdClient jdClient = getJDClient();
            UnionOpenGoodsQueryResponse queryResponse = jdClient.execute(queryRequest);
            if (StrUtil.equals(env, "dev")) {
                logger.info("Method:[{}],Request:{},Response:{}", "JDGoodsSearch", JsonUtil.transferToJson(queryRequest), JsonUtil.transferToJson(queryResponse));
            }
            if (queryResponse.getQueryResult() != null &&
                    queryResponse.getQueryResult().getCode() == 200 &&
                    queryResponse.getQueryResult().getData() != null &&
                    queryResponse.getQueryResult().getData().length > 0) {
                GoodsResp goodsResp = queryResponse.getQueryResult().getData()[0];

                // 获取推广链接
                UnionOpenPromotionBysubunionidGetRequest bysubunionidGetRequest = new UnionOpenPromotionBysubunionidGetRequest();
                PromotionCodeReq promotionCodeReq = new PromotionCodeReq();
                promotionCodeReq.setMaterialId(goodsResp.getMaterialUrl());
                promotionCodeReq.setPositionId(jdProperties.getPositionId());
                bysubunionidGetRequest.setPromotionCodeReq(promotionCodeReq);
                bysubunionidGetRequest.setVersion("1.0");
                UnionOpenPromotionBysubunionidGetResponse bysubunionidGetResponse = jdClient.execute(bysubunionidGetRequest);
                if (StrUtil.equals(env, "dev")) {
                    logger.info("Method:[{}],Request:{},Response:{}", "JDPromotionBysubunionid", JsonUtil.transferToJson(bysubunionidGetRequest), JsonUtil.transferToJson(bysubunionidGetResponse));
                }

                if (bysubunionidGetResponse.getGetResult() != null &&
                        bysubunionidGetResponse.getGetResult().getCode() == 200 &&
                        bysubunionidGetResponse.getGetResult().getData() != null) {
                    PromotionCodeResp promotionCodeResp = bysubunionidGetResponse.getGetResult().getData();
                    WxMessage wxMessage = new WxMessage();
                    wxMessage.setFromUserName(request.getToUserName());
                    wxMessage.setToUserName(request.getFromUserName());
                    wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                    wxMessage.setMsgType("news");
                    wxMessage.setArticleCount(1);
                    List<WxMessage.Articles> articlesList = new ArrayList<>();
                    WxMessage.Articles articles = new WxMessage.Articles();
                    if (StrUtil.isEmpty(goodsResp.getSkuName())) {
                        articles.setTitle("优惠券：0");
                    } else {
                        articles.setTitle("优惠券:" + (goodsResp.getPriceInfo().getPrice() - goodsResp.getPriceInfo().getLowestCouponPrice()) + " 券后价:" + goodsResp.getPriceInfo().getLowestCouponPrice());
                    }
                    if (StrUtil.isEmpty(goodsResp.getSkuName())) {
                        articles.setDescription("点击直接购买");
                    } else {
                        articles.setDescription(goodsResp.getSkuName());
                    }
                    if (goodsResp.getImageInfo() != null &&
                            goodsResp.getImageInfo().getImageList() != null &&
                            goodsResp.getImageInfo().getImageList().length > 0) {
                        String imageUrl = goodsResp.getImageInfo().getImageList()[0].getUrl();
                        String[] splits = imageUrl.split("/ads");
                        imageUrl = splits[0] + "/ads/s200x200_jfs" + splits[1];
                        articles.setPicUrl(imageUrl);
                    }
                    articles.setUrl(promotionCodeResp.getClickURL());
                    articlesList.add(articles);
                    wxMessage.setArticles(articlesList);
                    return wxMessage;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
