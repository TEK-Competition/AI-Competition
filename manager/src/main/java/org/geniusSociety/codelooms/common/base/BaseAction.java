/**
 * Cealus Li 2025/7/5
 * Copyright
 */
package org.geniusSociety.codelooms.common.base;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.geniusSociety.codelooms.common.bo.BaseQuery;
import org.geniusSociety.codelooms.common.bo.PageQuery;
import org.geniusSociety.codelooms.common.dto.BaseResult;
import org.geniusSociety.codelooms.common.dto.PageDTO;
import org.geniusSociety.codelooms.common.vo.CommonResultVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础操作
 *
 * @param <VO,BO>
 * @author Cealus Li
 * @date 2025/7/5
 */
public abstract class BaseAction<VO> extends BaseController {

    /**
     * 获取service
     *
     * @return
     */
    protected abstract IService<VO> getService();

    /**
     * 查询
     *
     * @param query
     * @return
     */
    @PostMapping(value = "query")
    @Operation(summary = "查询", method = "POST")
    public CommonResultVO<PageDTO<VO>> query(@RequestBody final PageQuery query) {
        return CommonResultVO.success(this.getService().query(query, this.getUserId()));
    }

    @PostMapping(value = "list")
    @Operation(summary = "查询", method = "POST")
    CommonResultVO<List<VO>> list(@RequestBody final BaseQuery query) {
        return CommonResultVO.success(this.getService().list(query, this.getUserId()));
    }


    /**
     * 根据ID获取
     *
     * @param id
     */
    @GetMapping(value = "detail/{id}")
    @Operation(summary = "获取详情", method = "GET")
    public CommonResultVO<VO> detail(@NotNull(message = "缺少必要数据") @PathVariable("id") Long id) {
        return CommonResultVO.success(this.getService().detail(id, this.getUserId()));
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "delete/{id}")
    @Operation(summary = "删除", method = "DELETE")
    public BaseResult delete(@NotNull(message = "缺少必要数据") @PathVariable("id") final Long id) {
        this.getService().delete(id, this.getUserId());
        return CommonResultVO.success();
    }
}
