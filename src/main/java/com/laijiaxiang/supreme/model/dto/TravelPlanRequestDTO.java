package com.laijiaxiang.supreme.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "旅行规划表单")
@Getter
@Setter
public class TravelPlanRequestDTO {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "出发地")
    @NotBlank(message = "出发地不能为空")
    private String placeOfDeparture;

    @Schema(description = "目的地")
    @NotBlank(message = "目的地不能为空")
    private String destination;

    @Schema(description = "出发日期")
    @NotNull(message = "出发日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "出行人数")
    private int peopleAmount = 1;

    @Schema(description = "预算范围")
    @NotBlank(message = "预算范围不能为空")
    private String budgetRange;

    @Schema(description = "偏好")
    @NotEmpty(message = "偏好不能为空")
    private List<String> preference;

    @Schema(description = "额外说明")
    private String additionalInstructions;

}
