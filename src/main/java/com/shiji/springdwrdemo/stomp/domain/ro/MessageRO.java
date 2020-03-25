package com.shiji.springdwrdemo.stomp.domain.ro;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;

/**
 * 消息请求
 *
 * @author xsy
 * @date 2020/3/23
 */
@Getter
@Setter
@ToString
public class MessageRO implements Serializable {

    private static final long serialVersionUID = 3544216886850149310L;

    /**
     * 接收者
     */
    private String[] receiver;
    /**
     * 消息
     */
    private String message;
    /**
     * 图片
     */
    private String image;

    public String[] getReceiver() {
        return ArrayUtils.clone(receiver);
    }

    public void setReceiver(String[] receiver) {
        this.receiver = ArrayUtils.clone(receiver);
    }
}
