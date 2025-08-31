/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.dao;

import org.geniusSociety.codelooms.domain.entity.MateTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 表信息
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Repository
public interface MateTableRepository extends JpaRepository<MateTable, Long>, JpaSpecificationExecutor<MateTable> {

}
