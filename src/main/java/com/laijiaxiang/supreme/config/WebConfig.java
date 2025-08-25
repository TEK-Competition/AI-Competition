package com.laijiaxiang.supreme.config;

import com.laijiaxiang.supreme.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public WebConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor(redisTemplate))
                .addPathPatterns("/**")
                // 应用到特定路径
                .excludePathPatterns(
                        "/common/captcha/**",
                        "/auth/register",
                        "/auth/account/login",
                        "/auth/wechat/login",
                        "/auth/refreshToken",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
