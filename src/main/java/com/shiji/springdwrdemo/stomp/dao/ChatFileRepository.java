package com.shiji.springdwrdemo.stomp.dao;

import com.shiji.springdwrdemo.stomp.domain.mo.ChatFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatFileRepository extends MongoRepository<ChatFile, String> {
}
