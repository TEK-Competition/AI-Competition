package org.geniusSociety.codelooms.common.constant;

/**
 * @author Cealus 2025/7/5
 */
public class WebConstant {

//    public static final String HOME = "home";
    /**
     * 项目管理
     */
    public static final String PROJECT = "project/";
    /**
     * 任务管理
     */
    public static final String TASK = "task";
    /**
     * 系统管理
     */
    public static final String SYSTEM = "sys/";


    public static class ItemPath {
        /**
         * 项目信息
         */
        public static final String ITEM = PROJECT + "item";
        /**
         * 项目文件
         */
        public static final String FILE = PROJECT + "file";
        /**
         * 知识库
         */
        public static final String KNOWLEDGE = PROJECT + "knowledge";
        /**
         * 表信息
         */
        public static final String TABLE = PROJECT + "table";
        /**
         * 字段信息
         */
        public static final String FIELD = PROJECT + "field";
    }

    public static class SysPath {
        /**
         * 文件管理
         */
        public static final String FILE = SYSTEM + "file";

    }
}
