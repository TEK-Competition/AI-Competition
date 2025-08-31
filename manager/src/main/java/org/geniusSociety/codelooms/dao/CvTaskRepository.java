/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.dao;

import org.geniusSociety.codelooms.domain.entity.CvTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 任务信息
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Repository
public interface CvTaskRepository extends JpaRepository<CvTask, Long>, JpaSpecificationExecutor<CvTask> {

}
