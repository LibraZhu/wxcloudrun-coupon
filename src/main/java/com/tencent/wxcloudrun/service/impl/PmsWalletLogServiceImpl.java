package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.dao.PmsWalletLogMapper;
import com.tencent.wxcloudrun.dto.PageParam;
import com.tencent.wxcloudrun.model.PmsWalletLog;
import com.tencent.wxcloudrun.service.PmsWalletLogService;
import org.springframework.stereotype.Service;

/**
 * 钱包变动日志 服务实现类
 *
 * @author zjf
 * @since 2023年08月08日
 */
@Service
public class PmsWalletLogServiceImpl extends ServiceImpl<PmsWalletLogMapper, PmsWalletLog>
    implements PmsWalletLogService {

  @Override
  public CommonPage<PmsWalletLog> listLog(PageParam request) {
    if (ObjectUtil.isEmpty(request.getUid())) {
      Asserts.fail("用户不能为空");
    }
    Page<PmsWalletLog> page =
        lambdaQuery()
            .eq(PmsWalletLog::getUid, request.getUid())
            .eq(PmsWalletLog::getDisplay, 1)
            .page(Page.of(request.getPage(), request.getPageSize()));
    return CommonPage.page(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
  }
}
