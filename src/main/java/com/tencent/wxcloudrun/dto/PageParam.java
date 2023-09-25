package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** 分页参数 */
@Data
public class PageParam {
  @ApiModelProperty(value = "每页大小")
  private Long pageSize = 20L;

  @ApiModelProperty(value = "当前页")
  private Long page = 1L;
}
