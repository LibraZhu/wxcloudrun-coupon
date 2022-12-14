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
import com.taobao.api.request.TbkSpreadGetRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import com.taobao.api.response.TbkSpreadGetResponse;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.config.properties.ClientProperties;
import com.tencent.wxcloudrun.config.properties.JDProperties;
import com.tencent.wxcloudrun.config.properties.TaobaoProperties;
import com.tencent.wxcloudrun.dto.WxMenuRequest;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.dto.WxResponse;
import com.tencent.wxcloudrun.model.WxMessage;
import com.tencent.wxcloudrun.model.WxSendMessage;
import com.tencent.wxcloudrun.service.UserService;
import com.tencent.wxcloudrun.utils.RestTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public CommonResult userCreateMenu(WxMenuRequest request) {
        String url = "http://api.weixin.qq.com/cgi-bin/menu/create";
        WxResponse result = RestTemplateUtil.getInstance().postForObject(url, request, WxResponse.class);
        logger.info("????????????:[{}],Request:{},Response:{}", "cgi-bin/message/custom/send", JsonUtil.transferToJson(request), JsonUtil.transferToJson(result));
        if (result != null && result.getErrcode() == 0) {
            return CommonResult.success();
        } else {
            return CommonResult.failed(result != null ? result.getErrmsg() : "error");
        }
    }

    @Override
    public Object userMessage(WxMessageRequest request) {
        if (request != null && StrUtil.equals("text", request.getMsgType())) {
            if (request.getContent().contains("yangkeduo.com")) {
                return pddMessage(request);
            } else {
                return taobaoMessage(request);
            }
        } else if (request != null && StrUtil.equals("event", request.getMsgType()) && StrUtil.equals("click", request.getEvent())) {
            String eventKey = request.getEventKey();
            if (StrUtil.equals("coupon", eventKey)) {
                WxMessage wxMessage = new WxMessage();
                wxMessage.setFromUserName(request.getToUserName());
                wxMessage.setToUserName(request.getFromUserName());
                wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                wxMessage.setMsgType("text");
                wxMessage.setContent("mo-[??????]?????????????????????????????????????????????????????????????????????\n" +
                        "\n" +
                        "<a href=\"https://springboot-q6l6-14929-5-1314654459.sh.run.tcloudbase.com\">?????????????????????</a>");
                return wxMessage;
            } else if (StrUtil.equals("mt", eventKey)) {
                WxMessage wxMessage = new WxMessage();
                wxMessage.setFromUserName(request.getToUserName());
                wxMessage.setToUserName(request.getFromUserName());
                wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                wxMessage.setMsgType("text");
                wxMessage.setContent("??????mo-[??????]?????????????????????????????????????????????????????????????????????\n" +
                        "<a href=\"http://dpurl.cn/3UaVygJz\">??????????????????????????????</a>");
                return wxMessage;
            } else if (StrUtil.equals("ele", eventKey)) {
                WxMessage wxMessage = new WxMessage();
                wxMessage.setFromUserName(request.getToUserName());
                wxMessage.setToUserName(request.getFromUserName());
                wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                wxMessage.setMsgType("text");
                wxMessage.setContent("?????????mo-[??????]?????????????????????????????????????????????????????????????????????\n" +
                        "<a href=\"https://s.click.ele.me/miWyrPu\">?????????????????????????????????</a>");
                return wxMessage;
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
        searchRequest.setActivityTags(Collections.singletonList(24));//24-????????????
        searchRequest.setPid(clientProperties.getPid());
        try {
            //????????????
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
                        articles.setTitle("????????????0");
                    } else {
                        BigDecimal coupon = new BigDecimal(goodsListItem.getCouponDiscount());
                        BigDecimal price = new BigDecimal(goodsListItem.getMinGroupPrice());
                        String couponStr = coupon.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).toString();
                        String newPriceStr = price.subtract(coupon).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP).toString();
                        articles.setTitle("?????????:" + couponStr + " ?????????:" + newPriceStr);
                    }
                    if (StrUtil.isEmpty(goodsListItem.getGoodsName())) {
                        articles.setDescription("??????????????????");
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
                // ???????????????
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
            String content = request.getContent();
            if (StrUtil.isNotEmpty(content)) {
                Pattern pattern = Pattern.compile("???(.*?)???");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    content = matcher.group(1);
                }
            }
            TaobaoClient client = getTaobaoClient();
            TbkDgMaterialOptionalRequest req = new TbkDgMaterialOptionalRequest();
            req.setAdzoneId(taobaoProperties.getPid());
            req.setQ(content);
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
                    articles.setTitle("????????????0");
                } else {
                    articles.setTitle("?????????:" + data.getCouponAmount() + " ?????????:" + new BigDecimal(data.getZkFinalPrice()).subtract(new BigDecimal(data.getCouponAmount())));
                }
                if (StrUtil.isEmpty(data.getTitle())) {
                    articles.setDescription("??????????????????");
                } else {
                    articles.setDescription(data.getTitle());
                }
                articles.setPicUrl(data.getPictUrl());
                articles.setUrl(data.getUrl().startsWith("//") ? ("https:" + data.getUrl()) : data.getUrl());
                TbkSpreadGetRequest spreadGetRequest = new TbkSpreadGetRequest();
                List<TbkSpreadGetRequest.TbkSpreadRequest> list2 = new ArrayList<TbkSpreadGetRequest.TbkSpreadRequest>();
                TbkSpreadGetRequest.TbkSpreadRequest obj3 = new TbkSpreadGetRequest.TbkSpreadRequest();
                list2.add(obj3);
                obj3.setUrl(articles.getUrl());
                spreadGetRequest.setRequests(list2);
                TbkSpreadGetResponse spreadGetResponse = client.execute(spreadGetRequest);
                if (spreadGetResponse.getResults() != null && spreadGetResponse.getResults().size() > 0) {
                    TbkSpreadGetResponse.TbkSpread spread = spreadGetResponse.getResults().get(0);
                    if (StrUtil.equals(env, "dev")) {
                        logger.info("Method:[{}],Request:{},Response:{}", "Taobao Spread", JsonUtil.transferToJson(spreadGetRequest), JsonUtil.transferToJson(spreadGetResponse));
                    }
                    articles.setUrl("https://springboot-q6l6-14929-5-1314654459.sh.run.tcloudbase.com" + "?url=" + URLEncoder.encode(spread.getContent()));
                }
                articlesList.add(articles);
                wxMessage.setArticles(articlesList);
