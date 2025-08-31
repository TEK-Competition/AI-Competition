/**
 * Cealus Li 2025/7/28
 * Copyright
 */
package org.geniusSociety.codelooms.component;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.geniusSociety.codelooms.common.constant.BaseConstant;
import org.geniusSociety.codelooms.common.constant.EntityType;
import org.geniusSociety.codelooms.dao.*;
import org.geniusSociety.codelooms.domain.dto.AnswerDTO;
import org.geniusSociety.codelooms.domain.dto.ExegesisQuestionDTO;
import org.geniusSociety.codelooms.domain.dto.QuestionDTO;
import org.geniusSociety.codelooms.domain.entity.*;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务执行
 *
 * @author Cealus Li 2025/7/28
 */
@Slf4j
public class TaskWorker implements Runnable {

    // 任务ID
    private final Long taskId;
    // 项目ID
    private final Long itemId;
    // 项目名称
    private final String itemName;
    // 用户ID
    private final Integer userId;
    // 表字段
    private final Map<Long, List<MateTableField>> tableFieldMap = new HashMap<>();
    // spring上下文
    private final ApplicationContext context;

    private final CvTaskStageRepository taskStageRepository;
    private final CvItemFileRepository itemFileRepository;
    private final CodeloomsFeignClient feignClient;

    // 存储过程文件
    private final List<CvItemFile> files;

    public TaskWorker(final CvTask task, ApplicationContext context) {
        this.context = context;
        this.taskStageRepository = context.getBean(CvTaskStageRepository.class);
        this.itemFileRepository = context.getBean(CvItemFileRepository.class);
        this.feignClient = context.getBean(CodeloomsFeignClient.class);
        this.taskId = task.getId();
        this.itemId = task.getItemId();
        this.itemName = task.getName();
        this.userId = task.getUserId();
        files = itemFileRepository.findAll((root, q, cb) ->
                cb.and(cb.equal(root.get("itemId"), itemId)));
    }

    @Override
    public void run() {
        CvTaskRepository taskRepository = context.getBean(CvTaskRepository.class);
        CvItemRepository itemRepository = context.getBean(CvItemRepository.class);

        final CvTask task = taskRepository.findById(taskId).get();
        task.setStatus(EntityType.TaskStaus.RUNNING);
        task.setStartTime(new Date());
        taskRepository.save(task);
        // 任务阶段
        final Map<Integer, CvTaskStage> stages = taskStageRepository.findAll((root, q, cb) ->
                        cb.and(cb.equal(root.get("taskId"), task.getId()))).stream()
                .collect(Collectors.toMap(CvTaskStage::getStage, Function.identity()));
        try {
            for (Integer stage : EntityType.TaskStage.stages) {
                CvTaskStage step = stages.get(stage);
                if (!EntityType.TaskStaus.FINISH.equals(step.getStatus())) {
                    if (EntityType.TaskStage.READY.equals(stage)) {
                        // 知识库加载
                        this.loadKnowledge();
                    } else if (EntityType.TaskStage.TABLE_RELATION.equals(stage)) {
                        // 表关系任务
                        this.execute(step, this.relation());
                        this.generateRelation();
                    } else if (EntityType.TaskStage.TABLE_FIELD.equals(stage)) {
                        // 字段提取
                        this.execute(step, this.field());
                        this.generateMeta();
                    } else if (EntityType.TaskStage.CONVERSION.equals(stage)) {
                        // 存储过程转换
                        this.execute(step, this.conversion());
                    } else if (EntityType.TaskStage.EXEGESIS.equals(stage)) {
                        // 字段注释
                        this.execute(step, this.exegesis());
                    } else if (EntityType.TaskStage.FINISH.equals(stage)) {
                        // 结束
                        files.forEach(file -> file.setStage(EntityType.TaskStage.FINISH));
                        itemFileRepository.saveAll(files);
                        step.setStatus(EntityType.TaskStaus.FINISH);
                        step.setFinishTime(new Date());
                        taskStageRepository.save(step);
                    }
                }
            }
            task.setStatus(EntityType.TaskStaus.FINISH);
        } catch (Exception e) {
            log.error(e.getMessage());
            task.setStatus(EntityType.TaskStaus.FAIL);
        }
        taskRepository.save(task);
        CvItem item = itemRepository.findById(this.itemId).get();
        item.setStatus(EntityType.TaskStaus.FINISH);
        itemRepository.save(item);
    }

    /**
     * 任务执行
     *
     * @param step     步骤
     * @param function 方法
     */
    private void execute(CvTaskStage step, Consumer<CvItemFile> function) {
        step.setStartTime(new Date());
        step.setStatus(EntityType.TaskStaus.RUNNING);
        taskStageRepository.save(step);
        try {
            files.forEach(f -> {
                function.accept(f);
                f.setStage(step.getStage());
                itemFileRepository.save(f);
            });
            step.setStatus(EntityType.TaskStaus.FINISH);
            step.setFinishTime(new Date());
        } catch (Exception e) {
            log.error(step.toString(), e);
            step.setStatus(EntityType.TaskStaus.FAIL);
        }
        taskStageRepository.save(step);
    }

