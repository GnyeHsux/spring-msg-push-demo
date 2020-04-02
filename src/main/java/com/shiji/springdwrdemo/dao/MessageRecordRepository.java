package com.shiji.springdwrdemo.dao;

import com.shiji.springdwrdemo.stomp.domain.mo.MessageRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRecordRepository extends MongoRepository<MessageRecord, String> {
}
