package com.laijiaxiang.supreme.interceptor;

import com.laijiaxiang.supreme.constants.CommonConstants;
import com.laijiaxiang.supreme.exception.TokenValidateException;
import com.laijiaxiang.supreme.handler.SupremeContextHandler;
import com.laijiaxiang.supreme.model.vo.UserTokenInfoVO;
import com.laijiaxiang.supreme.model.vo.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;


public class TokenInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader(CommonConstants.AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            throw new TokenValidateException("token不存在，请先登录！");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new TokenValidateException("token格式不合法，请重新登录！");
        }
        String[] authorizationArray = authorization.split(" ");
        if(authorizationArray.length < 2){
            throw new TokenValidateException("token无效，请重新登录！");
        }
        String token = authorizationArray[1];
        boolean verified = false;
        try {
            //redis中获取token
            UserTokenInfoVO clientTokenInfo = (UserTokenInfoVO) redisTemplate.opsForValue().get(CommonConstants.REDIS_CLIENT_ACCESS_TOKEN + token);
            if (!ObjectUtils.isEmpty(clientTokenInfo)) {
                verified = true;
                SupremeContextHandler.setUserId(clientTokenInfo.getId());
                SupremeContextHandler.setToken(UserLoginVO.builder().accessToken(token).refreshToken(clientTokenInfo.getRefreshToken()).build());
            }
        } catch (Exception ex) {
            throw new TokenValidateException("token校验失败，请重新登录！");
        }
        if (!verified) {
            throw new TokenValidateException("token已失效，请重新登录！");
        }
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SupremeContextHandler.remove();
    }
}
