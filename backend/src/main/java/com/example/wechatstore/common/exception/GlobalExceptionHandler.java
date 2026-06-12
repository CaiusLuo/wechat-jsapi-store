package com.example.wechatstore.common.exception;

import com.example.wechatstore.common.result.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 统一处理业务异常。
     * <p>
     * 业务异常通常用于参数非法、状态不允许、第三方接口失败等可预期问题。
     * </p>
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 统一处理请求参数校验失败。
     * <p>
     * 这里只返回第一条错误，保证前端收到的信息简洁明确。
     * </p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("request validation failed");
        return Result.fail(400, message);
    }
}
