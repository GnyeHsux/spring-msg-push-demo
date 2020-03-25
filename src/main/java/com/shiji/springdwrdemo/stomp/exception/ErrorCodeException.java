package com.shiji.springdwrdemo.stomp.exception;

import com.shiji.springdwrdemo.stomp.enums.inter.Code;

/**
 * 返回错误码
 *
 * @author xsy
 * @date 2020/3/23
 */
public class ErrorCodeException extends Exception {

    private Code code;

    public ErrorCodeException(Code code) {
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }
}
