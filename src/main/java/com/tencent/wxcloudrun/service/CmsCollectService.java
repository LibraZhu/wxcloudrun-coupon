package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dto.CollectParam;
import com.tencent.wxcloudrun.dto.HJKJDProduct;
import com.tencent.wxcloudrun.model.CmsCollect;

/**
 * <p>
 * 收藏表 服务类
 * </p>
 *
 * @author zjf
 * @since 2023年08月02日
 */
public interface CmsCollectService extends IService<CmsCollect> {
    Boolean check(CollectParam request);

    CommonPage<HJKJDProduct> listCollect(CollectParam request);

    Object handle(CollectParam request);
}
