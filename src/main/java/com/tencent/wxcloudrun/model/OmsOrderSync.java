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
 * 订单同步信息表
 * </p>
 *
 * @author zjf
 * @since 2023年07月12日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("oms_order_sync")
public class OmsOrderSync implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 来源: 1->pdd; 2->jd; 3->tb
     */
    @TableField("order_source")
    private Integer orderSource;

    /**
     * 订单最后同步时间
     */
    @TableField("order_sync_time")
    private LocalDateTime orderSyncTime;


}
