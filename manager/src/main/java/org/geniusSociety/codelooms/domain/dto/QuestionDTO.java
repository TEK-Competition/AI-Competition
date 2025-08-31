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
 * 存储过程转换
 *
 * @author Cealus Li 2025/8/4
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {
    // 项目ID
    private Long id;
    // 存储过程
    private String sp;
}
