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

    private Integer id;
    private String name;
    private Integer roleId;
}
