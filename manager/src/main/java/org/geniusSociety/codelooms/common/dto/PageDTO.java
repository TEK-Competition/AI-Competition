package org.geniusSociety.codelooms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * 页对象
 *
 * @author Cealus
 * @date 2025/7/2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class PageDTO<T> {

    /**
     * 对象列表
     */
    private List<T> list;
    /**
     * 数据总量
     */
    private Long totalElements;
    /**
     * 页大小
     */
    private Integer size;

    public static PageDTO toPage(List list, Long total, Integer size) {
        return new PageDTO(list, total, size);
    }
}
