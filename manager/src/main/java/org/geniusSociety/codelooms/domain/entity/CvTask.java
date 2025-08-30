/**
 * Cealus Li 2025/7/9
 * Copyright
 */
package org.geniusSociety.codelooms.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.geniusSociety.codelooms.common.entity.BaseEntity;

import java.util.Date;

/**
 * 转换任务
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
@Table(name = "cv_task")
public class CvTask extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;
    //项目名
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "type")
    private Integer type;

    @Column(name = "status")
    private Integer status;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "finish_time")
    private Date finishTime;
    // 用户ID
    @Column(name = "user_id")
    private Integer userId;

}

