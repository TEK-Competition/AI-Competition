/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.dao;

import org.geniusSociety.codelooms.domain.entity.CvItemKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 知识库
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Repository
public interface CvItemKnowledgeRepository extends JpaRepository<CvItemKnowledge, Long>, JpaSpecificationExecutor<CvItemKnowledge> {

}
