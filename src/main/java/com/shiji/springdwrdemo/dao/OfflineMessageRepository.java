package com.shiji.springdwrdemo.dao;

import com.shiji.springdwrdemo.stomp.domain.mo.OfflineMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OfflineMessageRepository extends MongoRepository<OfflineMessage, String> {
}
