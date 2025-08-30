/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.geniusSociety.codelooms.common.entity.BaseEntity;

/**
 * 项目文件
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
@Table(name = "cv_item_file")
public class CvItemFile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    // 项目ID
    @Column(name = "item_id", nullable = false)
    private Long itemId;
    // 文件名
    @Column(name = "name", nullable = false, length = 128)
    private String name;
    // 存储过程文件路径
    @Column(name = "sp_path", nullable = false, length = 256)
    private String spPath;
    // SQL文件路径
    @Column(name = "sql_path", length = 256)
    private String sqlPath;
    // 步骤
    @Column(name = "stage")
    private Integer stage;
    // 存储方式
    @Column(name = "save_mode", length = 64)
    private String saveMode;
    // 关系
    @Column(name = "relation", length = 4096)
    private String relation;
    // 删除：1
    @Column(name = "is_del")
    private Integer isDel;
    // 用户ID
    @Column(name = "user_id")
    private Integer userId;
}

