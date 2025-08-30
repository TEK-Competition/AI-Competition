/**
 * Cealus Li 2025/7/12
 * Copyright
 */
package org.geniusSociety.codelooms.controller;

import cn.hutool.core.io.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.geniusSociety.codelooms.common.base.BaseController;
import org.geniusSociety.codelooms.common.constant.EntityType;
import org.geniusSociety.codelooms.common.constant.ErrorCode;
import org.geniusSociety.codelooms.common.constant.WebConstant;
import org.geniusSociety.codelooms.common.dto.BaseResult;
import org.geniusSociety.codelooms.common.vo.CommonResultVO;
import org.geniusSociety.codelooms.domain.vo.FileVO;
import org.geniusSociety.codelooms.service.FileManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件管理
 *
 * @author Cealus Li
 * @date 2025/7/12
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping(WebConstant.SysPath.FILE)
public class FileManagerController extends BaseController {

    @Autowired
    private FileManagerService fileService;

    /**
     * 上传
     *
     * @param file
     * @return
     */
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", method = "POST")
    public CommonResultVO<FileVO> upload(
            @Parameter(description = "上传的文件", required = true, schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file) throws IOException {
        final String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if (null != fileType && EntityType.FileType.TYPES.contains(fileType.toLowerCase())) {
            return CommonResultVO.success(fileService.create(file, this.getUserId()));
        }
        return CommonResultVO.fail(ErrorCode.FILE_TYPE_FORBIDDEN);
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
        fileService.remove(id, this.getUserId());
        return CommonResultVO.success();
    }
}
