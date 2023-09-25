package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 小程序用户更新参数 */
@Data
public class UserInfoParam {
  @ApiModelProperty(value = "avatar")
  private String avatar;

  @ApiModelProperty(value = "nickname")
  private String nickname;
}
