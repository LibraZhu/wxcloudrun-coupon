package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.dto.PageParam;
import com.tencent.wxcloudrun.dto.WalletCashConfirmParam;
import com.tencent.wxcloudrun.dto.WalletCashParam;
import com.tencent.wxcloudrun.dto.WalletSettleParam;
import com.tencent.wxcloudrun.model.PmsWalletRecord;

/**
 * 钱包交易记录表 服务类
 *
 * @author zjf
 * @since 2023年08月08日
 */
public interface PmsWalletRecordService extends IService<PmsWalletRecord> {
  /**
   * 结算
   *
   * @param request 结算请求
   * @return
   */
  Object settle(WalletSettleParam request);

  /**
   * 提现申请
   *
   * @param request 提现请求
   * @return
   */
  Object cash(WalletCashParam request);

  /**
   * 提现申请确认
   *
   * @param request
   * @return
   */
  Object cashConfirm(WalletCashConfirmParam request);

  /**
   * 提现申请取消
   *
   * @param request
   * @return
   */
  Object cashFail(WalletCashConfirmParam request);

  /**
   * 结算记录
   *
   * @param request
   * @return
   */
  CommonPage<PmsWalletRecord> settleList(PageParam request);

  /**
   * 提现记录
   *
   * @param request
   * @return
   */
  CommonPage<PmsWalletRecord> cashList(PageParam request);
}
