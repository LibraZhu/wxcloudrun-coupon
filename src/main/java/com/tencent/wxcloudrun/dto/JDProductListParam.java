package com.tencent.wxcloudrun.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class JDProductListParam extends PageParam {
  @ApiModelProperty(value = "第几页（默认第1页）")
  @NotNull(message = "频道不能为空")
  private Integer eliteId;

  @ApiModelProperty(value = "1 单价 4销量")
  private String sortName;

  @ApiModelProperty(value = "asc 升序 desc 降序 默认降序")
  private String sort;
}
