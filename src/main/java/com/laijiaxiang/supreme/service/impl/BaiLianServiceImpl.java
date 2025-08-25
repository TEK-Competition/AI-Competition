package com.laijiaxiang.supreme.service.impl;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.laijiaxiang.supreme.service.BaiLianService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaiLianServiceImpl implements BaiLianService {

    @Value("${aliyun.bailian.apiKey}")
    private String apiKey;

    @Value("${aliyun.bailian.appId}")
    private String appId;

    @Override
    public String singleChat(String prompt) throws NoApiKeyException, InputRequiredException {
        ApplicationParam param = ApplicationParam.builder()
                // 若没有配置环境变量，可用百炼API Key将下行替换为：.apiKey("sk-xxx")。但不建议在生产环境中直接将API Key硬编码到代码中，以减少API Key泄露风险。
                .apiKey(apiKey)
                .appId(appId)
                .prompt( prompt)
                .build();
        Application application = new Application();
        log.info("开始时间：{}",  System.currentTimeMillis());
        ApplicationResult result = application.call(param);
        log.info("结束时间：{}",  System.currentTimeMillis());
        return result.getOutput().getText();
    }

}
