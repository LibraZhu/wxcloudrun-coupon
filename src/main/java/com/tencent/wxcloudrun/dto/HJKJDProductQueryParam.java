package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKJDProductQueryParam extends PageParam {
  @ApiModelProperty(value = "关键字")
  private String keyword;

  @ApiModelProperty(value = "1 单价 4销量")
  private String sortName;

  @ApiModelProperty(value = "asc 升序 desc 降序 默认降序")
  private String sort;

  @ApiModelProperty(value = "g 自营 ，p POP")
  private String owner;
}
