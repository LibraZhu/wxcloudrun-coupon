package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonResult;
import com.tencent.wxcloudrun.dto.UserInfoParam;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.UmsUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zjf
 * @since 2023年08月02日
 */
public interface UmsUserService extends IService<UmsUser> {
    Object userMessage(WxMessageRequest request);
    UmsUser login(String openid, String unionid);
    Object update(UserInfoParam request);
}
