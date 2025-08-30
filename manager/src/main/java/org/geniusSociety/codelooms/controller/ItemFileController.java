/**
 * Cealus Li 2025/7/26
 * Copyright
 */
package org.geniusSociety.codelooms.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.geniusSociety.codelooms.common.base.BaseAction;
import org.geniusSociety.codelooms.common.base.IService;
import org.geniusSociety.codelooms.common.constant.WebConstant;
import org.geniusSociety.codelooms.domain.vo.FileVO;
import org.geniusSociety.codelooms.service.ItemFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目文件
 *
 * @author Cealus Li 2025/7/26
 */
@Tag(name = "项目文件")
@RestController
@RequestMapping(WebConstant.ItemPath.FILE)
public class ItemFileController extends BaseAction<FileVO> {

    @Autowired
    private ItemFileService itemFileService;

    @Override
    protected IService<FileVO> getService() {
        return itemFileService;
    }

}
