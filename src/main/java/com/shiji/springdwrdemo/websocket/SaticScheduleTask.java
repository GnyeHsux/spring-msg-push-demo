package com.shiji.springdwrdemo.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

/**
 * 心跳
 */
@Configuration
@EnableScheduling
public class SaticScheduleTask {

    @Scheduled(fixedRate = 58 * 1000)
    private void configureTasks() {
        WebsocketServer.setHearBeatMsg();
    }
}
