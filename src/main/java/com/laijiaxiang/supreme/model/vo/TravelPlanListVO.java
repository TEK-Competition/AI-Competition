package com.laijiaxiang.supreme.model.vo;

import com.laijiaxiang.supreme.model.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "旅行计划列表")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravelPlanListVO {

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
    private int peopleAmount;

    @Schema(description = "预算范围")
    private String budgetRange;

    @Schema(description = "偏好")
    private List<String> preference;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "总共天数")
    private int totalDays;

    public int getTotalDays() {
        return (int) (this.endDate.toEpochDay() - this.startDate.toEpochDay());
    }

}
