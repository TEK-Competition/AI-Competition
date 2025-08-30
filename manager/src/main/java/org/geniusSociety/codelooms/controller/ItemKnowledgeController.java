/**
 * Cealus Li 2025/7/28
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
 * 知识库
 *
 * @author Cealus Li 2025/7/28
 */
@Tag(name = "知识库")
@RestController
@RequestMapping(WebConstant.ItemPath.KNOWLEDGE)
public class ItemKnowledgeController extends BaseAction<FileVO> {

    @Autowired
    private ItemFileService itemFileService;

    @Override
    protected IService<FileVO> getService() {
        return itemFileService;
    }

}
