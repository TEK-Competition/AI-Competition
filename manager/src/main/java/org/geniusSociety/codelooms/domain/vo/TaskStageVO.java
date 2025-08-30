/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class TaskStageVO {
    @Schema(name = "ID")
    private Long id;
    @Schema(name = "任务ID")
    private Long taskId;
    @Schema(name = "状态")
    private Integer status;
    @Schema(name = "步骤")
    private Integer stage;
    @Schema(name = "开始时间")
    private Date startTime;
    @Schema(name = "结束时间")
    private Date finishTime;
}

