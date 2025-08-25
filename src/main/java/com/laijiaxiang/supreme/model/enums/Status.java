package com.laijiaxiang.supreme.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

    IN_PROGRESS("正在处理..."),
    SUCCESS("规划成功"),
    FAIL("规划失败，点击重试");

    private String desc;

}
