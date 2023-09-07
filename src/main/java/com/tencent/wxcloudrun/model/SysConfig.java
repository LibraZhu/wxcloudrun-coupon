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
 * 配置和参数表
 * </p>
 *
 * @author zjf
 * @since 2023年09月06日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("sys_config")
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    @TableField("c_name")
    private String cName;

    /**
     * 编码
     */
    @TableField("c_code")
    private String cCode;

    /**
     * 值
     */
    @TableField("c_value")
    private String cValue;


}
