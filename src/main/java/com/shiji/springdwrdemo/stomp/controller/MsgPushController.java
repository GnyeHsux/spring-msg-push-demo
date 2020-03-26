package com.shiji.springdwrdemo.stomp.controller;

import com.shiji.springdwrdemo.stomp.cache.UserCache;
import com.shiji.springdwrdemo.stomp.constant.RobotConstant;
import com.shiji.springdwrdemo.stomp.constant.StompConstant;
import com.shiji.springdwrdemo.stomp.domain.mo.User;
import com.shiji.springdwrdemo.stomp.domain.ro.MessageRO;
import com.shiji.springdwrdemo.stomp.domain.ro.RevokeMessageRO;
import com.shiji.springdwrdemo.stomp.domain.vo.MessageVO;
import com.shiji.springdwrdemo.stomp.domain.vo.RevokeMsgVo;
import com.shiji.springdwrdemo.stomp.enums.CodeEnum;
import com.shiji.springdwrdemo.stomp.enums.MessageTypeEnum;
import com.shiji.springdwrdemo.stomp.enums.inter.Code;
import com.shiji.springdwrdemo.stomp.exception.ErrorCodeException;
import com.shiji.springdwrdemo.stomp.service.MessageService;
import com.shiji.springdwrdemo.stomp.utils.CheckUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
public class MsgPushController {

    @Resource
    private MessageService messageService;

    /**
     * 聊天室发布订阅
     *
     * @param messageRO 消息请求对象
     * @param user 发送消息的用户对象
     * @throws Exception
     */
    @MessageMapping(StompConstant.PUB_CHAT_ROOM)
    public void chatRoom(MessageRO messageRO, User user) throws Exception {
        String message = messageRO.getMessage();

        if (!CheckUtils.checkMessageRo(messageRO) || !CheckUtils.checkUser(user)) {
            throw new ErrorCodeException(CodeEnum.INVALID_PARAMETERS);
        }
        if (CheckUtils.checkMessage(message) && message.startsWith(RobotConstant.prefix)) {
            messageService.sendMessageToRobot(StompConstant.SUB_CHAT_ROOM, message, user);
        }
        log.info("用户【{}】发送广播消息：【{}】", user.getUsername(), messageRO.getMessage());
        messageService.sendMessage(StompConstant.SUB_CHAT_ROOM, new MessageVO(user, message, messageRO.getImage(),
                MessageTypeEnum.USER));
    }

    /**
     * 发送消息到指定用户
     *
     * @param messageRO 消息请求对象
     * @param user 发送消息的用户对象
     * @throws Exception
     */
    @MessageMapping(StompConstant.PUB_USER)
    public void sendToUser(MessageRO messageRO, User user) throws Exception {
        if (!CheckUtils.checkMessageRo(messageRO) || !CheckUtils.checkUser(user)) {
            throw new ErrorCodeException(CodeEnum.INVALID_PARAMETERS);
        }

        log.info("用户【{}】发送消息:【{}】到-->> 【{}】", user.getUsername(), messageRO.getMessage(), UserCache.getUsers(messageRO.getReceiver()));

        messageService.sendMessageToUser(messageRO.getReceiver(), new MessageVO(user, messageRO.getMessage(),
                messageRO.getImage(), MessageTypeEnum.USER, messageRO.getReceiver()));
    }

    /**
     * 消息异常处理
     *
     * @param e 异常对象
     * @param user 发送消息的用户对象
     */
    @MessageExceptionHandler(Exception.class)
    public void handleExceptions(Exception e, User user) {
        Code code = CodeEnum.INTERNAL_SERVER_ERROR;

        if (e instanceof ErrorCodeException) {
            code = ((ErrorCodeException) e).getCode();
        } else {
            log.error("error:", e);
        }

        messageService.sendErrorMessage(code, user);
    }

    /**
     * 撤回消息
     *
     * @param revokeMessageRO 撤消消息请求对象
     * @param user 发送消息的用户对象
     * @throws Exception
     */
    @MessageMapping(StompConstant.PUB_CHAT_ROOM_REVOKE)
    public void revokeMessage(RevokeMessageRO revokeMessageRO, User user) throws Exception {
        if (revokeMessageRO == null || !CheckUtils.checkUser(user)) {
            throw new ErrorCodeException(CodeEnum.INVALID_PARAMETERS);
        }

        CheckUtils.checkMessageId(revokeMessageRO.getMessageId(), user.getUserId());

        RevokeMsgVo revokeMsgVo = new RevokeMsgVo();
        revokeMsgVo.setRevokeMessageId(revokeMessageRO.getMessageId());
        revokeMsgVo.setUser(user);
        revokeMsgVo.setType(MessageTypeEnum.REVOKE);

        if (CheckUtils.checkReceiver(revokeMessageRO.getReceiver())) {
            // 将消息发送到指定用户
            messageService.sendMessageToUser(revokeMessageRO.getReceiver(), revokeMsgVo);
            return;
        }

        // 将消息发送到所有用户
        messageService.sendMessage(StompConstant.SUB_CHAT_ROOM, revokeMsgVo);
    }
}
