package com.tencent.wxcloudrun.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsPromotionUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkGoodsSearchRequest;
import com.pdd.pop.sdk.http.api.pop.request.PddDdkRpPromUrlGenerateRequest;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsPromotionUrlGenerateResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkGoodsSearchResponse;
import com.pdd.pop.sdk.http.api.pop.response.PddDdkRpPromUrlGenerateResponse;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.WxMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * counter控制器
 */
@Api(tags = "UserMessageController", description = "商品优选管理")
@RestController
public class UserMessageController {

    final Logger logger;
    final String clientId = "5ffc31b3d3ef472faaa23ed35081a998";
    final String clientSecret = "940114100d31bff5ab16b4bbb19ec2ad89b78693";
    final String pid = "35772384_257791152";

    @Value("${spring.profiles.active}")
    private String env;

    public UserMessageController() {
        this.logger = LoggerFactory.getLogger(UserMessageController.class);
    }

    public PopClient getClient() {
        return new PopHttpClient(
                clientId,
                clientSecret);
    }

    /**
     * 消息推送
     *
     * @param request {@link WxMessageRequest}
     * @return API response json
     */
    @ApiOperation("微信公众号消息处理")
    @PostMapping(value = "/user/message")
    public Object userMessage(@RequestBody WxMessageRequest request) {
        if (request != null && StrUtil.equals("text", request.getMsgType())) {
            PddDdkGoodsSearchRequest pddDdkGoodsSearchRequest = new PddDdkGoodsSearchRequest();
            pddDdkGoodsSearchRequest.setKeyword(request.getContent());
            pddDdkGoodsSearchRequest.setActivityTags(Collections.singletonList(24));//24-品牌高佣
            pddDdkGoodsSearchRequest.setPid(pid);
            try {
                //同步调用
                PddDdkGoodsSearchResponse pddDdkGoodsSearchResponse = getClient().syncInvoke(pddDdkGoodsSearchRequest);
                if (StrUtil.equals(env, "dev")) {
                    logger.info("Method:[{}],Request:{},Response:{}", "PddDdkGoodsSearch", JsonUtil.transferToJson(pddDdkGoodsSearchRequest), JsonUtil.transferToJson(pddDdkGoodsSearchResponse));
                }
                if (pddDdkGoodsSearchResponse.getGoodsSearchResponse().getGoodsList() != null && pddDdkGoodsSearchResponse.getGoodsSearchResponse().getGoodsList().size() > 0) {
                    PddDdkGoodsSearchResponse.GoodsSearchResponseGoodsListItem goodsListItem = pddDdkGoodsSearchResponse.getGoodsSearchResponse().getGoodsList().get(0);

                    PddDdkGoodsPromotionUrlGenerateRequest pddDdkGoodsPromotionUrlGenerateRequest = new PddDdkGoodsPromotionUrlGenerateRequest();
                    pddDdkGoodsPromotionUrlGenerateRequest.setGoodsSignList(Collections.singletonList(goodsListItem.getGoodsSign()));
                    pddDdkGoodsPromotionUrlGenerateRequest.setPId(pid);
                    pddDdkGoodsPromotionUrlGenerateRequest.setSearchId(goodsListItem.getSearchId());
                    PddDdkGoodsPromotionUrlGenerateResponse pddDdkGoodsPromotionUrlGenerateResponse = getClient().syncInvoke(pddDdkGoodsPromotionUrlGenerateRequest);
                    if (StrUtil.equals(env, "dev")) {
                        logger.info("Method:[{}],Request:{},Response:{}", "PddDdkGoodsPromotionUrlGenerate", JsonUtil.transferToJson(pddDdkGoodsPromotionUrlGenerateRequest), JsonUtil.transferToJson(pddDdkGoodsPromotionUrlGenerateResponse));
                    }
                    if (pddDdkGoodsPromotionUrlGenerateResponse.getGoodsPromotionUrlGenerateResponse() != null &&
                            pddDdkGoodsPromotionUrlGenerateResponse.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList() != null &&
                            pddDdkGoodsPromotionUrlGenerateResponse.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList().size() > 0) {
                        PddDdkGoodsPromotionUrlGenerateResponse.GoodsPromotionUrlGenerateResponseGoodsPromotionUrlListItem urlListItem =
                                pddDdkGoodsPromotionUrlGenerateResponse.getGoodsPromotionUrlGenerateResponse().getGoodsPromotionUrlList().get(0);

                        WxMessage wxMessage = new WxMessage();
                        wxMessage.setFromUserName(request.getToUserName());
                        wxMessage.setToUserName(request.getFromUserName());
                        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
                        wxMessage.setMsgType("news");
                        wxMessage.setArticleCount(1);
                        List<WxMessage.Articles> articlesList = new ArrayList<>();
                        WxMessage.Articles articles = new WxMessage.Articles();
                        articles.setTitle(goodsListItem.getGoodsName());
                        articles.setPicUrl(goodsListItem.getGoodsThumbnailUrl());
                        articles.setUrl(urlListItem.getMobileUrl());
                        articlesList.add(articles);
                        wxMessage.setArticles(articlesList);

//                        wxMessage.setMsgType("text");
//                        wxMessage.setContent(urlListItem.getMobileUrl());


                        return wxMessage;
                    } else {

                    }
                } else {
                    // 没有搜索到
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return "";
    }

    @GetMapping(value = "/pdd")
    public Object pdd(Map<String, Object> request) {
        return "";
    }

    @ApiOperation("推广位生成备案链接")
    @GetMapping(value = "/pdd/pid/generate/url")
    public CommonResult pddPidGenerateUrl(@RequestParam Map<String, String> request) {
        try {
            List<String> list = new ArrayList<>();
            if (request != null) {
                list.add(request.get("pid"));
            }
            PddDdkRpPromUrlGenerateRequest pddDdkGoodsPidGenerateRequest = new PddDdkRpPromUrlGenerateRequest();
            pddDdkGoodsPidGenerateRequest.setChannelType(10);
            pddDdkGoodsPidGenerateRequest.setPIdList(list);
            //同步调用
            PddDdkRpPromUrlGenerateResponse response = getClient().syncInvoke(pddDdkGoodsPidGenerateRequest);
            if (StrUtil.equals(env, "dev")) {
                logger.info("Method:[{}],Request:{},Response:{}", "PddDdkRpPromUrlGenerate", JsonUtil.transferToJson(pddDdkGoodsPidGenerateRequest), JsonUtil.transferToJson(response));
            }
            return CommonResult.success(response);
        } catch (Exception e) {
            System.out.println(e);
            return CommonResult.failed(e.getLocalizedMessage());
        }
    }
}