package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dto.PageParam;
import com.tencent.wxcloudrun.model.PmsWalletLog;

/**
 * <p>
 * 钱包变动日志 服务类
 * </p>
 *
 * @author zjf
 * @since 2023年08月08日
 */
public interface PmsWalletLogService extends IService<PmsWalletLog> {
    CommonPage<PmsWalletLog> listLog(PageParam request);
}
