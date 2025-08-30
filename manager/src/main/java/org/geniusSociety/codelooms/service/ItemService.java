/**
 * Cealus Li 2025/7/6
 * Copyright
 */
package org.geniusSociety.codelooms.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.common.base.BaseService;
import org.geniusSociety.codelooms.common.constant.BaseConstant;
import org.geniusSociety.codelooms.common.constant.EntityType;
import org.geniusSociety.codelooms.common.constant.ErrorCode;
import org.geniusSociety.codelooms.common.util.AssertUtil;
import org.geniusSociety.codelooms.component.FileComponent;
import org.geniusSociety.codelooms.component.TaskManager;
import org.geniusSociety.codelooms.dao.CvFileRepository;
import org.geniusSociety.codelooms.dao.CvItemFileRepository;
import org.geniusSociety.codelooms.dao.CvItemKnowledgeRepository;
import org.geniusSociety.codelooms.dao.CvItemRepository;
import org.geniusSociety.codelooms.domain.bo.ItemCreateBO;
import org.geniusSociety.codelooms.domain.bo.ItemUpdateBO;
import org.geniusSociety.codelooms.domain.entity.CvFile;
import org.geniusSociety.codelooms.domain.entity.CvItem;
import org.geniusSociety.codelooms.domain.entity.CvItemFile;
import org.geniusSociety.codelooms.domain.entity.CvItemKnowledge;
import org.geniusSociety.codelooms.domain.vo.ItemVO;
import org.geniusSociety.codelooms.domain.vo.RelationVO;
import org.geniusSociety.codelooms.mapper.ItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 转换项目
 *
 * @author Cealus Li
 * @date 2025/7/6
 */
@Service
public class ItemService extends BaseService<ItemVO, CvItem> {

    @Autowired
    private TaskManager taskManager;
    @Autowired
    private FileComponent fileComponent;
    @Autowired
    private CvItemRepository itemRepository;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private CvFileRepository fileRepository;
    @Autowired
    private CvItemFileRepository itemFileRepository;
    @Autowired
    private CvItemKnowledgeRepository itemKnowledgeRepository;

    @Override
    protected ItemVO detail(CvItem record, ItemVO vo) {
        if (StrUtil.isNotEmpty(record.getSpRelation())) {
            vo.setSpGraph(JSONUtil.toList(record.getSpRelation(), RelationVO.class));
        }
        if (StrUtil.isNotEmpty(record.getTableRelation())) {
            vo.setTableGraph(JSONUtil.toList(record.getTableRelation(), RelationVO.class));
        }
        return vo;
    }

    /**
     * 创建
     *
     * @param record
     * @param userId
     * @return
     */
    public Long create(final ItemCreateBO record, final Integer userId) {
        // 存储过程处理
        List<CvFile> files = fileRepository.findAllById(record.getSpFiles());
        AssertUtil.isTrue(CollectionUtil.isNotEmpty(files), ErrorCode.REQUEST_NOT_FOUND.getCode(), "存储过程文件" + ErrorCode.REQUEST_NOT_FOUND.getDesc());
        CvItem item = itemRepository.save(CvItem.builder().name(record.getName()).remark(record.getRemark()).type(record.getMode())
                .userId(userId).status(EntityType.TaskStaus.NEW).build());
        final Long id = item.getId();
        final List<CvItemFile> rels = new ArrayList<>();
        for (CvFile file : files) {
            if (EntityType.FileType.COMPRESS_TYPES.contains(file.getType())) {
                List<CvFile> list = fileRepository.findAll((root, q, cb)
                        -> cb.equal(root.get("parent"), file.getId()));
                list.stream().filter(f -> EntityType.FileType.SQL.equals(f.getType())).forEach(f -> {
                    rels.add(CvItemFile.builder().itemId(id).name(f.getName()).spPath(f.getPath()).stage(EntityType.TaskStaus.NEW)
                            .userId(userId).saveMode(file.getSaveMode()).isDel(BaseConstant.NO).build());
                    f.setItemId(id);
                    fileRepository.save(f);
                });
            } else if (EntityType.FileType.SQL.equals(file.getType())) {
                rels.add(CvItemFile.builder().itemId(id).name(file.getName()).spPath(file.getPath()).stage(EntityType.TaskStaus.NEW)
                        .saveMode(file.getSaveMode()).isDel(BaseConstant.NO).userId(userId).build());
            }
            file.setItemId(id);
            fileRepository.save(file);
        }
        AssertUtil.isTrue(CollectionUtil.isNotEmpty(rels), ErrorCode.ARGUMENTS_LOST_ERROR.getCode(), "缺失必要存储过程文件");
        itemFileRepository.saveAll(rels);
        // 知识库处理
        if (CollectionUtil.isNotEmpty(record.getKnowledgeFiles())) {
            files = fileRepository.findAllById(record.getKnowledgeFiles());
            for (CvFile file : files) {
                if (EntityType.FileType.COMPRESS_TYPES.contains(file.getType())) {
                    List<CvFile> list = fileRepository.findAll((root, q, cb)
                            -> cb.equal(root.get("parent"), file.getId()));
                    list.stream().filter(f -> EntityType.FileType.DOCUMENT_TYPES.contains(file.getType()))
                            .forEach(f -> itemKnowledgeRepository.save(CvItemKnowledge.builder().itemId(id).name(file.getName()).path(file.getPath())
                                    .userId(userId).isDel(BaseConstant.NO).build()));
                } else if (EntityType.FileType.DOCUMENT_TYPES.contains(file.getType())) {
                    itemKnowledgeRepository.save(CvItemKnowledge.builder().itemId(id).name(file.getName()).path(file.getPath())
                            .userId(userId).isDel(BaseConstant.NO).build());
                }
            }
        }
        taskManager.createTask(item);
        return item.getId();
    }

    /**
     * 更新
     *
     * @param record
     * @param userId
     */
    public void update(final ItemUpdateBO record, final Integer userId) {
        Optional<CvItem> optional = itemRepository.findById(record.getId());
        if (optional.isPresent()) {
            CvItem item = optional.get();
            // 验证
            this.assertUser(item.getUserId(), userId);

            if (null != record.getMode()) {
                item.setType(record.getMode());
            }
            if (StrUtil.isNotEmpty(record.getRemark())) {
                item.setRemark(record.getRemark());
            }
            itemRepository.save(item);
        }
    }

    /**
     * 删除
     *
     * @param id
     * @param userId
     */
    @Override
    public void delete(Long id, Integer userId) {
        Optional<CvItem> optional = itemRepository.findById(id);
        if (optional.isPresent()) {
            CvItem item = optional.get();
            // 验证
            this.assertUser(item.getUserId(), userId);

            List<CvFile> list = fileRepository.findAll((root, q, cb)
                    -> cb.equal(root.get("itemId"), id));
            for (CvFile f : list) {
                fileRepository.deleteById(f.getId());
                fileComponent.remove(f.getPath(), f.getSaveMode());
            }
            itemFileRepository.delete((root, q, cb)
                    -> cb.equal(root.get("itemId"), id));
        }
        itemRepository.deleteById(id);
    }

    @Override
    protected JpaSpecificationExecutor<CvItem> getRepository() {
        return itemRepository;
    }

    @Override
    protected BaseMapper<ItemVO, CvItem> getMapper() {
        return itemMapper;
    }
}
