package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.utils.JsonUtil;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dao.CmsCollectMapper;
import com.tencent.wxcloudrun.dto.CollectParam;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.model.CmsCollect;
import com.tencent.wxcloudrun.service.CmsCollectService;
import com.tencent.wxcloudrun.utils.RequestHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 收藏表 服务实现类
 *
 * @author zjf
 * @since 2023年08月02日
 */
@Service
public class CmsCollectServiceImpl extends ServiceImpl<CmsCollectMapper, CmsCollect>
    implements CmsCollectService {

  @Override
  public Boolean check(CollectParam request) {
    String uid = RequestHolder.getUid();
    if (ObjectUtil.isEmpty(uid)) {
      return false;
    }
    if (ObjectUtil.isEmpty(request.getProductId())) {
      return false;
    }
    return lambdaQuery()
            .eq(CmsCollect::getUid, uid)
            .eq(CmsCollect::getProductId, request.getProductId())
            .one()
        != null;
  }

  @Override
  public CommonPage<HJKJDProduct> listCollect(CollectParam request) {
    String uid = RequestHolder.getUid();
    if (ObjectUtil.isEmpty(uid)) {
      return CommonPage.page(request.getPage(), request.getPageSize(), 0L, null);
    }
    Page<CmsCollect> page =
        lambdaQuery()
            .eq(CmsCollect::getUid, uid)
            .orderByDesc(CmsCollect::getId)
            .page(Page.of(request.getPage(), request.getPageSize()));
    return CommonPage.page(
        page.getCurrent(),
        page.getSize(),
        page.getTotal(),
        page.getRecords().stream()
            .map(item -> JsonUtil.toObj(item.getProduct(), HJKJDProduct.class))
            .collect(Collectors.toList()));
  }

  @Override
  public Object handle(CollectParam request) {
    String uid = RequestHolder.getUid();
    if (ObjectUtil.isEmpty(uid)) {
      return false;
    }
    if (ObjectUtil.isEmpty(request.getProductId())) {
      return false;
    }
    QueryWrapper<CmsCollect> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("uid", uid);
    queryWrapper.eq("product_id", request.getProductId());
    if (request.getCollect()) {
      CmsCollect cmsCollect = this.getOne(queryWrapper);
      if (cmsCollect == null) {
        cmsCollect = new CmsCollect();
        cmsCollect.setUid(Long.valueOf(uid));
        cmsCollect.setProductId(request.getProductId());
        cmsCollect.setProduct(request.getProduct());
        this.save(cmsCollect);
      }
      return true;
    } else {
      this.remove(queryWrapper);
      return false;
    }
  }
}
