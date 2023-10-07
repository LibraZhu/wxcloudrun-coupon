package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.dao.UmsUserMapper;
import com.tencent.wxcloudrun.dto.UserInfoParam;
import com.tencent.wxcloudrun.dto.WxMessage;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.PmsWallet;
import com.tencent.wxcloudrun.model.UmsUser;
import com.tencent.wxcloudrun.service.*;
import com.tencent.wxcloudrun.utils.JsonUtil;
import com.tencent.wxcloudrun.utils.RequestHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 服务实现类
 *
 * @author zjf
 * @since 2023年08月02日
 */
@Service
public class UmsUserServiceImpl extends ServiceImpl<UmsUserMapper, UmsUser>
    implements UmsUserService {

  final Logger logger = LoggerFactory.getLogger(UmsUserServiceImpl.class);

  @Resource private PddService pddService;
  @Resource private JDService jdService;
  @Resource private TBService tbService;
  @Resource private DYService dyService;
  @Resource private WPHService wpService;
  @Resource private PmsWalletService pmsWalletService;

  @Override
  public Object userMessage(WxMessageRequest request) {
    UmsUser umsUser = loginG(null, request.getFromUserName());
    Object message = "success";
    if (ObjectUtil.isNull(umsUser)) {
      return message;
    }
    if (StrUtil.equals("text", request.getMsgType())) {
      if (request.getContent().startsWith("https://item.m.jd.com")
          || request.getContent().startsWith("https://item.jd.com")) {
        message = jdService.wxMessage(request, umsUser.getId());
      } else if (request.getContent().contains("yangkeduo.com")) {
        message = pddService.wxMessage(request, umsUser.getId());
      } else if (request.getContent().contains("m.tb.cn")
          || request.getContent().contains("item.taobao.com")
          || request.getContent().contains("detail.tmall.com")) {
        message = tbService.wxMessage(request, umsUser.getId());
      } else if (request.getContent().contains("v.douyin.com")) {
        message = dyService.wxMessage(request, umsUser.getId());
      } else if (request.getContent().contains("t.vip.com")) {
        message = wpService.wxMessage(request, umsUser.getId());
      } else if (ObjectUtil.equals(request.getContent(), "找券返")) {
        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        wxMessage.setMsgType("news");
        wxMessage.setArticleCount(1);
        WxMessage.Articles articles = new WxMessage.Articles();
        articles.setTitle("找券返");
        articles.setDescription("专属查返利");
        articles.setPicUrl("");
        articles.setUrl("https://coupon-h5.pages.dev/#/search/index?uid=" + umsUser.getId());
        wxMessage.setArticles(Collections.singletonList(articles));

      } else if (ObjectUtil.equals(request.getContent(), "提现")) {
        WxMessage wxMessage = new WxMessage();
        wxMessage.setFromUserName(request.getToUserName());
        wxMessage.setToUserName(request.getFromUserName());
        wxMessage.setCreateTime(String.valueOf((int) (System.currentTimeMillis() / 1000)));
        String money =
            pmsWalletService
                .lambdaQuery()
                .eq(PmsWallet::getUid, umsUser.getId())
                .oneOpt()
                .map(PmsWallet::getMoney)
                .orElse("0.00");
        if (ObjectUtil.equals(money, "0.00")) {
          wxMessage.setMsgType("text");
          wxMessage.setContent("账户余额: " + money);
        } else {
          wxMessage.setMsgType("news");
          wxMessage.setArticleCount(1);
          WxMessage.Articles articles = new WxMessage.Articles();
          articles.setTitle("账户余额: " + money);
          articles.setDescription("点击提交申请");
          articles.setPicUrl("");
          articles.setUrl("https://coupon-h5.pages.dev/#/wallet/index?uid=" + umsUser.getId());
          wxMessage.setArticles(Collections.singletonList(articles));
        }
        message = wxMessage;
      }
    }
    logger.info(
        "[{}],Uid:{},Request:{},Response:{}",
        "公众号消息查询",
        umsUser.getId(),
        JsonUtil.toJson(request),
        ObjectUtil.equals(message, "success") ? "success" : JsonUtil.toJson(message));
    return message;
  }

  @Override
  public UmsUser login(String openid, String unionid) {
    if (ObjectUtil.isEmpty(openid)) {
      logger.info("[{}],openid:{},unionid:{}", "登录", openid, unionid);
      Asserts.fail("openid不能为空");
    }
    try {
      UmsUser umsUser = lambdaQuery().eq(UmsUser::getOpenid, openid).one();
      if (umsUser == null) {
        umsUser = new UmsUser();
        umsUser.setOpenid(openid);
        umsUser.setUnionid(unionid);
        baseMapper.insert(umsUser);
        //
        pmsWalletService.add(umsUser.getId());
      } else {
        if (ObjectUtil.isNotEmpty(unionid)) {
          umsUser.setUnionid(unionid);
          baseMapper.updateById(umsUser);
        }
      }
      logger.info("[{}],uid:{},openid:{},unionid:{}", "登录", umsUser.getId(), openid, unionid);
      return umsUser;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public UmsUser loginG(String openid, String gOpendid) {
    if (ObjectUtil.isEmpty(gOpendid)) {
      logger.info("[{}],openid:{},gOpendid:{}", "公众号登录", openid, gOpendid);
      Asserts.fail("gOpendid不能为空");
    }
    try {
      UmsUser umsUser = lambdaQuery().eq(UmsUser::getGOpenid, gOpendid).one();
      if (umsUser == null) {
        umsUser = new UmsUser();
        umsUser.setOpenid(openid);
        umsUser.setGOpenid(gOpendid);
        baseMapper.insert(umsUser);
        //
        pmsWalletService.add(umsUser.getId());
      } else {
        if (ObjectUtil.isNotEmpty(openid)) {
          umsUser.setOpenid(openid);
          baseMapper.updateById(umsUser);
        }
        baseMapper.updateById(umsUser);
      }
      logger.info("[{}],uid:{},openid:{},gOpendid:{}", "公众号登录", umsUser.getId(), openid, gOpendid);
      return umsUser;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public Object update(UserInfoParam request) {
    String uid = RequestHolder.getUid();
    if (ObjectUtil.isEmpty(uid)) {
      Asserts.fail("用户不能为空");
    }
    UpdateWrapper<UmsUser> updateWrapper = new UpdateWrapper<>();
    updateWrapper.eq("id", uid);
    if (ObjectUtil.isNotEmpty(request.getAvatar())) {
      updateWrapper.set("avatar", request.getAvatar());
    }
    if (ObjectUtil.isNotEmpty(request.getNickname())) {
      updateWrapper.set("nickname", request.getNickname());
    }
    return this.update(updateWrapper);
  }
}