    /**
     * 表关系提取
     *
     * @return
     */
    private Consumer<CvItemFile> relation() {
        return file -> {
            ThreadUtil.safeSleep(1000);
            String sp = FileUtil.readString(file.getSpPath(), CharsetUtil.CHARSET_UTF_8);
            QuestionDTO question = QuestionDTO.builder().id(this.taskId).sp(sp).build();
            log.info(question.toString());
            AnswerDTO answer = feignClient.relation(question);
            JSONArray json = JSONUtil.parseArray(answer.getData());
            file.setRelation(json.toString());
            itemFileRepository.save(file);
        };
    }

    /**
     * 关系生成
     */
    private void generateRelation() {
        CvItemRepository itemRepository = context.getBean(CvItemRepository.class);
        final StrBuilder builder = new StrBuilder();
        // 表关系生成
        for (CvItemFile file : files) {
            builder.append(file.getRelation()).append(";");
        }
        CvItem item = itemRepository.findById(this.itemId).get();
        QuestionDTO question = QuestionDTO.builder().id(this.taskId).sp(builder.toString()).build();
        log.info(question.toString());
        AnswerDTO answer = feignClient.graph(question);
        item.setTableRelation(this.pretreatmentRelation(answer.getData()));
        log.info(item.getTableRelation());
        builder.reset();
        // 文件关系生成
        for (CvItemFile file : files) {
            builder.append("\"file\"").append(file.getName()).append(",").append(file.getRelation()).append(";");
        }
        question = QuestionDTO.builder().id(this.taskId).sp(builder.toString()).build();
        log.info(question.toString());
        answer = feignClient.process(question);
        item.setSpRelation(this.pretreatmentRelation(answer.getData()));
        log.info(item.getSpRelation());
        itemRepository.save(item);
    }

    /**
     * 关系预处理
     *
     * @return
     */
    private String pretreatmentRelation(String relation) {
        JSONArray arr = JSONUtil.parseArray(relation);
        for (int i = 0; i < arr.size(); ++i) {
            JSONObject obj = arr.getJSONObject(i);
            String from = obj.getStr("from");
            if (BaseConstant.NONE.equalsIgnoreCase(from)) {
                obj.set("from", BaseConstant.START);
            }
            String to = obj.getStr("to");
            if (BaseConstant.NONE.equalsIgnoreCase(to)) {
                obj.set("to", BaseConstant.END);
            }
        }
        return arr.toString();
    }

    /**
     * 字段处理
     *
     * @return
     */
    private Consumer<CvItemFile> field() {
        final CvTableRepository tableRepository = this.context.getBean(CvTableRepository.class);
        final CvTableFieldRepository tableFieldRepository = this.context.getBean(CvTableFieldRepository.class);
        return file -> {
            ThreadUtil.safeSleep(1000);
            String sp = FileUtil.readString(file.getSpPath(), CharsetUtil.CHARSET_UTF_8);
            QuestionDTO question = QuestionDTO.builder().id(this.taskId).sp(sp).build();
            log.info(question.toString());
            AnswerDTO answer = feignClient.field(question);
            log.info(answer.getData());
            JSONArray array = JSONUtil.parseArray(answer.getData());
            for (int i = 0; i < array.size(); ++i) {
                JSONObject obj = array.getJSONObject(i);
                String tableName = obj.getStr("table");
                CvTable table = CvTable.builder().taskId(this.taskId).name(tableName).userId(userId).build();
                table = tableRepository.save(table);
                JSONArray fields = obj.getJSONArray("fields");
                for (int j = 0; j < fields.size(); ++j) {
                    JSONObject field = fields.getJSONObject(j);
                    tableFieldRepository.save(CvTableField.builder().tableId(table.getId())
                            .name(field.getStr("name")).dataType(field.getStr("dataType")).build());
                }
            }
        };
    }

