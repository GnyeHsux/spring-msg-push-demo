package com.shiji.springdwrdemo.stomp.service;


import com.shiji.springdwrdemo.stomp.domain.dto.ChatRecordDTO;

import java.util.HashMap;
import java.util.List;

/**
 * 聊天记录
 *
 * @author xsy
 * @date 2020/3/23
 */
public interface ChatRecordService {

    /**
     * 添加聊天记录
     *
     * @param chatRecordDTO 聊天记录对象
     */
    void addRecord(ChatRecordDTO chatRecordDTO);

    /**
     * 聊天记录列表
     *
     * @param directoryName 目录名
     * @return 聊天记录列表
     */
    List<HashMap<String, Object>> listRecord(String directoryName);
}
