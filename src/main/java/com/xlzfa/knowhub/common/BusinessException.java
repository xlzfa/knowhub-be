package com.xlzfa.knowhub.common;


/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException{

    private Integer code;

    //只传信息
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    //传错误码和信息
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
