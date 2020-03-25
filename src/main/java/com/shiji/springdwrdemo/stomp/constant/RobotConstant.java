package com.shiji.springdwrdemo.stomp.constant;

/**
 * 机器人相关常量
 *
 * @author xsy
 * @date 2020/3/23
 */
public interface RobotConstant {
    /**
     * 存储的key
     */
    String key = "robot";
    /**
     * 触发机器人聊天的消息前缀
     */
    String prefix = "#";
    /**
     * 机器人名称
     */
    String name = "小小毅";
    /**
     * 机器人头像
     */
    String avatar = "./images/avatar/robot.jpeg";
    /**
     * 机器人地理位置
     */
    String address = "火星";
}
