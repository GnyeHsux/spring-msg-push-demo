package com.shiji.springdwrdemo.stomp.domain.mo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * 离线消息记录
 */
@Getter
@Setter
@ToString
@Builder
public class OfflineMessage implements Serializable {

    private static final long serialVersionUID = -996981471364447653L;

    @Id
    private String ofMsgId;

    private String messageId;

    private Boolean hasSend = false;

    private String reSendTime;

    private String receiverId;
}
