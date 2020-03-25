package com.shiji.springdwrdemo.stomp.domain.vo;

import com.shiji.springdwrdemo.stomp.domain.mo.User;
import com.shiji.springdwrdemo.stomp.enums.MessageTypeEnum;
import lombok.*;

import java.util.List;

/**
 * 聊天室动态消息
 *
 * @author yanpanyi
 * @date 2019/3/22
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DynamicMsgVo extends MessageVO {

    /**
     * 在线人数
     */
    private int onlineCount;

    /**
     * 在线用户列表
     */
    private List<User> onlineUserList;

    @Override
    public MessageTypeEnum getType() {
        return MessageTypeEnum.SYSTEM;
    }
}
