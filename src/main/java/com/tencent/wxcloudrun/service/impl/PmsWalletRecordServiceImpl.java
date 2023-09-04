package com.tencent.wxcloudrun.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencent.wxcloudrun.common.api.CommonPage;
import com.tencent.wxcloudrun.common.exception.Asserts;
import com.tencent.wxcloudrun.dao.PmsWalletRecordMapper;
import com.tencent.wxcloudrun.dto.PageParam;
import com.tencent.wxcloudrun.dto.WalletCashConfirmParam;
import com.tencent.wxcloudrun.dto.WalletCashParam;
import com.tencent.wxcloudrun.dto.WalletSettleParam;
import com.tencent.wxcloudrun.enums.WalletPayStatus;
import com.tencent.wxcloudrun.enums.WalletPayType;
import com.tencent.wxcloudrun.enums.WalletRecordType;
import com.tencent.wxcloudrun.model.OmsOrder;
import com.tencent.wxcloudrun.model.PmsWallet;
import com.tencent.wxcloudrun.model.PmsWalletLog;
import com.tencent.wxcloudrun.model.PmsWalletRecord;
import com.tencent.wxcloudrun.service.OmsOrderService;
import com.tencent.wxcloudrun.service.PmsWalletLogService;
import com.tencent.wxcloudrun.service.PmsWalletRecordService;
import com.tencent.wxcloudrun.service.PmsWalletService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 钱包交易记录表 服务实现类
 *
 * @author zjf
 * @since 2023年08月08日
 */
