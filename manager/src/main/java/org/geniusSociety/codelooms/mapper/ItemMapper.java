/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.mapper;

import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.domain.entity.CvItem;
import org.geniusSociety.codelooms.domain.vo.ItemVO;
import org.mapstruct.Mapper;

/**
 * 实体转换
 *
 * @author Cealus Li
 * @date 2025/7/15
 */

@Mapper(componentModel = "spring")
public interface ItemMapper extends BaseMapper<ItemVO, CvItem> {

}
