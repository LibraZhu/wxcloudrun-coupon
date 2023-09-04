package com.tencent.wxcloudrun.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 钱包变动日志
 * </p>
 *
 * @author zjf
 * @since 2023年08月08日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("pms_wallet_log")
public class PmsWalletLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易流水sn，关联pms_wallet_record
     */
    @TableField("record_sn")
    private String recordSn;

    /**
     * 用户id
     */
    @TableField("uid")
    private Long uid;

    /**
     * 变动金额 增+ 减-
     */
    @TableField("change_money")
    private String changeMoney;

    /**
     * 变动后的金额
     */
    @TableField("money")
    private String money;

    /**
     * 描述
     */
    @TableField("des")
    private String des;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 是否显示 0不显示 1显示
     */
    @TableField("display")
    private Integer display;


}
