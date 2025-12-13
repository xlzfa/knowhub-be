package com.xlzfa.knowhub.common;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public com.xlzfa.knowhub.common.ResponseResult handleBusinessException(BusinessException ex) {
        return com.xlzfa.knowhub.common.ResponseResult.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 运行时异常
     * @param ex
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public com.xlzfa.knowhub.common.ResponseResult handleRuntimeException(RuntimeException ex) {
        return com.xlzfa.knowhub.common.ResponseResult.error(500, "系统运行异常： " + ex.getMessage());
    }

    /**
     * 其他异常
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public com.xlzfa.knowhub.common.ResponseResult handleException(Exception ex) {
        return com.xlzfa.knowhub.common.ResponseResult.error(500, "未知异常： " + ex.getMessage());
    }


}
