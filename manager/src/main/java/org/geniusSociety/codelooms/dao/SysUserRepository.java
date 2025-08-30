/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.dao;

import org.geniusSociety.codelooms.common.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Cealus Li
 * @date 2025/7/9
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Integer> {

}
