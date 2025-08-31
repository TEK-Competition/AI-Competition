/**
 * Cealus Li 2025/8/4
 * Copyright
 */
package org.geniusSociety.codelooms.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 介绍查询
 *
 * @author Cealus Li 2025/8/4
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExegesisQuestionDTO {
    // ID
    private Long id;
    // 名称
    private String name;
    // 信息列表
    private String list;
}
