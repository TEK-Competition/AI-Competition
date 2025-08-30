/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.geniusSociety.codelooms.common.entity.BaseEntity;

/**
 * 表字段
 *
 * @author Cealus Li
 * @date 2025/7/9
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mate_table_field")
public class MateTableField extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "data_type", length = 32)
    private String dataType;

    @Column(name = "description", length = 1024)
    private String description;
    // 用户ID
    @Column(name = "user_id")
    private Integer userId;
}

