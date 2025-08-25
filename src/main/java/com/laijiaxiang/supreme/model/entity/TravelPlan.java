package com.laijiaxiang.supreme.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@TableName("c_travel_plan")
@Alias("travelPlan")
public class TravelPlan {

    private Long id;

    private Long userId;
    private String placeOfDeparture;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private int peopleAmount;
    private String budgetRange;
    private String preference;
    private String additionalInstructions;

    private String result;
    private String status;
    private Integer retryTimes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

}
