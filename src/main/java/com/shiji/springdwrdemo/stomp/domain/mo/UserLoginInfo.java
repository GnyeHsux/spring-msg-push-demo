package com.shiji.springdwrdemo.stomp.domain.mo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * 用户登录信息
 */
@Getter
@Setter
@ToString
public class UserLoginInfo implements Serializable {

    private static final long serialVersionUID = -9012421359897204061L;

    @Id
    private String infoId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 第一次登录时间
     */
    private String firstLoginTime;

    /**
     * 最近一次登录时间
     */
    private String lastLoginTime;

    /**
     * 登录次数
     */
    private Integer loginTimes;

    /**
     * 地址
     */
    private String address;
}
