package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.dao.UmsUserMapper;
import com.tencent.wxcloudrun.dto.UserInfoParam;
import com.tencent.wxcloudrun.dto.WxMessageRequest;
import com.tencent.wxcloudrun.model.UmsUser;
import com.tencent.wxcloudrun.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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

  @Override
  public Object userMessage(WxMessageRequest request) {
    if (request != null && StrUtil.equals("text", request.getMsgType())) {
      if (request.getContent().startsWith("https://item.m.jd.com")
          || request.getContent().startsWith("https://item.jd.com")) {
        return jdService.wxMessage(request);
      } else if (request.getContent().contains("yangkeduo.com")) {
        return pddService.wxMessage(request);
      } else if (request.getContent().contains("m.tb.cn")
          || request.getContent().contains("item.taobao.com")
          || request.getContent().contains("detail.tmall.com")) {
        return tbService.wxMessage(request);
      } else if (request.getContent().contains("v.douyin.com")) {
        return dyService.wxMessage(request);
      } else if (request.getContent().contains("t.vip.com")) {
        return wpService.wxMessage(request);
      }
    }
    return "success";
  }

  @Override
  public UmsUser login(String openid, String unionid) {
    if (ObjectUtil.isEmpty(openid)) {
      Asserts.fail("openid不能为空");
    }
    QueryWrapper<UmsUser> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("openid", openid);
    UmsUser umsUser = this.getOne(queryWrapper);
    if (umsUser == null) {
      umsUser = new UmsUser();
      umsUser.setOpenid(openid);
      umsUser.setUnionid(unionid);
      baseMapper.insert(umsUser);
    } else {
      if (ObjectUtil.isNotEmpty(unionid)) {
        baseMapper.updateById(umsUser);
      }
    }
    return umsUser;
  }

  @Override
  public Object update(UserInfoParam request) {
    UpdateWrapper<UmsUser> updateWrapper = new UpdateWrapper<>();
    updateWrapper.eq("id", request.getUid());
    if (ObjectUtil.isNotEmpty(request.getAvatar())) {
      updateWrapper.set("avatar", request.getAvatar());
    }
    if (ObjectUtil.isNotEmpty(request.getNickname())) {
      updateWrapper.set("nickname", request.getNickname());
    }
    return this.update(updateWrapper);
  }
}
