package com.shiji.springdwrdemo.stomp.constant;

/**
 * 消息模板
 *
 * @author anlingyi
 * @date 2020/3/23
 */
public interface MessageConstant {
    /**
     * 进入聊天室广播消息
     */
    String ONLINE_MESSAGE = "%s上线了";
    /**
     * 离开聊天室广播消息
     */
    String OFFLINE_MESSAGE = "%s已离开";
    /**
     * 机器人欢迎消息
     */
    String ROBOT_WELCOME_MESSAGE = "@%s 欢迎来到聊天室！消息内容以'#'开头的我就能收到哦（PS：双击我的头像与我聊天），" +
            "随时来撩我呀！";
}
