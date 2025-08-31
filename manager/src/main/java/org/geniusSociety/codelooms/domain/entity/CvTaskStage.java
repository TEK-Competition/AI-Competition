/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 任务计划
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cv_task_stage")
public class CvTaskStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    // 任务ID
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    // 状态
    @Column(name = "status")
    private Integer status;
    // 阶段
    @Column(name = "stage")
    private Integer stage;
    // 开始时间
    @Column(name = "start_time")
    private Date startTime;
    // 介绍时间
    @Column(name = "finish_time")
    private Date finishTime;
    // 用户ID
    @Column(name = "user_id")
    private Integer userId;

}

