package com.shiji.springdwrdemo.stomp.interceptor;

import com.shiji.springdwrdemo.stomp.dao.UserLoginInfoRepository;
import com.shiji.springdwrdemo.stomp.dao.UserRepository;
import com.shiji.springdwrdemo.stomp.constant.DateConstant;
import com.shiji.springdwrdemo.stomp.constant.UserStatusConstant;
import com.shiji.springdwrdemo.stomp.domain.mo.User;
import com.shiji.springdwrdemo.stomp.domain.mo.UserLoginInfo;
import com.shiji.springdwrdemo.stomp.utils.DateUtils;
import com.shiji.springdwrdemo.stomp.utils.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private UserRepository userRepository;

    @Autowired
    private UserLoginInfoRepository userLoginInfoRepository;

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
            String userId = stompHeaderAccessor.getFirstNativeHeader("userId");
            String username = stompHeaderAccessor.getFirstNativeHeader("username");
            String avatar = stompHeaderAccessor.getFirstNativeHeader("avatar");
            String address = stompHeaderAccessor.getFirstNativeHeader("address");
            if (StringUtils.isEmpty(avatar)) {
                avatar = "/images/avator.png";
            }

            User user = new User();
            if (StringUtils.isEmpty(userId)) {
                user.setUsername(username);
                Optional<User> userRst = userRepository.findOne(Example.of(user));
                if (userRst.isPresent()) {
                    user.setUserId(userRst.get().getUserId());
                    if (!avatar.endsWith("/images/avator.png")) {
                        user.setAvatar(avatar);
                    } else {
                        user.setAvatar(userRst.get().getAvatar());
                    }
                } else {
                    user.setUserId(UUIDUtils.create());
                    user.setAvatar(avatar);
                }
                user.setUsername(username);
                user.setAddress(address);
                user.setStatus(UserStatusConstant.ONLINE);
                if (userRst.isPresent()) {
                    userRepository.save(user);
                } else {
                    userRepository.insert(user);
                }
            } else {
                user.setUserId(userId);
                Optional<User> userRst = userRepository.findOne(Example.of(user));
                if (userRst.isPresent()) {
                    user = userRst.get();
                    user.setUsername(username);
                    if (!avatar.endsWith("/images/avator.png")) {
                        user.setAvatar(avatar);
                    }
                    user.setAvatar(avatar);
                    user.setAddress(address);
                    user.setStatus(UserStatusConstant.ONLINE);
                    userRepository.save(user);
                } else {
                    user.setUserId(UUIDUtils.create());
                    user.setUsername(username);
                    user.setAvatar(avatar);
                    user.setAddress(address);
                    user.setStatus(UserStatusConstant.ONLINE);
                    userRepository.insert(user);
                }
            }

            stompHeaderAccessor.setUser(user);
            log.info("绑定用户信息 -> {}", user);

            // 记录登录信息
            UserLoginInfo loginInfo = new UserLoginInfo();
            loginInfo.setUserId(user.getUserId());

            Optional<UserLoginInfo> loginInfoRst = userLoginInfoRepository.findOne(Example.of(loginInfo));
            if (loginInfoRst.isPresent()) {
                loginInfo = loginInfoRst.get();
                loginInfo.setUsername(user.getUsername());
                loginInfo.setAddress(user.getAddress());
                loginInfo.setLastLoginTime(DateUtils.getDate(DateConstant.SEND_TIME_FORMAT));
                loginInfo.setLoginTimes(loginInfo.getLoginTimes() + 1);
                userLoginInfoRepository.save(loginInfo);
            } else {
                loginInfo.setUsername(user.getUsername());
                loginInfo.setAddress(user.getAddress());
                loginInfo.setFirstLoginTime(DateUtils.getDate(DateConstant.SEND_TIME_FORMAT));
                loginInfo.setLastLoginTime(loginInfo.getFirstLoginTime());
                loginInfo.setLoginTimes(1);
                userLoginInfoRepository.insert(loginInfo);
            }
        }

        return message;
    }
}
