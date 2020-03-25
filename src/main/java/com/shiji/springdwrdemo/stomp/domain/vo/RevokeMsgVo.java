package com.shiji.springdwrdemo.stomp.domain.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 撤消消息
 *
 * @author xsy
 * @date 2020/3/23
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class RevokeMsgVo extends MessageVO {

    /**
     * 撤回的消息id
     */
    private String revokeMessageId;
}
