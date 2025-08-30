/**
 * Cealus Li 2025/8/8
 * Copyright
 */
package org.geniusSociety.codelooms.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.geniusSociety.codelooms.common.base.BaseAction;
import org.geniusSociety.codelooms.common.base.IService;
import org.geniusSociety.codelooms.common.constant.WebConstant;
import org.geniusSociety.codelooms.domain.vo.TableVO;
import org.geniusSociety.codelooms.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 表信息
 *
 * @author Cealus Li 2025/8/8
 */
@Tag(name = "表信息")
@RestController
@RequestMapping(WebConstant.ItemPath.TABLE)
public class TableController extends BaseAction<TableVO> {

    @Autowired
    private TableService tableService;

    @Override
    protected IService<TableVO> getService() {
        return tableService;
    }
}
