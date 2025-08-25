package com.laijiaxiang.supreme.service;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

public interface BaiLianService {

    String singleChat(String prompt) throws NoApiKeyException, InputRequiredException;

}
