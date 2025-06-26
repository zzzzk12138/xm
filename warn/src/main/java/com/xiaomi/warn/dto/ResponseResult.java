package com.xiaomi.warn.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class ResponseResult<T> {
    private Integer status;
    private String msg;
    private T data;

    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setStatus(200);
        result.setMsg("ok");
        result.setData(data);
        return result;
    }

    public static <T> ResponseResult<T> error(int status, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setStatus(status);
        result.setMsg(message);
        return result;
    }

    public static ResponseResult<List<Map<String, Object>>> error(String s) {
        log.error(s);
        return null;
    }
}