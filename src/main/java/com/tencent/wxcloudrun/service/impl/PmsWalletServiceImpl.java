package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.dao.PmsWalletMapper;
import com.tencent.wxcloudrun.dto.WalletMoneyDto;
import com.tencent.wxcloudrun.dto.WalletParam;
import com.tencent.wxcloudrun.enums.OrderStatus;
import com.tencent.wxcloudrun.enums.WalletPayStatus;
import com.tencent.wxcloudrun.enums.WalletPayType;
import com.tencent.wxcloudrun.enums.WalletRecordType;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.model.PmsWallet;
import com.tencent.wxcloudrun.model.PmsWalletRecord;
import com.tencent.wxcloudrun.service.OmsOrderService;
import com.tencent.wxcloudrun.service.PmsWalletRecordService;
import com.tencent.wxcloudrun.service.PmsWalletService;
import com.tencent.wxcloudrun.utils.RequestHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户钱包 服务实现类
 *
 * @author zjf
 * @since 2023年08月08日
 */
@Service
public class PmsWalletServiceImpl extends ServiceImpl<PmsWalletMapper, PmsWallet>
    implements PmsWalletService {
  @Resource OmsOrderService omsOrderService;
  @Resource PmsWalletRecordService pmsWalletRecordService;

  @Override
  public void add(Long uid) {
    PmsWallet pmsWallet = new PmsWallet();
    pmsWallet.setUid(uid);
    baseMapper.insert(pmsWallet);
  }

  @Override
  public WalletMoneyDto number(WalletParam request) {
    String uid = RequestHolder.getUid();
    if (ObjectUtil.isEmpty(uid)) {
      Asserts.fail("用户不能为空");
    }
    WalletMoneyDto walletMoneyDto = new WalletMoneyDto();
    // 获取余额
    PmsWallet wallet = lambdaQuery().eq(PmsWallet::getUid, uid).one();
    walletMoneyDto.setMoney(Optional.ofNullable(wallet).map(PmsWallet::getMoney).orElse("0.00"));
    // 获取待结算订单数和金额
    List<OmsOrder> list =
        new LambdaQueryChainWrapper<>(omsOrderService.getBaseMapper())
            .eq(OmsOrder::getUid, uid)
            .eq(OmsOrder::getStatus, OrderStatus.COMPLETE.getCode())
            .list();
    walletMoneyDto.setSettleOrderNum(list.size());
    walletMoneyDto.setUnSettleMoney(
        list.stream()
            .map(order -> new BigDecimal((order.getRebate())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .toString());
    // 获取已提现金额
    BigDecimal totalMoney =
        Optional.ofNullable(
                pmsWalletRecordService.getMap(
                    new QueryWrapper<PmsWalletRecord>()
                        .select("CAST(SUM(money) as DECIMAL (16, 2)) AS totalMoney")
                        .eq("uid", uid)
                        .eq("type", WalletRecordType.CASH_OUT.getCode())
                        .eq("pay_status", WalletPayStatus.SUCCESS)))
            .map(map -> (BigDecimal) map.get("totalMoney"))
            .orElseGet(() -> new BigDecimal("0.00"));
    walletMoneyDto.setCashOutMoney(totalMoney.toString());
    return walletMoneyDto;
  }

  @Override
  public PmsWallet info(WalletParam request) {
    return lambdaQuery().eq(PmsWallet::getUid, RequestHolder.getUid()).one();
  }

  @Override
  public Object modify(WalletParam request) {
    if (ObjectUtil.isNotEmpty(request.getWeixin())) {
      PmsWallet wallet = lambdaQuery().eq(PmsWallet::getUid, RequestHolder.getUid()).one();
      // 不支持更新微信号
      if (wallet != null && ObjectUtil.isEmpty(wallet.getWeixin())) {
        wallet.setWeixin(request.getWeixin());
        wallet.setPayType(WalletPayType.WX.getCode());
        saveOrUpdate(wallet);
        return saveOrUpdate(wallet);
      }
    }
    return false;
  }
}
