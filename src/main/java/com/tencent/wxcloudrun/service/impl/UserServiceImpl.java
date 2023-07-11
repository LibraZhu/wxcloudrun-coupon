package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.StrUtil;
import com.jd.open.api.sdk.DefaultJdClient;
import com.jd.open.api.sdk.JdClient;
import com.jd.open.api.sdk.domain.kplunion.GoodsService.request.query.GoodsReq;
import com.jd.open.api.sdk.request.kplunion.UnionOpenGoodsQueryRequest;
import com.jd.open.api.sdk.response.kplunion.UnionOpenGoodsQueryResponse;
import com.pdd.pop.sdk.common.util.DigestUtil;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsPromotionUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsSearchRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkRpPromUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsPromotionUrlGenerateResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsSearchResponse;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalRequest;
import com.taobao.api.request.TbkSpreadGetRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import com.taobao.api.response.TbkSpreadGetResponse;
import com.tencent.wxcloudrun.config.properties.ClientProperties;
import com.tencent.wxcloudrun.config.properties.JDProperties;
import com.tencent.wxcloudrun.config.properties.TaobaoProperties;
import com.tencent.wxcloudrun.dto.HDKJDProductLinkResponse;
import com.tencent.wxcloudrun.dto.HJKJDProductDetailResponse;
import com.tencent.wxcloudrun.dto.HJKJDProductLinkResponse;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.dto.WxMessage;
import com.tencent.wxcloudrun.service.UserService;
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
import java.net.URLEncoder;
import java.util.*;
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
    public Object userMessage(WxMessageRequest request) {
        if (request != null && StrUtil.equals("text", request.getMsgType())) {
            if (request.getContent().startsWith("https://item.m.jd.com") || request.getContent().startsWith("https://item.jd.com")) {
                return jdMessage(request);
            } else if (request.getContent().contains("yangkeduo.com")) {
                return pddMessage(request);
            }
        } else if (request != null && StrUtil.equals("event", request.getMsgType()) && StrUtil.equals("click", request.getEvent())) {
            // 自定义菜单
        }
        return "success";
    }

    private PopClient getPddClient() {
        return new PopHttpClient(
                clientProperties.getClientId(),
                clientProperties.getClientSecret());
    }

    private Object pddMessage(WxMessageRequest request) {
        PddDdkGoodsSearchRequest searchRequest = new PddDdkGoodsSearchRequest();
        searchRequest.setKeyword(request.getContent());
        searchRequest.setPid(clientProperties.getPid());
        try {
            //同步调用
            PddDdkGoodsSearchResponse searchResponse = getPddClient().syncInvoke(searchRequest);
            if (StrUtil.equals(env, "dev")) {
                logger.info("[{}],Request:{},Response:{}", "PDD商详", JsonUtil.transferToJson(searchRequest), JsonUtil.transferToJson(searchResponse));
            }
            if (searchResponse.getGoodsSearchResponse().getGoodsList() != null && searchResponse.getGoodsSearchResponse().getGoodsList().size() > 0) {
                PddDdkGoodsSearchResponse.GoodsSearchResponseGoodsListItem goodsListItem = searchResponse.getGoodsSearchResponse().getGoodsList().get(0);

                PddDdkGoodsPromotionUrlGenerateRequest urlGenerateRequest = new PddDdkGoodsPromotionUrlGenerateRequest();
                urlGenerateRequest.setGoodsSignList(Collections.singletonList(goodsListItem.getGoodsSign()));
                urlGenerateRequest.setPId(clientProperties.getPid());
                urlGenerateRequest.setSearchId(goodsListItem.getSearchId());
                urlGenerateRequest.setCustomParameters(JsonUtil.transferToJson(new HashMap<String, Object>() {{
                    put("uid", clientProperties.getUid());
                    put("sid", DigestUtil.md5(request.getFromUserName()));
                }}));
                PddDdkGoodsPromotionUrlGenerateResponse urlGenerateResponse = getPddClient().syncInvoke(urlGenerateRequest);
                if (StrUtil.equals(env, "dev")) {
                    logger.info("[{}],Request:{},Response:{}", "PDD转链", JsonUtil.transferToJson(urlGenerateRequest), JsonUtil.transferToJson(urlGenerateResponse));
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
                    BigDecimal coupon = new BigDecimal(goodsListItem.getCouponDiscount());
                    BigDecimal price = new BigDecimal(goodsListItem.getMinGroupPrice());
                    BigDecimal priceEnd = price.subtract(coupon).divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN); // 券后价
                    BigDecimal commission = priceEnd
                            .multiply(new BigDecimal(goodsListItem.getPromotionRate()))
                            .divide(new BigDecimal(1000), 2, RoundingMode.HALF_DOWN); // 返佣
                    BigDecimal rebate = commission.multiply(new BigDecimal(clientProperties.getRate())); // 返利
                    if (StrUtil.equals(env, "dev")) {
                        logger.info("[{}],[{}],[{}],[{}]",
                                "PDD价格" + price.divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN),
                                "券后价" + priceEnd,
                                "返佣" + commission,
                                "返利" + String.format("%.2f", rebate));
                    }
                    articles.setTitle("券后价:" + priceEnd + " 约返:" + String.format("%.2f", rebate));
                    articles.setDescription("【领券下单拿返现】" + goodsListItem.getGoodsName());
                    articles.setPicUrl(goodsListItem.getGoodsThumbnailUrl());
                    articles.setUrl(urlListItem.getMobileUrl());
                    articlesList.add(articles);
                    wxMessage.setArticles(articlesList);
                    if (StrUtil.equals(env, "dev")) {
                        logger.info("[{}],[{}],[{}],[{}],[{}]",
                                "PDD价格" + price.divide(new BigDecimal(100), 2, RoundingMode.HALF_DOWN),
                                "券后价" + priceEnd,
                                "返佣" + commission,
                                "返利" + String.format("%.2f", rebate),
                                urlListItem.getMobileUrl());
                    }
                    return wxMessage;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    private DefaultTaobaoClient getTaobaoClient() {
        return new DefaultTaobaoClient(taobaoProperties.getUrl(), taobaoProperties.getAppKey(), taobaoProperties.getAppSecret());
    }

    private Object taobaoMessage(WxMessageRequest request) {
        try {
            String content = request.getContent();
            if (StrUtil.isNotEmpty(content)) {
                Pattern pattern = Pattern.compile("「(.*?)」");
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
                TbkSpreadGetRequest spreadGetRequest = new TbkSpreadGetRequest();
                List<TbkSpreadGetRequest.TbkSpreadRequest> list2 = new ArrayList<>();
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
        try {
            if (request.getContent().startsWith("https://item.m.jd.com") || request.getContent().startsWith("https://item.jd.com")) {
                String url = request.getContent().split("\\?")[0];
                String[] splits = url.split("/");
                String id = splits[splits.length - 1].replace(".html", "");

                HJKJDProductDetailResponse.HJKJDProductDetail productDetail = getHJKJDProductDetail(id);
                if (productDetail != null) {
                    WxMessage wxMessage = new WxMessage();
                    wxMessage.setFromUserName(request.getToUserName());
                    wxMessage.setToUserName(request.getFromUserName());
                    wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                    wxMessage.setMsgType("news");
                    wxMessage.setArticleCount(1);
                    List<WxMessage.Articles> articlesList = new ArrayList<>();
                    WxMessage.Articles articles = new WxMessage.Articles();
                    BigDecimal rebate = new BigDecimal(productDetail.getCommission()).multiply(new BigDecimal(jdProperties.getRate()));
                    articles.setTitle("券后价:" + productDetail.getPrice_after() + " 约返:" + String.format("%.2f", rebate));
                    articles.setDescription("【领券下单拿返现】" + productDetail.getGoods_name());
                    articles.setPicUrl(productDetail.getPicurl().replace("/jfs", "/s200x200_jfs"));
                    String unionUrl = getHDKUnionUrl(id, productDetail.getCouponurl(), DigestUtil.md5(request.getFromUserName()));
                    if (StrUtil.equals(env, "dev")) {
                        logger.info("[{}],[{}],[{}],[{}],[{}]",
                                "JD价格" + productDetail.getPrice(),
                                "券后价" + productDetail.getPrice_after(),
                                "返佣" + productDetail.getCommission(),
                                "返利" + String.format("%.2f", rebate),
                                unionUrl);
                    }
                    if (unionUrl != null) {
                        articles.setUrl(unionUrl);
                        articlesList.add(articles);
                        wxMessage.setArticles(articlesList);
                        return wxMessage;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    private String getHDKApiKey() {
        return "59623DEAE2F1";
    }

    private String getHJKApiKey() {
        return "2ec2ffab04637dfc";
    }

    /**
     * 好单库 京东推广链接
     *
     * @param id         商品id
     * @param coupon_url 优惠券链接
     * @param subUnionId 子渠道
     * @return
     */
    private String getHDKUnionUrl(String id, String coupon_url, String subUnionId) {
        // 获取推广链接
        String hdkApi = "http://v2.api.haodanku.com/get_jditems_link";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("apikey", getHDKApiKey());
        map.add("material_id", Long.valueOf(id));
        map.add("coupon_url", coupon_url);
        map.add("union_id", jdProperties.getUnionId());
        map.add("pid", jdProperties.getPid());
        map.add("subUnionId", subUnionId);
        System.out.println(subUnionId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, httpHeaders);
        HDKJDProductLinkResponse productLinkResponse = RestTemplateUtil.getInstance().postForObject(hdkApi, param, HDKJDProductLinkResponse.class);
        if (StrUtil.equals(env, "dev")) {
            logger.info("[{}],Request:{},Response:{}", "hjk京东转链", JsonUtil.transferToJson(map), JsonUtil.transferToJson(productLinkResponse));
        }
        if (productLinkResponse != null && productLinkResponse.getData() != null) {
            return productLinkResponse.getData().getShort_url();
        }
        return null;
    }

    /**
     * 蚂蚁星球 京东商详
     *
     * @param id 商品id
     * @return
     */
    private HJKJDProductDetailResponse.HJKJDProductDetail getHJKJDProductDetail(String id) {
        // 获取详情
        String api = "http://api-gw.haojingke.com/index.php/v1/api/jd/goodsdetail";
        Map<String, Object> searchParams = new HashMap<String, Object>() {{
            put("apikey", getHJKApiKey());
            put("goods_id", Long.valueOf(id));
        }};
        ResponseEntity<HJKJDProductDetailResponse> detailResponseEntity = RestTemplateUtil.getInstance().postForEntity(api, searchParams, HJKJDProductDetailResponse.class);
        if (StrUtil.equals(env, "dev")) {
            logger.info("[{}],Request:{},Response:{}", "hjk京东商品详情", JsonUtil.transferToJson(searchParams), JsonUtil.transferToJson(detailResponseEntity));
        }
        if (detailResponseEntity.getBody() != null) {
            return detailResponseEntity.getBody().getData();
        }
        return null;
    }

    /**
     * 蚂蚁星球 京东推广链接
     *
     * @param id 商品id
     * @return
     */
    private String getHJKUnionUrl(String id) {
        // 获取推广链接
        String hdkApi = "http://api-gw.haojingke.com/index.php/v1/api/jd/getunionurl";
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("apikey", getHJKApiKey());
            put("goods_id", Long.valueOf(id));
            put("positionid", jdProperties.getPositionId());
            put("type", 1);
            put("giftCouponKey", "");
        }};
        ResponseEntity<HJKJDProductLinkResponse> productLinkResponse = RestTemplateUtil.getInstance().postForEntity(hdkApi, map, HJKJDProductLinkResponse.class);
        if (StrUtil.equals(env, "dev")) {
            logger.info("[{}],Request:{},Response:{}", "hjk京东转链", JsonUtil.transferToJson(map), JsonUtil.transferToJson(productLinkResponse));
        }
        if (productLinkResponse.getBody() != null && productLinkResponse.getBody().getData() != null) {
            return productLinkResponse.getBody().getData();
        }
        return null;
    }

    @Override
    public Object pddAuthUrl() {
        PddDdkRpPromUrlGenerateRequest request = new PddDdkRpPromUrlGenerateRequest();
        request.setChannelType(10);
        List<String> pIdList = new ArrayList<>();
        pIdList.add(clientProperties.getPid());
        request.setPIdList(pIdList);
        request.setCustomParameters(JsonUtil.transferToJson(new HashMap<String, Object>() {{
            put("uid", clientProperties.getUid());
        }}));
        try {
            //同步调用
            return getPddClient().syncInvoke(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        JdClient client = new DefaultJdClient("https://api.jd.com/routerjson", null, "fd07e7373553e152016b787fba46c182", "eaf58fbf234c406186632d3dcf17771a");
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
