/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.mapper;

import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.domain.entity.MateTable;
import org.geniusSociety.codelooms.domain.vo.TableVO;
import org.mapstruct.Mapper;

/**
 * 项目表实体转换
 *
 * @author Cealus Li
 * @date 2025/7/15
 */
@Mapper(componentModel = "spring")
public interface TableMapper extends BaseMapper<TableVO, MateTable> {

}
