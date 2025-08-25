package com.laijiaxiang.supreme.model.entity;

import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SupremePrompt {

    private String placeOfDeparture;

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    private int peopleAmount = 1;

    private String budgetRange;

    private String preference;

    private String additionalInstructions;

    public String toPrompt() {
        return "出发地：" + placeOfDeparture
                + "\n目的地：" + destination
                + "\n出发日期：" + startDate
                + "\n结束日期：" + endDate
                + "\n预算范围：" + budgetRange
                + "\n偏好：" + preference
                + "\n其他要求：" + (additionalInstructions == null ? "" : additionalInstructions);
    }

}
