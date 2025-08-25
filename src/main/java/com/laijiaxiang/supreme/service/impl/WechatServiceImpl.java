package com.laijiaxiang.supreme.service.impl;

import com.alibaba.fastjson2.JSON;
import com.laijiaxiang.supreme.exception.BizException;
import com.laijiaxiang.supreme.model.response.WechatResponse;
import com.laijiaxiang.supreme.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class WechatServiceImpl implements WechatService {

    private static final String MINI_PROGRAM_ACCESS_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code ";

    private final OkHttpClient httpClient;

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.appSecret}")
    private String appSecret;

    @Autowired
    public WechatServiceImpl(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String getMiniProgramOpenid(String wechatCode) {
        String accessUrl = String.format(MINI_PROGRAM_ACCESS_URL, appId, appSecret, wechatCode);
        Request request = new Request.Builder()
                .url(accessUrl)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            WechatResponse wechatResponse = JSON.parseObject(responseBody, WechatResponse.class);
            int errcode = wechatResponse.getErrcode();
            if (errcode != 0) {
                throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR, "获取小程序openid异常，错误码：" + errcode + "， 错误信息：" + wechatResponse.getErrmsg());
            }
            return wechatResponse.getOpenid();
        } catch (IOException ex) {
            log.error("获取小程序openid异常", ex);
            throw new BizException(HttpStatus.INTERNAL_SERVER_ERROR, "获取小程序openid异常，请稍后再试");
        }
    }

}
