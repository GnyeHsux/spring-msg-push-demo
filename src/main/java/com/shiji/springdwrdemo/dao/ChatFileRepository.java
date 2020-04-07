package com.shiji.springdwrdemo.dao;

import com.shiji.springdwrdemo.stomp.domain.mo.ChatFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatFileRepository extends MongoRepository<ChatFile, String> {
}
