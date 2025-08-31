/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.geniusSociety.codelooms.common.entity.BaseEntity;

/**
 * 表信息
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
@Table(name = "mate_table")
public class MateTable extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;
    // 项目ID
    @Column(name = "item_id")
    private Long itemId;
    //项目名
    @Column(name = "name", length = 128)
    private String name;
    // 用户ID
    @Column(name = "user_id")
    protected Integer userId;
}
