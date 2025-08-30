/**
 * Cealus Li 2025/7/27
 * Copyright
 */
package org.geniusSociety.codelooms.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.geniusSociety.codelooms.common.base.BaseAction;
import org.geniusSociety.codelooms.common.base.IService;
import org.geniusSociety.codelooms.common.constant.WebConstant;
import org.geniusSociety.codelooms.common.vo.CommonResultVO;
import org.geniusSociety.codelooms.domain.vo.TaskVO;
import org.geniusSociety.codelooms.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务管理
 *
 * @author Cealus Li 2025/7/27
 */
@Tag(name = "任务管理")
@RestController
@RequestMapping(WebConstant.TASK)
public class TaskController extends BaseAction<TaskVO> {

    @Autowired
    private TaskService taskService;

    @PostMapping(value = "renew")
    @Operation(summary = "重置任务", method = "POST")
    public CommonResultVO<Long> renewTask(@NotNull(message = "缺少必要数据") @Parameter(description = "项目ID") final Long itemId) {
        taskService.renewTask(itemId, null, null);
        return CommonResultVO.success(itemId);
    }

    @PostMapping(value = "renew/stage")
    @Operation(summary = "重置任务阶段", method = "POST")
    public CommonResultVO<Long> renewStage(@NotNull(message = "缺少必要数据") @Parameter(description = "项目ID") final Long itemId,
                                           @NotNull(message = "缺少必要数据") @Parameter(description = "阶段") final Integer stage) {
        taskService.renewTask(itemId, stage, null);
        return CommonResultVO.success(itemId);
    }

    @PostMapping(value = "renew/file")
    @Operation(summary = "重置任务文件", method = "POST")
    public CommonResultVO<Long> renewFile(@NotNull(message = "缺少必要数据") @Parameter(description = "项目ID") final Long itemId,
                                          @NotNull(message = "缺少必要数据") @Parameter(description = "文件ID") final Long fileId) {
        taskService.renewTask(itemId, null, fileId);
        return CommonResultVO.success(fileId);
    }

    @Override
    protected IService<TaskVO> getService() {
        return taskService;
    }
}
