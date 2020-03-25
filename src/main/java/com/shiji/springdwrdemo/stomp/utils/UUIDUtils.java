package com.shiji.springdwrdemo.stomp.utils;

import java.util.UUID;

/**
 * uuid工具类
 *
 * @author xsy
 * @date 2020/3/23
 */
public class UUIDUtils {

    /**
     * 生成uuid
     *
     * @return
     */
    public static String create() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
