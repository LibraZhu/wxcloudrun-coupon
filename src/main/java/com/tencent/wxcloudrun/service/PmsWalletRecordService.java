package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dto.PageParam;
import com.tencent.wxcloudrun.dto.WalletCashConfirmParam;
import com.tencent.wxcloudrun.dto.WalletCashParam;
import com.tencent.wxcloudrun.dto.WalletSettleParam;
import com.tencent.wxcloudrun.model.PmsWalletRecord;

/**
 * <p>
 * 钱包交易记录表 服务类
 * </p>
 *
 * @author zjf
 * @since 2023年08月08日
 */
public interface PmsWalletRecordService extends IService<PmsWalletRecord> {
    Object settle(WalletSettleParam request);

    Object cash(WalletCashParam request);

    Object cashConfirm(WalletCashConfirmParam request);

    Object cashFail(WalletCashConfirmParam request);

    CommonPage<PmsWalletRecord> settleList(PageParam request);

    CommonPage<PmsWalletRecord> cashList(PageParam request);
}
