package com.imooc.bilibili.domain.exception;
/**
 * ConditionException：条件异常，根据条件来抛出异常
 * */
public class ConditionException extends RuntimeException {

    private static final long serialVersionUID = 1L;//添加一个序列

    private String code;//响应的状态码

    public ConditionException(String code, String name) {
        super(name);
        this.code = code;
    }

    public ConditionException(String name) {
        super(name);
        code = "500";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
