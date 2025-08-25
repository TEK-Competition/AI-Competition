package com.laijiaxiang.supreme.constants;

public final class CommonConstants {

    private CommonConstants() {
    }

    public static final String JWT_SECRET = System.getenv("jasypt.encryptor.password");

    public static final String DEFAULT_PASSWORD = "123456";

    public static final String DEVICE_ID = "DeviceId";
    public static final String AUTHORIZATION = "Authorization";

    public static final String REDIS_CODE_IMAGE = "code:image:";
    public static final String REDIS_MANAGER_ACCESS_TOKEN = "access_token:manager:";
    public static final String REDIS_MANAGER_REFRESH_TOKEN = "refresh_token:manager:";
    public static final String REDIS_ADMIN_ACCESS_TOKEN = "access_token:admin:";
    public static final String REDIS_ADMIN_REFRESH_TOKEN = "refresh_token:admin:";
    public static final String REDIS_CLIENT_ACCESS_TOKEN = "travel_plan:access_token:client:";
    public static final String REDIS_CLIENT_REFRESH_TOKEN = "travel_plan:refresh_token:client:";
    public static final String REDIS_ADMIN_AUTH_INFO = "auth_info:admin:";

    /**
     * 微信openid
     */
    public static final String OPENID = "openid";

    /**
     * 微信unionid
     */
    public static final String UNIONID = "unionid";

    /**
     * 过期时间
     */
    public static final String EXPIRE_TIME = "expire_time";

}
