/**
 * Cealus Li 2025/7/12
 * Copyright
 */
package org.geniusSociety.codelooms.component;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.geniusSociety.codelooms.common.constant.EntityType;
import org.geniusSociety.codelooms.common.util.DecompressUtil;
import org.geniusSociety.codelooms.domain.dto.FileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 文件管理
 *
 * @author Cealus Li
 * @date 2025/7/12
 */
@Slf4j
@Component
public class FileComponent {

    @Value("${config.file.save-mode}")
    private String mode;
    @Value("${config.file.base-path}")
    private String basePath;

    private static final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    /**
     * 保存文件
     *
     * @param srcfile
     * @param userId
     * @return
     * @throws IOException
     */
    public FileDTO save(final MultipartFile srcfile, final Integer userId) throws IOException {
        final InputStream in = srcfile.getInputStream();
        final String fileType = FileUtil.getSuffix(srcfile.getOriginalFilename()).toLowerCase();
        final String id = snowflake.nextIdStr();
        final StrBuilder builder = new StrBuilder();
        builder.append(basePath).append(File.separator).append(userId).append(File.separator);
        String path = builder.toString();
        builder.reset();
        builder.append(id).append(".").append(fileType);
        String fileName = builder.toString();
        builder.reset();
        builder.append(path).append(fileName);
        final String filePath = builder.toString();
        final File file = FileUtil.writeFromStream(in, filePath);
        log.info(file.getAbsolutePath());
        builder.reset();
        final FileDTO record = FileDTO.builder().fileName(srcfile.getOriginalFilename()).filePath(filePath).fileType(fileType)
                .saveMode(mode).build();
        builder.append(path).append(File.separator).append(id);
        path = builder.toString();
        builder.reset();
        List<File> files = null;
        if (EntityType.FileType.ZIP.equalsIgnoreCase(fileType)) {
            files = DecompressUtil.decompressZip(file, path);
        } else if (EntityType.FileType.TAR_GIZ.equalsIgnoreCase(fileType)) {
            files = DecompressUtil.decompressTarGz(file, path);
        } else if (EntityType.FileType.GIZ.equalsIgnoreCase(fileType)) {
            files = DecompressUtil.decompressTgz(file, path);
        }
        if (CollectionUtil.isNotEmpty(files)) {
            record.setFiles(files.stream().map(f -> FileDTO.builder().fileName(f.getName()).filePath(f.getAbsolutePath())
                    .fileType(FileUtil.getType(f).toLowerCase()).saveMode(mode).build()).toList());
        }
        return record;
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @param saveMode
     */
    public void remove(final String filePath, String saveMode) {
        FileUtil.del(filePath);
    }
}
