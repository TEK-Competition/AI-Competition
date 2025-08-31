/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.dao;

import org.geniusSociety.codelooms.domain.entity.CvItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 项目管理
 * @author Cealus Li
 * @date 2025/7/9
 */
@Repository
public interface CvItemRepository extends JpaRepository<CvItem, Long>, JpaSpecificationExecutor<CvItem> {

}
