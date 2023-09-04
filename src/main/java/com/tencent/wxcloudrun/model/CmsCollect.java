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
 * 收藏表
 * </p>
 *
 * @author zjf
 * @since 2023年08月04日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cms_collect")
public class CmsCollect implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品id
     */
    @TableField("product_id")
    private String productId;

    /**
     * 用户id
     */
    @TableField("uid")
    private Long uid;

    /**
     * 商品json
     */
    @TableField("product")
    private String product;


}
