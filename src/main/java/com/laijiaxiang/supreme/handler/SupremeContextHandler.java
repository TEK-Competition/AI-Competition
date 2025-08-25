package com.laijiaxiang.supreme.handler;

import com.laijiaxiang.supreme.model.vo.UserLoginVO;

public class SupremeContextHandler {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<UserLoginVO> tokenHolder = new ThreadLocal<>();

    private SupremeContextHandler() {
    }

    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static void setToken(UserLoginVO token) {
        tokenHolder.set(token);
    }

    public static UserLoginVO getToken() {
        return tokenHolder.get();
    }

    public static void remove() {
        userIdHolder.remove();
        tokenHolder.remove();
    }

}
