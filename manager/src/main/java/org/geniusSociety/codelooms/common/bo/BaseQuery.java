package org.geniusSociety.codelooms.common.bo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 基础查询条件
 *
 * @author Cealus
 * @date 2025/7/2
 */
@Setter
@Getter
@Schema(description = "基础查询对象")
public class BaseQuery {

    @Schema(title = "id")
    protected Long id;
    @Schema(title = "名称")
    protected String name;
    @Schema(title = "类型")
    protected Integer type;
}
