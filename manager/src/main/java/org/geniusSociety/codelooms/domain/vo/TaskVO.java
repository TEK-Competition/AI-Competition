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
import java.util.List;

/**
 * 转换任务
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskVO {

    @Schema(name = "ID")
    private Long id;
    @Schema(name = "项目ID")
    private Long itemId;
    @Schema(name = "名称")
    private String name;
    @Schema(name = "运行模式", example = "1", description = "1：自动；2：手动")
    private Integer type;
    @Schema(name = "状态")
    private Integer status;
    @Schema(name = "步骤", description = "1： 初始化；2：表关系任务；3：表字段任务；4：转换任务；5：注释任务；6：完成")
    private Integer steps;
    @Schema(name = "阶段状态")
    private List<TaskStageVO> stages;
    @Schema(name = "开始时间")
    private Date startTime;
    @Schema(name = "结束时间")
    private Date finishTime;

}

