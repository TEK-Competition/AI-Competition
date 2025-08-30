/**
 * Cealus Li 2025/7/6
 * Copyright
 */
package org.geniusSociety.codelooms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.geniusSociety.codelooms.common.base.BaseAction;
import org.geniusSociety.codelooms.common.base.IService;
import org.geniusSociety.codelooms.common.constant.WebConstant;
import org.geniusSociety.codelooms.common.vo.CommonResultVO;
import org.geniusSociety.codelooms.domain.bo.ItemCreateBO;
import org.geniusSociety.codelooms.domain.bo.ItemUpdateBO;
import org.geniusSociety.codelooms.domain.vo.ItemVO;
import org.geniusSociety.codelooms.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 項目管理
 *
 * @author Cealus Li
 * @date 2025/7/6
 */
@Tag(name = "项目管理")
@RestController
@RequestMapping(WebConstant.ItemPath.ITEM)
public class ItemController extends BaseAction<ItemVO> {

    @Autowired
    private ItemService itemService;


    @Override
    protected IService<ItemVO> getService() {
        return itemService;
    }

    /**
     * 创建
     *
     * @param record
     * @return
     */
    @PostMapping(value = "create")
    @Operation(summary = "创建", method = "POST")
    public CommonResultVO<Long> create(@Valid @RequestBody final ItemCreateBO record) {
        return CommonResultVO.success(itemService.create(record, this.getUserId()));
    }

    /**
     * 更新
     *
     * @param record
     * @return
     */
    @PostMapping(value = "update")
    @Operation(summary = "更新", method = "POST")
    public CommonResultVO<Long> update(@Valid @RequestBody final ItemUpdateBO record) {
        itemService.update(record, this.getUserId());
        return CommonResultVO.success(record.getId());
    }
}
