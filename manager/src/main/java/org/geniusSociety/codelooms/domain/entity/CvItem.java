/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.geniusSociety.codelooms.common.entity.BaseEntity;

/**
 * 转换项目
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
@Table(name = "cv_item")
public class CvItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    protected Long id;
    //项目名
    @Column(name = "name", nullable = false, length = 64)
    private String name;
    // 执行模式
    @Column(name = "type", nullable = false)
    private Integer type;
    // 执行状态
    @Column(name = "status", nullable = false)
    private Integer status;
    // 表关系
    @Column(name = "table_relation", length = 4096)
    private String tableRelation;
    // 文件关系
    @Column(name = "sp_relation", length = 4096)
    private String spRelation;
    // 说明
    @Column(name = "remark", length = 2048)
    private String remark;
    // 用户ID
    @Column(name = "user_id")
    protected Integer userId;
}

