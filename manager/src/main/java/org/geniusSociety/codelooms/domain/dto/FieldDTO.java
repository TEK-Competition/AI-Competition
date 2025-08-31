package org.geniusSociety.codelooms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldDTO {
    // 字段
    private String column;
    // 数据类型
    private String dataType;
}