    /**
     * 表信息生成
     */
    private void generateMeta() {
        final CvTableRepository tableRepository = this.context.getBean(CvTableRepository.class);
        final CvTableFieldRepository tableFieldRepository = this.context.getBean(CvTableFieldRepository.class);
        final Map<Long, CvTable> tableMap = tableRepository.findAll((root, q, cb) ->
                cb.equal(root.get("taskId"), this.taskId)).stream().collect(Collectors.toMap(CvTable::getId, Function.identity()));
        final List<CvTableField> fields = tableFieldRepository.findAll((root, q, cb) ->
                root.get("tableId").in(tableMap.keySet()));

        final Map<String, Map<String, CvTableField>> metaMap = new HashMap<>();
        for (CvTableField field : fields) {
            CvTable table = tableMap.get(field.getTableId());
            Map<String, CvTableField> map = metaMap.computeIfAbsent(table.getName(), k -> new HashMap<>());
            map.putIfAbsent(field.getName(), field);
        }
        // 保存元数据
        final MateTableRepository tableDao = this.context.getBean(MateTableRepository.class);
        final MateTableFieldRepository tableFieldDao = this.context.getBean(MateTableFieldRepository.class);
        for (Map.Entry<String, Map<String, CvTableField>> entry : metaMap.entrySet()) {
            String tableNmae = entry.getKey();
            MateTable table = tableDao.saveAndFlush(MateTable.builder().itemId(this.taskId).name(tableNmae).userId(userId).build());
            List<MateTableField> mateTableFields = new ArrayList<>();
            for (Map.Entry<String, CvTableField> fieldEntry : entry.getValue().entrySet()) {
                CvTableField field = fieldEntry.getValue();
                MateTableField mateTableField = MateTableField.builder().tableId(table.getId()).name(field.getName())
                        .userId(userId).dataType(field.getDataType()).build();
                tableFieldDao.saveAndFlush(mateTableField);
                mateTableFields.add(mateTableField);
            }
            tableFieldMap.put(table.getId(), mateTableFields);
        }
    }

    /**
     * SQL转换
     *
     * @return
     */
    private Consumer<CvItemFile> conversion() {
        return f -> {
            ThreadUtil.safeSleep(1000);
            File file = new File(f.getSpPath());
            String name = FileUtil.getName(file);
            String sp = FileUtil.readString(file, CharsetUtil.CHARSET_UTF_8);
            QuestionDTO question = QuestionDTO.builder().id(this.taskId).sp(sp).build();
            log.info(question.toString());
            AnswerDTO answer = feignClient.conversion(question);
            String path = file.getParentFile().getParentFile().getAbsolutePath() + File.separator + name;
            FileUtil.writeString(answer.getData(), path, CharsetUtil.CHARSET_UTF_8);
            f.setSqlPath(path);
            itemFileRepository.save(f);
        };
    }

    /**
     * 字段注释
     *
     * @return
     */
    private Consumer<CvItemFile> exegesis() {
        return file -> {
            ThreadUtil.safeSleep(1000);
            if (tableFieldMap.isEmpty()) {
                log.error("Please execute TABLE_FIELD step first");
            } else {
                final MateTableRepository tableDao = this.context.getBean(MateTableRepository.class);
                final MateTableFieldRepository tableFieldDao = this.context.getBean(MateTableFieldRepository.class);
                tableFieldMap.keySet().forEach(tableId -> {
                    Optional<MateTable> optional = tableDao.findById(tableId);
                    if (optional.isPresent()) {
                        StringBuilder builder = new StringBuilder();
                        List<MateTableField> dbMateTableFields = tableFieldMap.get(tableId);
                        dbMateTableFields.forEach(tableField -> {
                            builder.append(tableField.getName()).append("(").append(tableField.getDataType()).append("),");
                        });
                        ExegesisQuestionDTO question = ExegesisQuestionDTO.builder().id(this.taskId).name(optional.get().getName())
                                .list(builder.toString()).build();
                        log.info(question.toString());
                        AnswerDTO answer = feignClient.exegesis(question);
                        log.info(answer.getData());
                        JSONArray array = JSONUtil.parseArray(answer.getData());
                        for (int i = 0; i < array.size(); ++i) {
                            JSONObject obj = array.getJSONObject(i);
                            String fieldName = obj.getStr("name");
                            dbMateTableFields.forEach(tableField -> {
                                if (tableField.getName().equals(fieldName)) {
                                    tableField.setDescription(obj.getStr("comment"));
                                }
                            });
                            tableFieldDao.saveAll(dbMateTableFields);
                        }
                    }

                });
            }
        };
    }

    /**
     * 知识库加载
     */
    private void loadKnowledge() {
        final CvItemKnowledgeRepository itemKnowledgeRepository = this.context.getBean(CvItemKnowledgeRepository.class);
        List<String> knowledges = itemKnowledgeRepository.findAll((root, q, cb) ->
                        cb.and(cb.equal(root.get("itemId"), this.itemId), cb.equal(root.get("isDel"), BaseConstant.NO))).stream()
                .map(CvItemKnowledge::getPath).toList();
        if (CollectionUtil.isNotEmpty(knowledges)) {
            ExegesisQuestionDTO question = ExegesisQuestionDTO.builder().id(this.taskId).name(itemName)
                    .list(CollectionUtil.join(knowledges, ",")).build();
            feignClient.knowledge(question);
        }
    }
}