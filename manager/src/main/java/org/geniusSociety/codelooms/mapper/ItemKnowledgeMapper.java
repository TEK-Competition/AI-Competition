/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.mapper;

import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.domain.entity.CvItemKnowledge;
import org.geniusSociety.codelooms.domain.vo.FileVO;
import org.mapstruct.Mapper;

/**
 * 实体转换
 *
 * @author Cealus Li
 * @date 2025/7/28
 */
@Mapper(componentModel = "spring")
public interface ItemKnowledgeMapper extends BaseMapper<FileVO, CvItemKnowledge> {

}