//                RestTemplate restTemplate = new RestTemplate();
//                String url = "http://api.weixin.qq.com/cgi-bin/message/custom/send";
//                WxSendMessage wxSendMessage = new WxSendMessage();
//                wxSendMessage.setToUser(request.getFromUserName());
//                wxSendMessage.setMsgType("link");
//                WxSendMessage.Link link = new WxSendMessage.Link();
//                if (StrUtil.isEmpty(data.getTitle())) {
//                    link.setTitle("????????????0");
//                } else {
//                    link.setTitle("?????????:" + data.getCouponAmount() + " ?????????:" + new BigDecimal(data.getZkFinalPrice()).subtract(new BigDecimal(data.getCouponAmount())));
//                }
//                if (StrUtil.isEmpty(data.getTitle())) {
//                    link.setDescription("??????????????????");
//                } else {
//                    link.setDescription(data.getTitle());
//                }
//                link.setThumbUrl(data.getPictUrl());
//                link.setUrl(data.getUrl().startsWith("//") ? ("https:" + data.getUrl()) : data.getUrl());
//                wxSendMessage.setLink(link);
//                Object result = restTemplate.postForObject(url, wxSendMessage, Object.class);
//                logger.info("????????????:[{}],Request:{},Response:{}", "cgi-bin/message/custom/send", JsonUtil.transferToJson(wxSendMessage), JsonUtil.transferToJson(result));
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

                // ??????????????????
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
                        articles.setTitle("????????????0");
                    } else {
                        articles.setTitle("?????????:" + (goodsResp.getPriceInfo().getPrice() - goodsResp.getPriceInfo().getLowestCouponPrice()) + " ?????????:" + goodsResp.getPriceInfo().getLowestCouponPrice());
                    }
                    if (StrUtil.isEmpty(goodsResp.getSkuName())) {
                        articles.setDescription("??????????????????");
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


    private void sendWxMessage(WxSendMessage wxSendMessage) {
        String url = "http://api.weixin.qq.com/cgi-bin/message/custom/send";
        Object result = RestTemplateUtil.getInstance().postForObject(url, wxSendMessage, Object.class);
        logger.info("????????????:[{}],Request:{},Response:{}", "cgi-bin/message/custom/send", JsonUtil.transferToJson(wxSendMessage), JsonUtil.transferToJson(result));
    }
}
