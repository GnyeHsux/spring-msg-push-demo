package com.shiji.springdwrdemo.stomp.aop;

import com.shiji.springdwrdemo.dao.MessageRecordRepository;
import com.shiji.springdwrdemo.stomp.domain.dto.ChatRecordDTO;
import com.shiji.springdwrdemo.stomp.domain.mo.MessageRecord;
import com.shiji.springdwrdemo.stomp.domain.vo.MessageVO;
import com.shiji.springdwrdemo.stomp.enums.MessageTypeEnum;
import com.shiji.springdwrdemo.stomp.service.ChatRecordService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * 聊天记录切面类
 *
 * @author xsy
 * @date 2020/3/23
 */
@Aspect
@Component
@Slf4j
public class ChatRecordAspect {

    @Resource
    private ChatRecordService chatRecordService;

    @Autowired
    private MessageRecordRepository messageRecordRepository;

    @Pointcut("@annotation(com.shiji.springdwrdemo.stomp.annotation.ChatRecord)")
    public void chatRecordPointcut() {
    }

    @Before("chatRecordPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        log.debug("before -> {}", joinPoint);

        MessageVO messageVO = null;
        Object[] args = joinPoint.getArgs();
        for (Object obj : args) {
            if (obj instanceof MessageVO) {
                messageVO = (MessageVO) obj;
                break;
            }
        }

        Assert.notNull(messageVO, "方法必需以MessageVO类或该类的子类作为参数");

        if (messageVO.getType() != MessageTypeEnum.SYSTEM) {
            // 对于User类型的消息做敏感词处理
//            messageVO.setMessage(SensitiveWordUtils.loveChina(messageVO.getMessage()));
            // 记录聊天信息
            MessageRecord record = MessageRecord.toMessageRecord(messageVO);
            if (messageVO.getReceiver() == null) {
                record.setGroupMsg(true);
                record.setGroupId("lgp");
            } else {
                record.setGroupMsg(false);
            }
            messageRecordRepository.insert(record);
        }

        log.debug("添加聊天记录 -> {}", messageVO);
        chatRecordService.addRecord(ChatRecordDTO.toChatRecordDTO(messageVO));
    }

}
