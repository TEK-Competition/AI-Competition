/**
 * Cealus Li 2025/7/12
 * Copyright
 */
package org.geniusSociety.codelooms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息
 *
 * @author Cealus Li
 * @date 2025/7/12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    /**
     * 用户ID
     */
    private Integer id;
    /**
     * 用户名称
     */
    private String name;
    /**
     * 用户角色
     */
    private Integer roleId;
}
