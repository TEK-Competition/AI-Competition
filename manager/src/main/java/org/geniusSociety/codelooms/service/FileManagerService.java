/**
 * Cealus Li 2025/7/12
 * Copyright
 */
package org.geniusSociety.codelooms.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import org.geniusSociety.codelooms.common.constant.BaseConstant;
import org.geniusSociety.codelooms.common.constant.EntityType;
import org.geniusSociety.codelooms.common.constant.ErrorCode;
import org.geniusSociety.codelooms.common.util.AssertUtil;
import org.geniusSociety.codelooms.component.FileComponent;
import org.geniusSociety.codelooms.dao.CvFileRepository;
import org.geniusSociety.codelooms.domain.dto.FileDTO;
import org.geniusSociety.codelooms.domain.entity.CvFile;
import org.geniusSociety.codelooms.domain.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 项目文件
 *
 * @author Cealus Li
 * @date 2025/7/12
 */
@Service
public class FileManagerService {

    @Autowired
    private CvFileRepository fileRepository;
    @Autowired
    private FileComponent fileComponent;

    /**
     * 上传文件
     *
     * @param srcfile 文件
     * @param userId  用户ID
     * @return
     * @throws IOException
     */
    public FileVO create(final MultipartFile srcfile, final Integer userId) throws IOException {
        FileDTO file = fileComponent.save(srcfile, userId);
        // 压缩文件
        if (EntityType.FileType.COMPRESS_TYPES.contains(file.getFileType())) {
            // 内容验证
            AssertUtil.isTrue(CollectionUtil.isNotEmpty(file.getFiles()),
                    ErrorCode.FILE_CONTENT_FORBIDDEN.getCode(), ErrorCode.FILE_CONTENT_FORBIDDEN.getDesc());
            AssertUtil.isTrue(file.getFiles().stream().anyMatch(f -> {
                        final String fileType = FileUtil.getSuffix(f.getFileName());
                        return null != fileType && EntityType.FileType.TYPES.contains(fileType.toLowerCase());
                    }),
                    ErrorCode.FILE_CONTENT_FORBIDDEN.getCode(), ErrorCode.FILE_CONTENT_FORBIDDEN.getDesc());
        }
        CvFile record = CvFile.builder().name(file.getFileName()).path(file.getFilePath()).type(file.getFileType())
                .saveMode(file.getSaveMode()).parent(Long.valueOf(BaseConstant.BASE_ID)).userId(userId).build();
        record = fileRepository.save(record);
        final Long id = record.getId();
        fileRepository.saveAll(file.getFiles().stream().map(f ->
                CvFile.builder().parent(id).name(f.getFileName()).path(f.getFilePath())
                        .type(f.getFileType()).saveMode(f.getSaveMode()).userId(userId).build()
        ).toList());
        return FileVO.builder().id(record.getId()).name(record.getName()).build();
    }

    /**
     * 删除
     *
     * @param id     文件ID
     * @param userId 用户ID
     */
    public void remove(final Long id, final Integer userId) {
        Optional<CvFile> file = fileRepository.findById(id);
        // 验证
        AssertUtil.isTrue((file.isPresent() && Objects.equals(file.get().getUserId(), userId)),
                ErrorCode.REQUEST_NOT_FOUND.getCode(), ErrorCode.REQUEST_NOT_FOUND.getDesc());
        CvFile record = file.get();
        fileRepository.deleteById(id);
        fileComponent.remove(record.getPath(), record.getSaveMode());
        if (null != record.getParent() && !BaseConstant.BASE_ID.equals(record.getParent())) {
            List<CvFile> list = fileRepository.findAll((root, q, cb)
                    -> cb.equal(root.get("parent"), id));
            for (CvFile f : list) {
                fileRepository.deleteById(f.getId());
                fileComponent.remove(f.getPath(), f.getSaveMode());
            }
        }
    }
}
