/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.dao;

import org.geniusSociety.codelooms.domain.entity.CvFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 系统文件
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Repository
public interface CvFileRepository extends JpaRepository<CvFile, Long>, JpaSpecificationExecutor<CvFile> {

}
