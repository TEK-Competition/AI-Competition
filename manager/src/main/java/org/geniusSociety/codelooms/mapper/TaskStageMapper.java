/**
 * Cealus Li 2025/7/15
 * Copyright
 */
package org.geniusSociety.codelooms.mapper;

import org.geniusSociety.codelooms.common.base.BaseMapper;
import org.geniusSociety.codelooms.domain.entity.CvTask;
import org.geniusSociety.codelooms.domain.entity.CvTaskStage;
import org.geniusSociety.codelooms.domain.vo.TaskStageVO;
import org.geniusSociety.codelooms.domain.vo.TaskVO;
import org.mapstruct.Mapper;

/**
 * 实体转换
 *
 * @author Cealus Li
 * @date 2025/7/15
 */
@Mapper(componentModel = "spring")
public interface TaskStageMapper extends BaseMapper<TaskStageVO, CvTaskStage> {

}
