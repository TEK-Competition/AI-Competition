package com.laijiaxiang.supreme.exception;

import com.laijiaxiang.supreme.model.response.SupremeResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String AT_LEAST_CONTAIN_ONE_FILE = "请求中必须至少包含一个有效文件";

    private static final Pattern pattern = Pattern.compile("\\[[\\u4e00-\\u9fa5][^]]+]");

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Object> bizException(BizException ex, HttpServletRequest request) {
        log.warn("BizException:", ex);
        HttpStatus httpStatus = ex.getHttpStatus();
        return switch (httpStatus) {
            case HttpStatus.BAD_REQUEST ->
                    new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, ex.getMessage()), httpStatus);
            case HttpStatus.UNAUTHORIZED ->
                    new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.UNAUTHORIZED, ex.getMessage()), httpStatus);
            case HttpStatus.FORBIDDEN ->
                    new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.FORBIDDEN, ex.getMessage()), httpStatus);
            case HttpStatus.NOT_FOUND ->
                    new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.SERVER_NOT_FOUND, ex.getMessage()), httpStatus);
            default -> new ResponseEntity<>(SupremeResponse.fail(ex.getMessage()), httpStatus);
        };
    }

    @ExceptionHandler(ParameterValidateException.class)
    public ResponseEntity<Object> parameterValidateException(ParameterValidateException ex, HttpServletRequest request) {
        log.warn("ParameterValidateException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenValidateException.class)
    public ResponseEntity<Object> tokenValidateException(TokenValidateException ex, HttpServletRequest request) {
        log.warn("TokenValidateException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.UNAUTHORIZED, ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceForbiddenException.class)
    public ResponseEntity<Object> resourceForbiddenExceptionException(ResourceForbiddenException ex, HttpServletRequest request) {
        log.warn("ResourceForbiddenException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.FORBIDDEN, ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException exception, HttpServletResponse response) {
        log.debug("NoHandlerFoundException:", exception);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.SERVER_NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> bindException(BindException ex, HttpServletRequest request) {
        log.debug("BindException:", ex);
        String msgs = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        if (StringUtils.hasText(msgs)) {
            return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, msgs), HttpStatus.BAD_REQUEST);
        }
        StringBuilder msg = new StringBuilder();
        List<FieldError> fieldErrors = ex.getFieldErrors();
        fieldErrors.forEach(oe ->
                msg.append("参数:[").append(oe.getObjectName())
                        .append(".").append(oe.getField())
                        .append("]的传入值:[").append(oe.getRejectedValue()).append("]与预期的字段类型不匹配.")
        );
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, msg.toString()), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.debug("MethodArgumentTypeMismatchException:", ex);
        String msg = "参数：[" + ex.getName() +
                "]的传入值：[" + ex.getValue() +
                "]与预期的字段类型：[" + Objects.requireNonNull(ex.getRequiredType()).getName() + "]不匹配";
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, msg), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> illegalStateException(IllegalStateException ex, HttpServletRequest request) {
        log.debug("IllegalStateException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, "无效参数异常"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> missingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.debug("MissingServletRequestParameterException:", ex);
        String msg = "缺少必须的[" + ex.getParameterType() + "]类型的参数[" + ex.getParameterName() + "]";
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, msg), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> nullPointerException(NullPointerException ex, HttpServletRequest request) {
        log.debug("NullPointerException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail("空指针异常"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> illegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.debug("IllegalArgumentException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, "无效参数异常"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.debug("HttpMediaTypeNotSupportedException:", ex);
        MediaType contentType = ex.getContentType();
        if (contentType != null) {
            return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, "请求类型(Content-Type)[" + contentType.toString() + "] 与实际接口的请求类型不匹配"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, "无效的Content-Type类型"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> missingServletRequestPartException(MissingServletRequestPartException ex, HttpServletRequest request) {
        log.debug("MissingServletRequestPartException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, AT_LEAST_CONTAIN_ONE_FILE), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<Object> servletException(ServletException ex, HttpServletRequest request) {
        log.debug("ServletException:", ex);
        String msg = "UT010016: Not a multi part request";
        if (msg.equalsIgnoreCase(ex.getMessage())) {
            return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, AT_LEAST_CONTAIN_ONE_FILE), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.SYSTEM_BUSY, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> multipartException(MultipartException ex, HttpServletRequest request) {
        log.debug("MultipartException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, AT_LEAST_CONTAIN_ONE_FILE), HttpStatus.BAD_REQUEST);
    }

    /**
     * jsr 规范中的验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        log.debug("ConstraintViolationException:", ex);
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        String message = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(";"));
        if (StringUtils.hasText(message)) {
            return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
        }
        ConstraintViolation<?> violation = violations.iterator().next();
        String path = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String message2 = String.format("%s:%s", path, violation.getMessage());
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, message2), HttpStatus.BAD_REQUEST);
    }

    /**
     * 返回状态码:405
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.debug("HttpRequestMethodNotSupportedException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail(ExceptionCode.BAD_REQUEST, "不支持当前请求类型"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> sqlException(SQLException ex, HttpServletRequest request) {
        log.debug("SQLException:", ex);
        return new ResponseEntity<>(SupremeResponse.fail("运行SQL出现异常"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> otherExceptionHandler(Exception ex, HttpServletRequest request) {
        log.warn("Exception:", ex);
        if (ex.getCause() instanceof BizException) {
            BizException bizException = (BizException) ex.getCause();
            return new ResponseEntity<>(SupremeResponse.fail(ex.getMessage()), bizException.getHttpStatus());
        }
        return new ResponseEntity<>(SupremeResponse.fail(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
