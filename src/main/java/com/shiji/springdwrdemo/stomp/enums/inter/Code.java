package com.shiji.springdwrdemo.stomp.enums.inter;

/**
 * 获取响应code
 *
 * @author xsy
 * @date 2020/3/23
 */
public interface Code {

    /**
     * 获取响应码
     *
     * @return
     */
    int getCode();

    /**
     * 获取响应码描述
     *
     * @return
     */
    String getDesc();
}
