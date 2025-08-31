/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.common.base;

/**
 * 基础实体转换接口
 *
 * @author Cealus Li
 * @date 2025/7/15
 */
public interface BaseMapper<VO, BO> {

    /**
     * 实体转换
     *
     * @param record
     * @return
     */
    VO domainToVo(BO record);
}
