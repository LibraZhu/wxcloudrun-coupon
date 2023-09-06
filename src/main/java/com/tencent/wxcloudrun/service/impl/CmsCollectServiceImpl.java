package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdd.pop.sdk.common.util.JsonUtil;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dao.CmsCollectMapper;
import com.tencent.wxcloudrun.dto.CollectParam;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.model.CmsCollect;
import com.tencent.wxcloudrun.service.CmsCollectService;
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
    if (ObjectUtil.isEmpty(request.getUid())) {
      return false;
    }
    if (ObjectUtil.isEmpty(request.getProductId())) {
      return false;
    }
    return lambdaQuery()
            .eq(CmsCollect::getUid, request.getUid())
            .eq(CmsCollect::getProductId, request.getProductId())
            .one()
        != null;
  }

  @Override
  public CommonPage<HJKJDProduct> listCollect(CollectParam request) {
    if (ObjectUtil.isEmpty(request.getUid())) {
      return CommonPage.page(request.getPage(), request.getPageSize(), 0L, null);
    }
    Page<CmsCollect> page =
        lambdaQuery()
            .eq(CmsCollect::getUid, request.getUid())
            .orderByDesc(CmsCollect::getId)
            .page(Page.of(request.getPage(), request.getPageSize()));
    return CommonPage.page(
        page.getCurrent(),
        page.getSize(),
        page.getTotal(),
        page.getRecords().stream()
            .map(item -> JsonUtil.transferToObj(item.getProduct(), HJKJDProduct.class))
            .collect(Collectors.toList()));
  }

  @Override
  public Object handle(CollectParam request) {
    if (ObjectUtil.isEmpty(request.getUid())) {
      return false;
    }
    if (ObjectUtil.isEmpty(request.getProductId())) {
      return false;
    }
    QueryWrapper<CmsCollect> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("uid", request.getUid());
    queryWrapper.eq("product_id", request.getProductId());
    if (request.getCollect()) {
      CmsCollect cmsCollect = this.getOne(queryWrapper);
      if (cmsCollect == null) {
        cmsCollect = new CmsCollect();
        cmsCollect.setUid(Long.valueOf(request.getUid()));
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
