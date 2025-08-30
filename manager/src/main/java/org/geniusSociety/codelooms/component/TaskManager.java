/**
 * Cealus Li 2025/7/26
 * Copyright
 */
package org.geniusSociety.codelooms.component;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geniusSociety.codelooms.common.constant.EntityType;
import org.geniusSociety.codelooms.dao.CvItemRepository;
import org.geniusSociety.codelooms.dao.CvTaskRepository;
import org.geniusSociety.codelooms.dao.CvTaskStageRepository;
import org.geniusSociety.codelooms.domain.entity.CvItem;
import org.geniusSociety.codelooms.domain.entity.CvTask;
import org.geniusSociety.codelooms.domain.entity.CvTaskStage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 任务管理
 *
 * @author Cealus Li 2025/7/26
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskManager {

    private final CvItemRepository itemRepository;

    private final CvTaskRepository taskRepository;

    private final CvTaskStageRepository taskStageRepository;

    private final ApplicationContext context;

    @PostConstruct
    public void init() {
        List<CvTask> tasks = this.getTask();
        for (CvTask task : tasks) {
            execute(task);
        }
    }

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.MINUTES, new LinkedBlockingDeque<>(), new ThreadPoolExecutor.DiscardPolicy());

    public void createTask(final CvItem item) {
        CvTask task = CvTask.builder().itemId(item.getId()).type(item.getType()).name(item.getName())
                .status(EntityType.TaskStaus.NEW).steps(EntityType.TaskStaus.NEW).userId(item.getUserId()).build();
        task = taskRepository.save(task);
        for (Integer stage : EntityType.TaskStage.stages) {
            if (!EntityType.TaskStage.READY.equals(stage)) {
                taskStageRepository.save(CvTaskStage.builder().taskId(task.getId()).stage(stage).status(EntityType.TaskStage.READY)
                        .userId(task.getUserId()).build());
            }
        }
        this.execute(task);
    }

    public void execute(final CvTask task) {
        executor.execute(new TaskWorker(task, this.context));
    }

    private List<CvTask> getTask() {
        return taskRepository.findAll((root, q, cb) ->
                cb.or(cb.equal(root.get("status"), EntityType.TaskStaus.NEW),
                        cb.equal(root.get("status"), EntityType.TaskStaus.RUNNING))
        );
    }
}
