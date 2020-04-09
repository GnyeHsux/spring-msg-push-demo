package com.shiji.springdwrdemo.stomp.dao;

import com.shiji.springdwrdemo.stomp.domain.mo.OfflineMessageHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OfflineMessageHistoryRepository extends MongoRepository<OfflineMessageHistory, String> {
}
