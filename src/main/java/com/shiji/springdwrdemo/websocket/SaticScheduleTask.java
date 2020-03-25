//package com.shiji.springdwrdemo.websocket;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import javax.annotation.Resource;
//
///**
// * 心跳
// */
//@Configuration
//@EnableScheduling
//public class SaticScheduleTask {
//
//    /**
//     * 使用Nginx反向代理时，需要设置定时发送心跳消息，不然连接会断开
//     */
//    @Scheduled(fixedRate = 58 * 1000)
//    private void configureTasks() {
//        WebsocketServer.setHearBeatMsg();
//    }
//}
