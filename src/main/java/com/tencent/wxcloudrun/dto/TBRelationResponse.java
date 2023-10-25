package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class TBRelationResponse {

  @JsonProperty("error")
  private String error;
  @JsonProperty("msg")
  private String msg;
  @JsonProperty("data")
  private DataDTO data;
  @JsonProperty("request_id")
  private String requestId;

  @NoArgsConstructor
  @Data
  public static class DataDTO {
    @JsonProperty("inviter_list")
    private InviterListDTO inviterList;
    @JsonProperty("root_pid_channel_list")
    private RootPidChannelListDTO rootPidChannelList;
    @JsonProperty("total_count")
    private Integer totalCount;

    @NoArgsConstructor
    @Data
    public static class InviterListDTO {
      @JsonProperty("map_data")
      private List<MapDataDTO> mapData;

      @NoArgsConstructor
      @Data
      public static class MapDataDTO {
        @JsonProperty("account_name")
        private String accountName;
        @JsonProperty("create_date")
        private String createDate;
        @JsonProperty("note")
        private String note;
        @JsonProperty("offline_scene")
        private String offlineScene;
        @JsonProperty("online_scene")
        private String onlineScene;
        @JsonProperty("punish_status")
        private String punishStatus;
        @JsonProperty("real_name")
        private String realName;
        @JsonProperty("relation_app")
        private String relationApp;
        @JsonProperty("relation_id")
        private Long relationId;
        @JsonProperty("root_pid")
        private String rootPid;
        @JsonProperty("rtag")
        private String rtag;
      }
    }

    @NoArgsConstructor
    @Data
    public static class RootPidChannelListDTO {
      @JsonProperty("string")
      private List<String> string;
    }
  }
}
