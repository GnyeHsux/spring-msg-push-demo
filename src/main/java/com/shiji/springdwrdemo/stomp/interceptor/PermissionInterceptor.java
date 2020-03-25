package com.shiji.springdwrdemo.stomp.interceptor;

import com.shiji.springdwrdemo.stomp.enums.CodeEnum;
import com.shiji.springdwrdemo.stomp.exception.ErrorCodeException;
import com.shiji.springdwrdemo.stomp.utils.CheckUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限校验拦截器
 *
 * @author xsy
 * @date 2020/3/23
 */
@Component
@Slf4j
public class PermissionInterceptor implements HandlerInterceptor {

    private final static String TOKEN = "token";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ErrorCodeException {
        String token = request.getHeader(PermissionInterceptor.TOKEN);

        log.info("权限校验 token -> {}", token);

        if (CheckUtils.checkToken(token)) {
            return true;
        }

        log.info("没有权限");

        throw new ErrorCodeException(CodeEnum.INVALID_TOKEN);
    }
}
