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
 * LLM 返回
 *
 * @author Cealus Li 2025/8/4
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnswerDTO {
    // 状态
    private Integer code;
    // 数据
    private String data;
}
