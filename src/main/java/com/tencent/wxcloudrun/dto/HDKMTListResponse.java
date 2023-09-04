package com.tencent.wxcloudrun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class HDKMTListResponse {


    @JsonProperty("code")
    private Integer code;
    @JsonProperty("msg")
    private String msg;
    @JsonProperty("data")
    private DataDTO data;
    @JsonProperty("min_id")
    private Integer minId;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("red_activity")
        private List<RedActivityDTO> redActivity;
        @JsonProperty("time_activity")
        private List<TimeActivityDTO> timeActivity;

        @NoArgsConstructor
        @Data
        public static class RedActivityDTO {
            @JsonProperty("activity_id")
            private String activityId;
            @JsonProperty("activity_name")
            private String activityName;
            @JsonProperty("activity_desc_private")
            private String activityDescPrivate;
            @JsonProperty("activity_image")
            private String activityImage;
            @JsonProperty("start_time")
            private String startTime;
            @JsonProperty("end_time")
            private String endTime;
        }

        @NoArgsConstructor
        @Data
        public static class TimeActivityDTO {
            @JsonProperty("activity_id")
            private String activityId;
            @JsonProperty("activity_name")
            private String activityName;
            @JsonProperty("activity_desc_private")
            private String activityDescPrivate;
            @JsonProperty("activity_image")
            private String activityImage;
            @JsonProperty("start_time")
            private String startTime;
            @JsonProperty("end_time")
            private String endTime;
        }
    }
}
