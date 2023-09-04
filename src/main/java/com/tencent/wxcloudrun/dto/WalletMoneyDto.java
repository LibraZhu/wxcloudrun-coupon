package com.tencent.wxcloudrun.dto;

import lombok.Data;

/** 钱包金额信息 */
@Data
public class WalletMoneyDto {

  /** 余额 */
  private String money;

  /** 待结算订单 */
  private Integer settleOrderNum;

  /** 带结算金额 */
  private String unSettleMoney;

  /** 累计提现金额 */
  private String cashOutMoney;
}
