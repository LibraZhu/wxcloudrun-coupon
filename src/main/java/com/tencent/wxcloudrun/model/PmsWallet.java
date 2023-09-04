package com.tencent.wxcloudrun.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 用户钱包
 * </p>
 *
 * @author zjf
 * @since 2023年08月08日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_wallet")
public class PmsWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField("uid")
    private Long uid;

    /**
     * 金额
     */
    @TableField("money")
    private String money;

    /**
     * 提现姓名，不可更改
     */
    @TableField("name")
    private String name;

    /**
     * 身份证号
     */
    @TableField("idcard")
    private String idcard;

    /**
     * 银行卡号
     */
    @TableField("bank")
    private String bank;

    /**
     * 银行名称
     */
    @TableField("bank_name")
    private String bankName;

    /**
     * 微信账号
     */
    @TableField("weixin")
    private String weixin;

    /**
     * 默认交易方式 1银行卡 2微信
     */
    @TableField("pay_type")
    private Integer payType;


}
