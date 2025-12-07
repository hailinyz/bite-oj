package com.bite.common.core.domain;


import com.bite.common.core.enums.ResultCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class R<T> {

    private int code; //定义一些固定（常量集合维护用枚举）的code，前后端商量好的 0 成功 1 失败 2 权限不足 3 参数错误 4 账号不存在 5 账号已存在 6 密码错误 等等

    private String msg; // 通常是code的辅助说明 一个code对应一个msg

    private T data; //请求某个接口返回的数据，类型可能是不同一的，所以用泛型

    public static <T> R<T> ok() {
        return assembleResult( null, ResultCode.SUCCESS);
    }

    public static <T> R<T> ok(T data) {
        return assembleResult(data, ResultCode.SUCCESS);
    }

    public static <T> R<T> fail() {
        return assembleResult( null, ResultCode.FAILED);
    }

    public static <T> R<T> fail(int code, String msg) {
        return assembleResult(code,msg, null);
    }

    /**
     * 指定错误码
     * 指定错误码
     * @param resultCode 指定错误码
     * @param <T>
     * @return
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return assembleResult( null, resultCode);
    }

    private static <T> R<T> assembleResult(T data, ResultCode resultCode) {
        R<T> r = new R<T>();
        r.setCode(resultCode.getCode());
        r.setData(data);
        r.setMsg(resultCode.getMsg());
        return r;
    }

    private static <T> R<T> assembleResult(int code, String msg, T data) {
        R<T> r = new R<T>();
        r.setCode(code);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

}
