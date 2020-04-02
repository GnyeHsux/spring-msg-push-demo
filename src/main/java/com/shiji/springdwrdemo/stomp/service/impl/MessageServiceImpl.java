package com.shiji.springdwrdemo.stomp.service.impl;

import com.shiji.springdwrdemo.dao.OfflineMessageRepository;
import com.shiji.springdwrdemo.stomp.annotation.ChatRecord;
import com.shiji.springdwrdemo.stomp.cache.UserCache;
import com.shiji.springdwrdemo.stomp.constant.RobotConstant;
import com.shiji.springdwrdemo.stomp.constant.StompConstant;
import com.shiji.springdwrdemo.stomp.domain.mo.OfflineMessage;
import com.shiji.springdwrdemo.stomp.domain.mo.User;
import com.shiji.springdwrdemo.stomp.domain.vo.MessageVO;
import com.shiji.springdwrdemo.stomp.domain.vo.ResponseVO;
import com.shiji.springdwrdemo.stomp.enums.CodeEnum;
import com.shiji.springdwrdemo.stomp.enums.MessageTypeEnum;
import com.shiji.springdwrdemo.stomp.enums.inter.Code;
import com.shiji.springdwrdemo.stomp.exception.ErrorCodeException;
import com.shiji.springdwrdemo.stomp.service.MessageService;
import com.shiji.springdwrdemo.stomp.utils.CheckUtils;
import com.shiji.springdwrdemo.stomp.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xsy
 * @date 2020/3/23
 */
@Service
@Slf4j
public class MessageServiceImpl implements MessageService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private OfflineMessageRepository offlineMessageRepository;

    /*@Resource
    private RobotService robotService;*/

    @Override
    public void sendErrorMessage(Code code, User user) {
        log.debug("发送错误信息 -> {} -> {}", code, user);
        messagingTemplate.convertAndSendToUser(user.getUserId(), StompConstant.SUB_ERROR, new ResponseVO(code));
    }

    @ChatRecord
    @Override
    public void sendMessage(String subAddress, MessageVO messageVO) throws Exception {
        if (!CheckUtils.checkSubAddress(subAddress)) {
            throw new ErrorCodeException(CodeEnum.INVALID_PARAMETERS);
        }

        messagingTemplate.convertAndSend(subAddress, buildResponseVo(messageVO));
    }

    @ChatRecord
    @Override
    public void sendMessageToUser(String[] receivers, MessageVO messageVO) throws Exception {
        if (!CheckUtils.checkReceiver(receivers)) {
            throw new ErrorCodeException(CodeEnum.INVALID_PARAMETERS);
        }

        ResponseVO responseVO = buildResponseVo(messageVO);
        List<OfflineMessage> offlineMessageList = new ArrayList<>();
        for (String receiverId : receivers) {
            User receiver = UserCache.getUser(receiverId);
            if (receiver == null) {
                log.info("用户【{}】离线，保存离线信息...", receiverId);
                OfflineMessage offlineMsg = OfflineMessage.builder().messageId(messageVO.getMessageId()).receiverId(receiverId).build();
                offlineMessageList.add(offlineMsg);
            } else {
                // 将消息发送到指定用户 参数说明：1.消息接收者 2.消息订阅地址 3.消息内容
                messagingTemplate.convertAndSendToUser(receiverId, StompConstant.SUB_USER, responseVO);
            }
        }
        //保存离线消息
        if (CollectionUtils.isNotEmpty(offlineMessageList)) {
            offlineMessageRepository.insert(offlineMessageList);
        }
    }

    private ResponseVO buildResponseVo(MessageVO messageVO) throws ErrorCodeException {
        if (messageVO == null) {
            throw new ErrorCodeException(CodeEnum.INVALID_PARAMETERS);
        }

        return new ResponseVO(messageVO);
    }

    @Async
    @Override
    public void sendMessageToRobot(String subAddress, String message, User user) throws Exception {
        log.debug("user: {} -> 发送消息到机器人 -> {}", user, message);
        /*String robotMessage = robotService.sendMessage(user.getUserId(), message.replaceFirst(RobotConstant.prefix,
                ""));
        log.debug("机器人响应结果 -> {}", robotMessage);
        sendRobotMessage(subAddress, robotMessage);*/
    }

    @Override
    public void sendRobotMessage(String subAddress, String message) throws Exception {
        SpringUtils.getBean(this.getClass()).sendMessage(subAddress, new MessageVO(UserCache.getUser(RobotConstant.key),
                message, MessageTypeEnum.ROBOT));
    }
}
