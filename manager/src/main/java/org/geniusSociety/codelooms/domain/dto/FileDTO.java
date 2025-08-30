/**
 * Cealus Li 2025/7/12
 * Copyright
 */
package org.geniusSociety.codelooms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件信息
 *
 * @author Cealus Li
 * @date 2025/7/12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {

    //文件名
    private String fileName;
    //文件路径
    private String filePath;
    //文件类型
    private String fileType;
    //存储方式
    private String saveMode;
    // 子文件
    private List<FileDTO> files;


}
