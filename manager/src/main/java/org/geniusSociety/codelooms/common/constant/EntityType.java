/**
 * Cealus Li 2025/7/12
 * Copyright
 */
package org.geniusSociety.codelooms.common.constant;

import cn.hutool.core.collection.CollectionUtil;

import java.util.List;
import java.util.Set;

/**
 * 实体属性
 *
 * @author Cealus Li
 * @date 2025/7/12
 */
public class EntityType {

    /**
     * 文件类型
     */
    public static class FileType {
        public static final String ZIP = "zip";
        public static final String TAR_GIZ = "tar.gz";
        public static final String GIZ = "tgz";
        public static final String PDF = "pdf";
        public static final String DOC = "doc";
        public static final String DOCX = "docx";
        public static final String HTML = "html";
        public static final String SQL = "sql";

        public static final Set<String> TYPES = CollectionUtil.newHashSet(ZIP, TAR_GIZ, GIZ, PDF, DOC, DOCX, HTML, SQL);
        public static final Set<String> COMPRESS_TYPES = CollectionUtil.newHashSet(ZIP, TAR_GIZ, GIZ);
        public static final Set<String> DOCUMENT_TYPES = CollectionUtil.newHashSet(PDF, DOC, DOCX, HTML);

    }

    /**
     * 项目状态
     */
    public static class TaskStaus {
        // 新建
        public static final Integer NEW = 1;
        //运行
        public static final Integer RUNNING = 2;
        // 完成
        public static final Integer FINISH = 3;
        // 失败
        public static final Integer FAIL = 4;
    }

    /**
     * 步骤
     */
    public static class TaskStage {
        // 初始化
        public static final Integer READY = 1;
        // 表关系任务
        public static final Integer TABLE_RELATION = 2;
        // 表字段任务
        public static final Integer TABLE_FIELD = 3;
        // 转换任务
        public static final Integer CONVERSION = 4;
        // 注释任务
        public static final Integer EXEGESIS = 5;
        // 完成
        public static final Integer FINISH = 6;

        public static final List<Integer> stages = CollectionUtil.list(false,
                READY, TABLE_RELATION, TABLE_FIELD, CONVERSION, EXEGESIS, FINISH);
    }
}
