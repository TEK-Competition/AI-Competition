/**
 * Cealus Li 2025/7/28
 * Copyright
 */
package org.geniusSociety.codelooms.service;


import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.common.base.BaseService;
import org.geniusSociety.codelooms.dao.CvItemKnowledgeRepository;
import org.geniusSociety.codelooms.domain.entity.CvItemKnowledge;
import org.geniusSociety.codelooms.domain.vo.FileVO;
import org.geniusSociety.codelooms.mapper.ItemKnowledgeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

/**
 * 知识库
 *
 * @author Cealus Li 2025/7/28
 */
@Service
public class ItemKnowledgeService extends BaseService<FileVO, CvItemKnowledge> {

    @Autowired
    private CvItemKnowledgeRepository itemKnowledgeRepository;
    @Autowired
    private ItemKnowledgeMapper itemKnowledgeMapper;

    @Override
    protected JpaSpecificationExecutor<CvItemKnowledge> getRepository() {
        return itemKnowledgeRepository;
    }

    @Override
    protected BaseMapper<FileVO, CvItemKnowledge> getMapper() {
        return itemKnowledgeMapper;
    }
}
