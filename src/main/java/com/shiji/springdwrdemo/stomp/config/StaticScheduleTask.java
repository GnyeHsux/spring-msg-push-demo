package com.shiji.springdwrdemo.stomp.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.shiji.springdwrdemo.stomp.dao.OfflineMessageHistoryRepository;
import com.shiji.springdwrdemo.stomp.dao.OfflineMessageRepository;
import com.shiji.springdwrdemo.stomp.domain.mo.OfflineMessage;
import com.shiji.springdwrdemo.stomp.domain.mo.OfflineMessageHistory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
@Slf4j
public class StaticScheduleTask {

    @Autowired
    private OfflineMessageRepository offlineMessageRepository;

    @Autowired
    private OfflineMessageHistoryRepository offlineMessageHistoryRepository;



    //直接指定时间间隔，例如：5秒
    //@Scheduled(fixedRate=5000)
    //添加定时任务
    @Scheduled(cron = "0 0 0 * * ?")
    private void configureTasks() {
        log.info("执行清除已接收离线消息定时任务时间: {}", LocalDateTime.now());

        List<OfflineMessage> list = offlineMessageRepository.findAll(Example.of(OfflineMessage.builder().hasSend(true).build()));

        log.info("已发送离线消息条数：{}", list.size());

        if (CollectionUtils.isNotEmpty(list)) {
            String json = JSON.toJSONString(list);
            List<OfflineMessageHistory> historyList = JSONArray.parseArray(json, OfflineMessageHistory.class);
            offlineMessageHistoryRepository.insert(historyList);
            offlineMessageRepository.deleteAll(list);
        }

        log.info("执行结束！！！");
    }
}
