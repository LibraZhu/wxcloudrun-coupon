package com.tencent.wxcloudrun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HJKTBInviterResponse extends HJKResponse {
  private TbInviterData data;

  @Data
  public static class TbInviterData {
    private TbInviterList inviter_list;
  }

  @Data
  public static class TbInviterList {
    private TbMapData map_data;
  }

  @Data
  public static class TbMapData {
    private String special_id;
    private String external_id;
    private String root_pid;
    private String create_date;
  }
}
