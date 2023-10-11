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
 * 用户表
 * </p>
 *
 * @author zjf
 * @since 2023年10月11日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("ums_user")
public class UmsUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 小程序openid
     */
    @TableField("openid")
    private String openid;

    /**
     * 公众号openid
     */
    @TableField("g_openid")
    private String gOpenid;

    /**
     * 小程序unionid
     */
    @TableField("unionid")
    private String unionid;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 登录时间
     */
    @TableField("login_time")
    private LocalDateTime loginTime;


}