@Service
public class PmsWalletRecordServiceImpl extends ServiceImpl<PmsWalletRecordMapper, PmsWalletRecord>
    implements PmsWalletRecordService {

  @Resource OmsOrderService omsOrderService;
  @Resource PmsWalletLogService pmsWalletLogService;
  @Resource PmsWalletService pmsWalletService;

  @Override
  @Transactional
  public Object settle(WalletSettleParam request) {
    // 获取订单对应的返利总额
    BigDecimal totalRebate =
        Optional.ofNullable(
                omsOrderService.getMap(
                    new QueryWrapper<OmsOrder>()
                        .select("CAST(SUM(rebate) as DECIMAL (16, 2)) AS totalRebate")
                        .in("order_id", request.getOrderIds())))
            .map(map -> (BigDecimal) map.get("totalRebate"))
            .orElseGet(() -> new BigDecimal("0.00"));
    // 结算操作插入一条交易记录
    PmsWalletRecord record = new PmsWalletRecord();
    String recordSn = getRecordSn();
    record.setRecordSn(recordSn);
    record.setMoney(totalRebate.toString());
    record.setType(WalletRecordType.SETTLE.getCode());
    record.setPayType(WalletPayType.SETTLE.getCode());
    record.setPayStatus(WalletPayStatus.SUCCESS.getCode());
    LocalDateTime createTime = LocalDateTime.now();
    record.setPayTime(createTime);
    record.setCreateTime(createTime);
    record.setSettlePeriod(request.getPeriod());
    record.setSettleOrderId(String.join(",", request.getOrderIds()));
    record.setUid(Long.valueOf(request.getUid()));
    record.setRemark(request.getRemark());
    this.save(record);
    // 查询余额
    PmsWallet pmsWallet =
        new LambdaQueryChainWrapper<>(pmsWalletService.getBaseMapper())
            .eq(PmsWallet::getUid, request.getUid())
            .oneOpt()
            .orElseGet(
                () -> {
                  PmsWallet wallet = new PmsWallet();
                  wallet.setUid(Long.valueOf(request.getUid()));
                  return wallet;
                });
    // 将结算的返利累加到余额
    String moneyAfter =
        new BigDecimal(Optional.ofNullable(pmsWallet.getMoney()).orElse("0"))
            .add(totalRebate)
            .toString();
    pmsWallet.setMoney(moneyAfter);
    pmsWalletService.saveOrUpdate(pmsWallet);

    // 添加一条变更日志
    PmsWalletLog log = new PmsWalletLog();
    log.setRecordSn(recordSn);
    log.setUid(Long.valueOf(request.getUid()));
    log.setChangeMoney(String.format("+%s", totalRebate));
    log.setMoney(moneyAfter);
    log.setDes("结算");
    log.setCreateTime(createTime);
    pmsWalletLogService.save(log);
    return recordSn;
  }

  @Override
  @Transactional
  public Object cash(WalletCashParam request) {
    // 查询余额
    PmsWallet pmsWallet =
        new LambdaQueryChainWrapper<>(pmsWalletService.getBaseMapper())
            .eq(PmsWallet::getUid, request.getUid())
            .oneOpt()
            .orElseGet(
                () -> {
                  PmsWallet wallet = new PmsWallet();
                  wallet.setUid(Long.valueOf(request.getUid()));
                  return wallet;
                });
    BigDecimal cashMoney = new BigDecimal(request.getMoney()).setScale(2, RoundingMode.DOWN);
    BigDecimal money = new BigDecimal(Optional.ofNullable(pmsWallet.getMoney()).orElse("0"));
    if (cashMoney.compareTo(money) > 0) {
      Asserts.fail("提现金额不能大于余额");
    }
    // 创建提现申请记录
    PmsWalletRecord record = new PmsWalletRecord();
    String recordSn = getRecordSn();
    record.setRecordSn(recordSn);
    record.setMoney(cashMoney.toString());
    record.setType(WalletRecordType.CASH_OUT.getCode());
    record.setPayType(request.getPayType());
    record.setPayAccount(request.getAccount());
    record.setPayStatus(WalletPayStatus.NO.getCode());
    LocalDateTime createTime = LocalDateTime.now();
    record.setCreateTime(createTime);
    record.setUid(Long.valueOf(request.getUid()));
    this.save(record);
    // 将提现的金额从余额扣除
    String moneyAfter = money.subtract(cashMoney).toString();
    pmsWallet.setMoney(moneyAfter);
    // 如果提现交易方式变了更新默认方式
    if (ObjectUtil.notEqual(pmsWallet.getPayType(), request.getPayType())) {
      pmsWallet.setPayType(request.getPayType());
    }
    pmsWalletService.saveOrUpdate(pmsWallet);
    // 添加一条变更日志
    PmsWalletLog log = new PmsWalletLog();
    log.setRecordSn(recordSn);
    log.setUid(Long.valueOf(request.getUid()));
    log.setChangeMoney(String.format("-%s", cashMoney));
    log.setMoney(moneyAfter);
    log.setDes("提现");
    log.setCreateTime(createTime);
    pmsWalletLogService.save(log);
    return recordSn;
  }

  @Override
  public Object cashConfirm(WalletCashConfirmParam request) {
    return lambdaUpdate()
        .set(PmsWalletRecord::getPayStatus, WalletPayStatus.SUCCESS.getCode())
        .set(PmsWalletRecord::getPayTime, LocalDateTime.now())
        .eq(PmsWalletRecord::getRecordSn, request.getRecordSn())
        .eq(PmsWalletRecord::getType, WalletRecordType.CASH_OUT.getCode())
        .eq(PmsWalletRecord::getUid, request.getUid())
        .update();
  }

  @Override
  @Transactional
  public Object cashFail(WalletCashConfirmParam request) {
    PmsWalletRecord record =
        lambdaQuery()
            .eq(PmsWalletRecord::getRecordSn, request.getRecordSn())
            .eq(PmsWalletRecord::getType, WalletRecordType.CASH_OUT.getCode())
            .eq(PmsWalletRecord::getUid, request.getUid())
            .one();
    if (record == null) {
      Asserts.fail("提现记录不存在");
    }
    record.setRemark(
        Optional.ofNullable(record.getRemark())
            .map(s -> s + "。" + request.getRemark())
            .orElse(request.getRemark()));
    record.setPayStatus(WalletPayStatus.FAIL.getCode());
    this.updateById(record);
    // 钱包金额变更, 目前结算和提现不是并发，不存在金额更新不正确问题。后续如果有并发，添加表行锁。
    PmsWallet pmsWallet =
        new LambdaQueryChainWrapper<>(pmsWalletService.getBaseMapper())
            .eq(PmsWallet::getUid, request.getUid())
            .oneOpt()
            .orElseGet(
                () -> {
                  PmsWallet wallet = new PmsWallet();
                  wallet.setUid(Long.valueOf(request.getUid()));
                  return wallet;
                });
    String moneyAfter =
        new BigDecimal(pmsWallet.getMoney()).add(new BigDecimal(record.getMoney())).toString();
    pmsWallet.setMoney(moneyAfter);
    pmsWalletService.saveOrUpdate(pmsWallet);
    // 添加一条变更日志
    PmsWalletLog log = new PmsWalletLog();
    log.setRecordSn(record.getRecordSn());
    log.setUid(Long.valueOf(request.getUid()));
    log.setChangeMoney(String.format("+%s", record.getMoney()));
    log.setMoney(moneyAfter);
    log.setDes("提现失败");
    log.setCreateTime(LocalDateTime.now());
    pmsWalletLogService.save(log);
    return null;
  }

  @Override
  public CommonPage<PmsWalletRecord> settleList(PageParam request) {
    if (ObjectUtil.isEmpty(request.getUid())) {
      Asserts.fail("用户不能为空");
    }
    Page<PmsWalletRecord> page =
        lambdaQuery()
            .eq(PmsWalletRecord::getUid, request.getUid())
            .eq(PmsWalletRecord::getType, WalletRecordType.SETTLE.getCode())
            .eq(PmsWalletRecord::getPayStatus, WalletPayStatus.SUCCESS)
            .page(Page.of(request.getPage(), request.getPageSize()));
    return CommonPage.page(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
  }

  @Override
  public CommonPage<PmsWalletRecord> cashList(PageParam request) {
    if (ObjectUtil.isEmpty(request.getUid())) {
      Asserts.fail("用户不能为空");
    }
    Page<PmsWalletRecord> page =
        lambdaQuery()
            .eq(PmsWalletRecord::getUid, request.getUid())
            .eq(PmsWalletRecord::getType, WalletRecordType.CASH_OUT.getCode())
            .page(Page.of(request.getPage(), request.getPageSize()));
    return CommonPage.page(
        page.getCurrent(),
        page.getSize(),
        page.getTotal(),
        page.getRecords().stream()
            .peek(
                record ->
                    record.setPayAccount(
                        Optional.ofNullable(record.getPayAccount())
                            .map(
                                account -> {
                                  if (record.getPayType() == WalletPayType.BANK.getCode()) {
                                    String[] temp = account.split(",");
                                    temp[0] = temp[0].length()>4?"****"+temp[0].substring(temp[0].length()-4):temp[0];
                                    return String.join(",", temp);
                                  } else {
                                    return account;
                                  }
                                })
                            .orElse("")))
            .collect(Collectors.toList()));
  }

  private synchronized String getRecordSn() {
    // 时间（精确到毫秒）
    DateTimeFormatter ofPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    String localDate = LocalDateTime.now().format(ofPattern);
    // 3位随机数
    String randomNumeric = RandomStringUtils.randomNumeric(3);
    return localDate + randomNumeric;
  }
}
