/**
 * Cealus Li 2025/7/7
 * Copyright
 */
package org.geniusSociety.codelooms.common.configuration;

import lombok.extern.slf4j.Slf4j;
import org.geniusSociety.codelooms.common.constant.ErrorCode;
import org.geniusSociety.codelooms.common.constant.ResultContant;
import org.geniusSociety.codelooms.common.dto.BaseResult;
import org.geniusSociety.codelooms.common.exception.AssertException;
import org.geniusSociety.codelooms.common.vo.ValidationFailResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理
 *
 * @author Cealus Li
 * @date 2025/7/7
 */
@Slf4j
@RestControllerAdvice
public class GlobalValidationHandler {

    /**
     * 参数验证
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = AssertException.class)
    public BaseResult AssertExceptionHandler(final AssertException e) {
        log.error(e.getMessage(), e);
        return new BaseResult(ResultContant.Code.ERROR.getCode(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult handleValidationExceptions(MethodArgumentNotValidException ex) {
        ValidationFailResult result = new ValidationFailResult(ResultContant.Code.FAIL.getCode(), ErrorCode.ARGUMENT_WRONG.getCode());
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            result.setField(fieldName);
            result.setMsg(errorMessage);
        });
        return result;
    }

    /**
     * 运行时异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public BaseResult runtimeExceptionHandler(final Exception e) {
        log.error(e.getMessage(), e);
        return new BaseResult(ResultContant.Code.ERROR.getCode(), ErrorCode.ERROR_INTERNAL.getCode(), e.getMessage());
    }
}
