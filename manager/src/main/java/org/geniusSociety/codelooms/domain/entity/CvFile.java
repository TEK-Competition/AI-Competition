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
@Table(name = "cv_file")
public class CvFile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    //上层文件
    @Column(name = "parent")
    private Long parent;
    // 项目ID
    @Column(name = "item_id")
    private Long itemId;
    // 文件名
    @Column(name = "name", nullable = false, length = 128)
    private String name;
    // 文件路径
    @Column(name = "path", nullable = false, length = 256)
    private String path;
    // 文件类型
    @Column(name = "type")
    private String type;
    // 存储方式
    @Column(name = "save_mode")
    private String saveMode;
    // 用户ID
    @Column(name = "user_id")
    private Integer userId;
}

