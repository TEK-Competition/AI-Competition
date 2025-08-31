/**
 * Cealus Li 2025/7/27
 * Copyright
 */
package org.geniusSociety.codelooms.service;


import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.common.base.BaseService;
import org.geniusSociety.codelooms.common.constant.EntityType;
import org.geniusSociety.codelooms.component.TaskManager;
import org.geniusSociety.codelooms.dao.CvItemFileRepository;
import org.geniusSociety.codelooms.dao.CvItemRepository;
import org.geniusSociety.codelooms.dao.CvTaskRepository;
import org.geniusSociety.codelooms.dao.CvTaskStageRepository;
import org.geniusSociety.codelooms.domain.entity.CvItem;
import org.geniusSociety.codelooms.domain.entity.CvItemFile;
import org.geniusSociety.codelooms.domain.entity.CvTask;
import org.geniusSociety.codelooms.domain.entity.CvTaskStage;
import org.geniusSociety.codelooms.domain.vo.TaskVO;
import org.geniusSociety.codelooms.mapper.TaskMapper;
import org.geniusSociety.codelooms.mapper.TaskStageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Cealus Li 2025/7/27
 */
@Service
public class TaskService extends BaseService<TaskVO, CvTask> {

    @Autowired
    private TaskManager taskManager;
    @Autowired
    private CvTaskRepository taskRepository;
    @Autowired
    private CvItemRepository itemRepository;
    @Autowired
    private CvTaskStageRepository taskStageRepository;
    @Autowired
    private CvItemFileRepository itemFileRepository;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskStageMapper taskStageMapper;

    @Override
    protected TaskVO detail(CvTask record, TaskVO vo) {
        List<CvTaskStage> stages = taskStageRepository.findAll((root, q, cb) ->
                cb.equal(root.get("taskId"), record.getId()));
        vo.setStages(stages.stream().map(s -> taskStageMapper.domainToVo(s)).toList());
        return vo;
    }

    /**
     * 重置任务
     *
     * @param itemId 项目ID
     * @param stage  任务阶段
     * @param fileId 文件ID
     */
    public void renewTask(final Long itemId, final Integer stage, final Long fileId) {
        Optional<CvItem> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            CvItem item = itemOptional.get();
            Optional<CvTask> taskOptional = taskRepository.findOne((root, q, cb) ->
                    cb.equal(root.get("itemId"), itemId));
            if (taskOptional.isPresent()) {
                CvTask task = taskOptional.get();
                if (null != fileId) {
                    // 重置文件任务
                    Optional<CvItemFile> optional = itemFileRepository.findById(fileId);
                    if (optional.isPresent()) {
                        CvItemFile file = optional.get();
                        List<CvTaskStage> stages = taskStageRepository.findAll((root, q, cb) ->
                                cb.equal(root.get("taskId"), task.getId()));
                        stages.forEach(s -> s.setStatus(EntityType.TaskStaus.NEW));
                        file.setStage(EntityType.TaskStaus.NEW);
                        task.setSteps(EntityType.TaskStage.READY);
                        task.setStatus(EntityType.TaskStaus.RUNNING);
                        taskStageRepository.saveAll(stages);
                        taskRepository.save(task);
                        itemFileRepository.save(file);
                    }
                } else if (null != stage) {
                    //重置阶段任务
                    Optional<CvTaskStage> optional = taskStageRepository.findOne((root, q, cb) ->
                            cb.and(cb.equal(root.get("taskId"), task.getId()), cb.equal(root.get("stage"), stage)));
                    if (optional.isPresent()) {
                        CvTaskStage s = optional.get();
                        s.setStatus(EntityType.TaskStaus.NEW);
                        task.setSteps(s.getStage());
                        task.setStatus(EntityType.TaskStaus.RUNNING);
                        taskStageRepository.save(s);
                    }
                } else {
                    //全部重置
                    List<CvTaskStage> stages = taskStageRepository.findAll((root, q, cb) ->
                            cb.equal(root.get("taskId"), task.getId()));
                    stages.forEach(s -> s.setStatus(EntityType.TaskStaus.NEW));
                    task.setSteps(EntityType.TaskStage.READY);
                    task.setStatus(EntityType.TaskStaus.NEW);
                    taskStageRepository.saveAll(stages);
                    item.setStatus(EntityType.TaskStaus.NEW);
                }
                taskRepository.save(task);
                itemRepository.save(item);
                taskManager.execute(task);
            }
        }
    }


    @Override
    protected JpaSpecificationExecutor<CvTask> getRepository() {
        return taskRepository;
    }

    @Override
    protected BaseMapper<TaskVO, CvTask> getMapper() {
        return taskMapper;
    }
}
