package com.laijiaxiang.supreme.model.vo;

import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "旅行计划详情")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravelPlanDetailVO {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "出发地")
    private String placeOfDeparture;

    @Schema(description = "目的地")
    private String destination;

    @Schema(description = "开始时间")
    private LocalDate startDate;

    @Schema(description = "结束时间")
    private LocalDate endDate;

    @Schema(description = "出行人数")
    private int peopleAmount = 1;

    @Schema(description = "预算范围")
    private String budgetRange;

    @Schema(description = "偏好")
    private List<String> preference;

    @Schema(description = "额外说明")
    private String additionalInstructions;

    @Schema(description = "AI生成结果")
    private List<TravelPlanResultVO> travelPlanResultList;

    public void setTravelPlanResultList(String aiResult) {
        String replaceResult = aiResult.replace("```json", "").replace("```", "");
        this.travelPlanResultList = JSON.parseArray(replaceResult, TravelPlanResultVO.class);
    }

    public static void checkResult(String aiResult) {
        String replaceResult = aiResult.replace("```json", "").replace("```", "");
        JSON.parseArray(replaceResult, TravelPlanResultVO.class);
    }

}

@Schema(description = "AI返回结果")
@Getter
@Setter
class TravelPlanResultVO {

    @Schema(description = "第几天")
    private String day;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "景点名称")
    private String scenicSpotName;

    @Schema(description = "天气情况")
    private String weather;

    @Schema(description = "交通工具")
    private List<String> vehicle;

    @Schema(description = "交通情况")
    private String trafficInformation;

    @Schema(description = "推荐原因")
    private String recommendReason;

    @Schema(description = "注意事项")
    private String precautions;

    @Schema(description = "活动安排")
    private String activityArrangement;

    @Schema(description = "当地美食")
    private String localCuisine;

}
