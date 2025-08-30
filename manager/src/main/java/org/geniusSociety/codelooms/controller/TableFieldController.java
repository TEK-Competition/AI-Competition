/**
 * Cealus Li 2025/8/8
 * Copyright
 */
package org.geniusSociety.codelooms.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.geniusSociety.codelooms.common.base.BaseAction;
import org.geniusSociety.codelooms.common.base.IService;
import org.geniusSociety.codelooms.common.constant.WebConstant;
import org.geniusSociety.codelooms.domain.vo.TableFieldVO;
import org.geniusSociety.codelooms.service.TableFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 表字段
 *
 * @author Cealus Li 2025/8/8
 */
@Tag(name = "表字段")
@RestController
@RequestMapping(WebConstant.ItemPath.FIELD)
public class TableFieldController extends BaseAction<TableFieldVO> {

    @Autowired
    private TableFieldService tableFieldService;

    @Override
    protected IService<TableFieldVO> getService() {
        return tableFieldService;
    }
}
