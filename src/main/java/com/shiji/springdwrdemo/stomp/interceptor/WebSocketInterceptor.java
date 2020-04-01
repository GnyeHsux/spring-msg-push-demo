package com.shiji.springdwrdemo.stomp.interceptor;

import com.shiji.springdwrdemo.dao.UserRepository;
import com.shiji.springdwrdemo.stomp.constant.UserStatusConstant;
import com.shiji.springdwrdemo.stomp.domain.mo.User;
import com.shiji.springdwrdemo.stomp.utils.SensitiveWordUtils;
import com.shiji.springdwrdemo.stomp.utils.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * webcocket拦截器
 *
 * @author xsy
 * @date 2020/3/23
 */
@Component
@Slf4j
public class WebSocketInterceptor implements ChannelInterceptor {

    @Autowired
    UserRepository userRepository;

    /**
     * 绑定用户信息
     *
     * @param message
     * @param channel
     * @return
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.debug("进入拦截器 -> preSend");
        StompHeaderAccessor stompHeaderAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
            String username = stompHeaderAccessor.getFirstNativeHeader("username");

            User user = new User();
            user.setUsername(username);
            Optional<User> optionalUser = userRepository.findOne(Example.of(user));
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                user.setAvatar(stompHeaderAccessor.getFirstNativeHeader("avatar"));
                user.setAddress(stompHeaderAccessor.getFirstNativeHeader("address"));
                user.setStatus(UserStatusConstant.ONLINE);
                userRepository.save(user);
            } else {
                user.setUserId(UUIDUtils.create());
                user.setUsername(username);
                user.setAvatar(stompHeaderAccessor.getFirstNativeHeader("avatar"));
                user.setAddress(stompHeaderAccessor.getFirstNativeHeader("address"));
                user.setStatus(UserStatusConstant.ONLINE);
                userRepository.insert(user);
            }

            stompHeaderAccessor.setUser(user);
            log.info("绑定用户信息 -> {}", user);
        }

        return message;
    }
}
