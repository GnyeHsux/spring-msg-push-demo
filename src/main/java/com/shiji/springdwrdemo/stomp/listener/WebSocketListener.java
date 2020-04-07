package com.shiji.springdwrdemo.stomp.listener;

import com.shiji.springdwrdemo.dao.MessageRecordRepository;
import com.shiji.springdwrdemo.dao.OfflineMessageRepository;
import com.shiji.springdwrdemo.dao.UserRepository;
import com.shiji.springdwrdemo.stomp.cache.UserCache;
import com.shiji.springdwrdemo.stomp.constant.DateConstant;
import com.shiji.springdwrdemo.stomp.constant.MessageConstant;
import com.shiji.springdwrdemo.stomp.constant.StompConstant;
import com.shiji.springdwrdemo.stomp.constant.UserStatusConstant;
import com.shiji.springdwrdemo.stomp.domain.mo.MessageRecord;
import com.shiji.springdwrdemo.stomp.domain.mo.OfflineMessage;
import com.shiji.springdwrdemo.stomp.domain.mo.User;
import com.shiji.springdwrdemo.stomp.domain.vo.DynamicMsgVo;
import com.shiji.springdwrdemo.stomp.domain.vo.MessageVO;
import com.shiji.springdwrdemo.stomp.service.MessageService;
import com.shiji.springdwrdemo.stomp.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Example;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class WebSocketListener {

    @Resource
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfflineMessageRepository offlineMessageRepository;

    @Autowired
    private MessageRecordRepository messageRecordRepository;

    private User user;


    @EventListener
    public void handleConnectListener(SessionConnectedEvent sessionConnectedEvent) {
        log.debug("建立连接 -> {}", sessionConnectedEvent);

        user = (User) sessionConnectedEvent.getUser();

        UserCache.addUser(user.getUserId(), user);
        log.info("用户建立连接：{}", user);
    }

    @EventListener
    public void handleDisconnectListener(SessionDisconnectEvent sessionDisconnectEvent) throws Exception {
        log.debug("断开连接 -> {}", sessionDisconnectEvent);

        String userId = sessionDisconnectEvent.getUser().getName();
        User user = UserCache.getUser(userId);
        if (null == user) {
            log.info("用户不存在 uid ->", userId);
            return;
        }

        user.setStatus(UserStatusConstant.OFFLINE);
        userRepository.save(user);
        UserCache.removeUser(userId);

        // 广播离线消息
        sendGroupMessage(buildMessageVo(user, MessageConstant.OFFLINE_MESSAGE));
        log.info("广播离线消息 -> {}", user);
    }

    @EventListener
    public void handleSubscribeListener(SessionSubscribeEvent sessionSubscribeEvent) throws Exception {
        log.debug("新的订阅 -> {}", sessionSubscribeEvent);
        StompHeaderAccessor stompHeaderAccessor = MessageHeaderAccessor.getAccessor(sessionSubscribeEvent.getMessage(),
                StompHeaderAccessor.class);

        if (StompConstant.SUB_STATUS.equals(stompHeaderAccessor.getFirstNativeHeader(StompHeaderAccessor.STOMP_DESTINATION_HEADER))) {
            if (user != null) {
                try {
                    // 延迟100ms，防止客户端来不及接收上线消息
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    log.error("中断异常，error -> {}", e);
                }

                // 广播上线消息
                sendGroupMessage(buildMessageVo(user, MessageConstant.ONLINE_MESSAGE));
                // 发送机器人欢迎消息
//                sendRobotMessage(String.format(MessageConstant.ROBOT_WELCOME_MESSAGE, user.getUsername()));
                log.info("广播上线消息 -> {}", user);
            }
        }

        // 发送离线消息
        if (user != null) {
            if (("/user/" + user.getUserId() + StompConstant.SUB_USER).equals(stompHeaderAccessor.getFirstNativeHeader(StompHeaderAccessor.STOMP_DESTINATION_HEADER))) {
                OfflineMessage offlineMessage = OfflineMessage.builder().receiverId(user.getUserId()).hasSend(false).build();

                List<OfflineMessage> msgRst = offlineMessageRepository.findAll(Example.of(offlineMessage));
                if (CollectionUtils.isNotEmpty(msgRst)) {
                    for (OfflineMessage message : msgRst) {
                        log.info(MessageRecord.builder().messageId(message.getMessageId()).build().toString());
                        Optional<MessageRecord> messageRst = messageRecordRepository.findOne(Example.of(MessageRecord.builder().messageId(message.getMessageId()).build()));
                        if (messageRst.isPresent()) {
                            sendUserMessage(message.getReceiverId(), messageRst.get().toMessageVO());
                            //发送完毕回写记录已重新发送
                            message.setHasSend(true);
                            message.setReSendTime(DateUtils.getDate(DateConstant.SEND_TIME_FORMAT));
                            offlineMessageRepository.save(message);
                        }
                    }
                }
            }
        }
    }


    /**
     * 发送订阅消息，广播用户动态
     *
     * @param messageVO
     */
    private void sendGroupMessage(MessageVO messageVO) throws Exception {
        messageService.sendMessage(StompConstant.SUB_STATUS, messageVO);
    }

    private void sendUserMessage(String receiver, MessageVO messageVO) throws Exception {
        log.info(messageVO.toString());
        messageService.sendOfflineMessageToUser(new String[]{receiver}, messageVO);
    }

    /**
     * 构建消息视图
     *
     * @param user
     * @return
     */
    private MessageVO buildMessageVo(User user, String message) {
        DynamicMsgVo dynamicMsgVo = new DynamicMsgVo();
        dynamicMsgVo.setUser(user);
        dynamicMsgVo.setMessage(String.format(message, user.getUsername()));
        dynamicMsgVo.setOnlineCount(UserCache.getOnlineCount());
        dynamicMsgVo.setOnlineUserList(UserCache.listUser());

        return dynamicMsgVo;
    }
}
