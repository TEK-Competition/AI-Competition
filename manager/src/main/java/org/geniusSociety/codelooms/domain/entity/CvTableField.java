/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表字段
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cv_table_field")
public class CvTableField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    // 表ID
    @Column(name = "table_id", nullable = false)
    private Long tableId;
    // 名称
    @Column(name = "name", length = 128)
    private String name;
    // 数据类型
    @Column(name = "data_type", length = 32)
    private String dataType;
    // 用户ID
    @Column(name = "user_id")
    private Integer userId;
}

