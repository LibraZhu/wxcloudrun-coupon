package com.tencent.wxcloudrun.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tencent.wxcloudrun.dto.WalletMoneyDto;
import com.tencent.wxcloudrun.dto.WalletParam;
import com.tencent.wxcloudrun.model.PmsWallet;

/**
 * 用户钱包 服务类
 *
 * @author zjf
 * @since 2023年08月08日
 */
public interface PmsWalletService extends IService<PmsWallet> {
  void add(Long uid);

  WalletMoneyDto number(WalletParam request);

  PmsWallet info(WalletParam request);

  Object modify(WalletParam request);
}
