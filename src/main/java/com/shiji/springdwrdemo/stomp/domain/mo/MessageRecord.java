package com.shiji.springdwrdemo.stomp.domain.mo;

import com.shiji.springdwrdemo.stomp.domain.dto.ChatRecordDTO;
import com.shiji.springdwrdemo.stomp.domain.vo.MessageVO;
import com.shiji.springdwrdemo.stomp.enums.MessageTypeEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * 消息记录
 */
@Getter
@Setter
@ToString
@Builder
public class MessageRecord implements Serializable {

    private static final long serialVersionUID = 319516027267892760L;

    @Id
    private String recordId;

    private String messageId;

    private User user;

    private String message;

    private String image;

    private MessageTypeEnum msgType;

    private String sendTime;

    private String[] receiver;

    private Boolean groupMsg;

    private String groupId;

    public static MessageRecord toMessageRecord(MessageVO messageVO) {
        if (null == messageVO) {
            return null;
        }

        MessageRecord messageRecord = MessageRecord.builder().build();
        BeanUtils.copyProperties(messageVO, messageRecord);
        messageRecord.setMsgType(messageVO.getType());
        return messageRecord;
    }

    public MessageVO toMessageVO() {
        MessageVO messageVO = new MessageVO();
        messageVO.setType(this.getMsgType());
        messageVO.setRealMessageId(this.getMessageId());
        messageVO.setRealSendTime(this.getSendTime());
        messageVO.setOfflineMsg(true);
        BeanUtils.copyProperties(this, messageVO);
        return messageVO;
    }
}
