CREATE TABLE `cv_file` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `parent` bigint unsigned DEFAULT '0' COMMENT '上级文件',
  `item_id` bigint unsigned DEFAULT NULL COMMENT '项目ID',
  `name` varchar(128) NOT NULL COMMENT '文件名',
  `path` varchar(256) NOT NULL COMMENT '文件路径',
  `type` varchar(255) DEFAULT NULL COMMENT '文件类型',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `save_mode` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文件';

CREATE TABLE `cv_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(64) NOT NULL COMMENT '任务名称',
  `type` int NOT NULL COMMENT '运行模式，1：自动；2：手动',
  `status` int NOT NULL COMMENT '状态，1：新建；2：运行；3：完成；4：失败',
  `remark` varchar(2048) DEFAULT NULL COMMENT '说明',
  `table_relation` varchar(4096) DEFAULT NULL COMMENT '表关系',
  `sp_relation` varchar(4096) DEFAULT NULL COMMENT '存储过程关系',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转换项目';

CREATE TABLE `cv_item_file` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `item_id` bigint unsigned NOT NULL COMMENT '项目ID',
  `name` varchar(128) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件名',
  `sp_path` varchar(256) CHARACTER SET utf8mb4 NOT NULL COMMENT '存储过程文件路径',
  `sql_path` varchar(256) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'SQL文件路径',
  `stage` int DEFAULT NULL COMMENT '步骤，1： 初始化；2：表关系任务；3：表字段任务；4：转换任务；5：注释任务',
  `relation` varchar(4096) DEFAULT NULL COMMENT '表关系',
  `save_mode` varchar(64) DEFAULT NULL COMMENT '存储模式',
  `is_del` int DEFAULT NULL COMMENT '删除，1：是',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文件';

CREATE TABLE `cv_item_knowledge` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `item_id` bigint unsigned NOT NULL COMMENT '项目ID',
  `name` varchar(128) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件名',
  `path` varchar(256) CHARACTER SET utf8mb4 NOT NULL COMMENT '文件路径',
  `save_mode` varchar(64) DEFAULT NULL COMMENT '存储模式',
  `is_del` int NOT NULL COMMENT '删除，1：是',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文件';

CREATE TABLE `cv_table` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `task_id` bigint unsigned DEFAULT NULL COMMENT '任务ID',
  `name` varchar(128) DEFAULT NULL COMMENT '表名',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表信息';

CREATE TABLE `cv_table_field` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `table_id` bigint unsigned NOT NULL COMMENT '表ID',
  `name` varchar(128) DEFAULT NULL COMMENT '字段名',
  `data_type` varchar(32) DEFAULT NULL COMMENT '数据类型',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表字段';

CREATE TABLE `cv_task` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `item_id` bigint unsigned NOT NULL COMMENT '项目ID',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `type` int DEFAULT NULL COMMENT '运行模式，1：自动；2：手动',
  `status` int DEFAULT NULL COMMENT '状态，1：新建；2：运行；3：完成；4：失败',
  `steps` int DEFAULT NULL COMMENT '步骤，1： 初始化；2：表关系任务；3：表字段任务；4：转换任务；5：注释任务；6：完成',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '结束时间',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_updated` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转换任务';

CREATE TABLE `cv_task_stage` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `task_id` bigint unsigned NOT NULL COMMENT '任务ID',
  `status` int DEFAULT NULL COMMENT '状态，1：新建；2：运行；3：完成；4：失败',
  `stage` int DEFAULT NULL COMMENT '步骤，1： 初始化；2：表关系任务；3：表字段任务；4：转换任务；5：注释任务',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '结束时间',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_updated` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务计划';

CREATE TABLE `mate_table` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `item_id` bigint unsigned DEFAULT NULL COMMENT '项目ID',
  `name` varchar(128) DEFAULT NULL COMMENT '表名',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表信息';

CREATE TABLE `mate_table_field` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `table_id` bigint unsigned NOT NULL COMMENT '表ID',
  `name` varchar(128) DEFAULT NULL COMMENT '字段名',
  `data_type` varchar(32) DEFAULT NULL COMMENT '数据类型',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `user_id` int unsigned DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表字段';

CREATE TABLE `sys_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `account` varchar(32) NOT NULL COMMENT '账号',
  `password` varchar(64) NOT NULL COMMENT '密码',
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';
