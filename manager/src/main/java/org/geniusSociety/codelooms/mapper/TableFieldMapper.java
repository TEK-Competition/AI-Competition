/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.mapper;

import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.domain.entity.MateTableField;
import org.geniusSociety.codelooms.domain.vo.TableFieldVO;
import org.mapstruct.Mapper;

/**
 * 表字段实体转换
 *
 * @author Cealus Li
 * @date 2025/7/15
 */
@Mapper(componentModel = "spring")
public interface TableFieldMapper extends BaseMapper<TableFieldVO, MateTableField> {

}
