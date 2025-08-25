package com.laijiaxiang.supreme.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

//    public static void main(String[] args) {
//        System.out.println(PasswordUtils.encodePassword("79ee60ea-1fa0-4be8-ba0a-f4a951a0e54f"));
//    }

    private PasswordUtils() {

    }

    /**
     * 使用 BCrypt 算法对原始密码进行哈希处理。
     *
     * @param rawPassword 用户提供的原始密码。
     * @return 返回经过 BCrypt 加密后的哈希值字符串。
     */
    public static String encodePassword(String rawPassword) {
        // Generate a strong salt
        String salt = BCrypt.gensalt(12); // 12 是工作因子，默认值为 10
        return BCrypt.hashpw(rawPassword, salt);
    }

    /**
     * 检查用户提供的原始密码是否与存储的哈希密码匹配。
     *
     * @param rawPassword     用户提供的原始密码。
     * @param encodedPassword 存储在数据库中的 BCrypt 哈希密码。
     * @return 如果提供的原始密码正确，则返回 true；否则返回 false。
     */
    public static boolean checkPassword(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}