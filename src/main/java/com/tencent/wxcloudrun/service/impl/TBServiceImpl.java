package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.StrUtil;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.TbkDgMaterialOptionalRequest;
import com.taobao.api.request.TbkSpreadGetRequest;
import com.taobao.api.response.TbkDgMaterialOptionalResponse;
import com.taobao.api.response.TbkSpreadGetResponse;
import com.tencent.wxcloudrun.config.properties.TaobaoProperties;
import com.tencent.wxcloudrun.dto.WxMessage;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.service.PddService;
import com.tencent.wxcloudrun.service.TBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TBServiceImpl implements TBService {

    final Logger logger = LoggerFactory.getLogger(TBServiceImpl.class);

    @Resource
    private TaobaoProperties taobaoProperties;

    @Value("${spring.profiles.active}")
    private String env;

    private DefaultTaobaoClient getTaobaoClient() {
        return new DefaultTaobaoClient(taobaoProperties.getUrl(), taobaoProperties.getAppKey(), taobaoProperties.getAppSecret());
    }

    @Override
    public void syncOrder(String startTime) {

    }

    @Override
    public Object wxMessage(WxMessageRequest request) {
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
        return "success";
    }
}
