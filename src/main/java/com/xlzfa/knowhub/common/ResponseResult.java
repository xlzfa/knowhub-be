package com.xlzfa.knowhub.common;

import lombok.Data;

@Data
public class ResponseResult {

    private Integer code;
    private String msg;
    private Object data;

    //无参构造
    public ResponseResult() {
    }

    //有参构造
    public ResponseResult(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    //成功，无数据可返回
    public static ResponseResult success() {
        return new ResponseResult(200, "success", null);
    }

    //成功，返回数据
    public static ResponseResult success(Object data) {
        return new ResponseResult(200, "success", data);
    }

    //失败,返回失败信息
    public static ResponseResult error(String msg) {
        return new ResponseResult(500, msg, null);
    }
    //失败，返回code和信息
    public static ResponseResult error(Integer code, String msg) {
        return new ResponseResult(code, msg, null);
    }



}
