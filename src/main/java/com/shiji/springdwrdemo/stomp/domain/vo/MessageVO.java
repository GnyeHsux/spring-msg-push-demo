package com.shiji.springdwrdemo.stomp.domain.vo;

import com.shiji.springdwrdemo.stomp.constant.DateConstant;
import com.shiji.springdwrdemo.stomp.domain.mo.User;
import com.shiji.springdwrdemo.stomp.enums.MessageTypeEnum;
import com.shiji.springdwrdemo.stomp.utils.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 消息视图
 *
 * @author xsy
 * @date 2020/3/23
 */
@ToString
@NoArgsConstructor
public class MessageVO implements Serializable {

    private static final long serialVersionUID = -1455469852669257711L;

    private Long timestamp = System.currentTimeMillis();

    /**
     * 用户
     */
    @Getter
    @Setter
    private User user;
    /**
     * 消息信息
     */
    @Getter
    @Setter
    private String message;
    /**
     * 图片
     */
    @Getter
    @Setter
    private String image;
    /**
     * 消息类型
     */
    @Getter
    @Setter
    private MessageTypeEnum type;
    /**
     * 消息id
     */
    private String messageId;
    /**
     * 发送时间
     */
    private String sendTime;

    /**
     * 接收者
     */
    @Getter
    @Setter
    private String[] receiver;

    public MessageVO(User user, String message, String image, MessageTypeEnum type, String[] receiver) {
        this.user = user;
        this.message = message;
        this.image = image;
        this.type = type;
        this.receiver = receiver;
    }

    public MessageVO(User user, String message, String image, MessageTypeEnum type) {
        this.user = user;
        this.message = message;
        this.image = image;
        this.type = type;
    }

    public MessageVO(User user, String message, MessageTypeEnum type) {
        this.user = user;
        this.message = message;
        this.type = type;
    }

    public String getSendTime() {
        return DateUtils.getDate(timestamp, DateConstant.SEND_TIME_FORMAT);
    }

    public String getMessageId() {
        return user.getUserId() + ':' + timestamp;
    }
}
