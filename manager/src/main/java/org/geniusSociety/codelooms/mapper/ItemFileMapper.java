/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.mapper;

import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.domain.entity.CvItemFile;
import org.geniusSociety.codelooms.domain.vo.FileVO;
import org.mapstruct.Mapper;

/**
 * 项目文件实体转换
 *
 * @author Cealus Li
 * @date 2025/7/15
 */
@Mapper(componentModel = "spring")
public interface ItemFileMapper extends BaseMapper<FileVO, CvItemFile> {

}
