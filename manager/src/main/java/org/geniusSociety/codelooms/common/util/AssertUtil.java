package org.geniusSociety.codelooms.common.util;

import cn.hutool.core.util.StrUtil;
import jakarta.validation.ValidationException;
import org.geniusSociety.codelooms.common.exception.AssertException;

/**
 * 断言
 *
 * @author Cealus
 */
public class AssertUtil {

    public static void isFalse(boolean expression, String... message) {
        if (expression) {
            throw new ValidationException(StrUtil.concat(true, message));
        }
    }

    public static void isFalse(boolean expression, int code, String... message) {
        if (expression) {
            throw new AssertException(code, StrUtil.concat(true, message));
        }
    }

    public static void isTrue(boolean expression, String... message) {
        if (!expression) {
            throw new ValidationException(StrUtil.concat(true, message));
        }
    }

    public static void isTrue(boolean expression, int code, String... message) {
        if (!expression) {
            throw new AssertException(code, StrUtil.concat(true, message));
        }
    }

    public static void isAllTrue(String message, boolean... expressions) {
        for (boolean expression : expressions) {
            if (!expression) {
                throw new ValidationException(message);
            }
        }
    }

    public static void isAllTrue(String message, int code, boolean... expressions) {
        for (boolean expression : expressions) {
            if (!expression) {
                throw new AssertException(code, StrUtil.concat(true, message));
            }
        }
    }

    public static void isAnyTrue(String message, int code, boolean... expressions) {
        for (boolean expression : expressions) {
            if (expression) {
                throw new AssertException(code, StrUtil.concat(true, message));
            }
        }
    }

    public static void notNull(Object obj, String... message) {
        isNotNull(obj, message);
    }

    public static void notNull(Object obj, int code, String... message) {
        isNotNull(obj, code, message);
    }

    public static void isNotNull(Object obj, String... message) {
        if (null == obj) {
            throw new ValidationException(StrUtil.concat(true, message));
        }
    }

    public static void isNotNull(Object obj, int code, String... message) {
        if (null == obj) {
            throw new AssertException(code, StrUtil.concat(true, message));
        }
    }

    public static void isNull(Object obj, String... message) {
        if (null != obj) {
            throw new ValidationException(StrUtil.concat(true, message));
        }
    }

    public static void isNull(Object obj, int code, String... message) {
        if (null != obj) {
            throw new AssertException(code, StrUtil.concat(true, message));
        }
    }

    public static void throwMessage(String... message) {
        throw new ValidationException(StrUtil.concat(true, message));
    }

    public static void throwMessage(int code, String... message) {
        throw new AssertException(code, StrUtil.concat(true, message));
    }

}
