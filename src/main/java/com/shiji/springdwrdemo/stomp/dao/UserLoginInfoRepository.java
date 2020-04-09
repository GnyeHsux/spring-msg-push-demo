package com.shiji.springdwrdemo.stomp.dao;

import com.shiji.springdwrdemo.stomp.domain.mo.UserLoginInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserLoginInfoRepository extends MongoRepository<UserLoginInfo, String> {
}
