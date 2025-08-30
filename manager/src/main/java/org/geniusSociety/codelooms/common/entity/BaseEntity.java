/**
 * Cealus Li 2025/7/13
 * Copyright
 */
package org.geniusSociety.codelooms.common.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 基础信息
 *
 * @author Cealus Li
 * @date 2025/7/13
 */
@Getter
@Setter
public abstract class BaseEntity {
    // ID
    private Long id;
    // 名称
    private String name;
    // 用户ID
    private Integer userId;
}
