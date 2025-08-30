package org.geniusSociety.codelooms.common.bo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页查询
 *
 * @author Cealus
 * @date 2025/7/2
 */
@Setter
@Getter
@Schema(description = "分页查询对象")
public class PageQuery extends BaseQuery {

    @Schema(title = "页码", example = "1")
    protected Integer pageNum;
    @Schema(title = "分页大小", example = "10")
    protected Integer pageSize;

    @Schema(hidden = true)
    public int getStartRows() {
        if (null == pageNum) {
            pageNum = 1;
        }
        if (null == pageSize) {
            pageSize = 10;
        }
        return (this.pageNum - 1) * this.pageSize;
    }
}
