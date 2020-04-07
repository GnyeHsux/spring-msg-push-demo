package com.shiji.springdwrdemo.stomp.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

public class Md5Utils {

    public static String getMD5(InputStream inputStream) {
        try {
            return DigestUtils.md5Hex(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
