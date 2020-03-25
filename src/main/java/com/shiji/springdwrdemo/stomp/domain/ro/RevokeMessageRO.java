package com.shiji.springdwrdemo.stomp.domain.ro;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;

/**
 * 撤消消息请求
 *
 * @author anlingyi
 * @date 2020/3/23
 */
@Getter
@Setter
@ToString
public class RevokeMessageRO implements Serializable {

    private static final long serialVersionUID = -8463062216437674093L;

    /**
     * 接收者
     */
    private String[] receiver;
    /**
     * 消息id
     */
    private String messageId;

    public String[] getReceiver() {
        return ArrayUtils.clone(receiver);
    }

    public void setReceiver(String[] receiver) {
        this.receiver = ArrayUtils.clone(receiver);
    }
}
