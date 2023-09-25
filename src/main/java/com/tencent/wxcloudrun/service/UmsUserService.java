package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.UserInfoParam;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.UmsUser;

/**
 * 服务类
 *
 * @author zjf
 * @since 2023年08月02日
 */
public interface UmsUserService extends IService<UmsUser> {
  /**
   * 公众号消息
   *
   * @param request 消息
   * @return
   */
  Object userMessage(WxMessageRequest request);

  /**
   * 登录
   *
   * @param openid 小程序openid
   * @param unionid 公众号openid
   * @return
   */
  UmsUser login(String openid, String unionid);

  /**
   * 登录
   *
   * @param openid 小程序openid(绑定时候用到)
   * @param gOpenid 公众号openid
   * @return
   */
  UmsUser loginG(String openid, String gOpenid);

  /**
   * 用户信息更新
   *
   * @param request
   * @return
   */
  Object update(UserInfoParam request);
}
