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
 * @author Cealus Li 2025/8/4
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExegesisQuestionDTO {
    private Long id;
    private String name;
    private String list;
}
