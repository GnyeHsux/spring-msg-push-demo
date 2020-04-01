package com.shiji.springdwrdemo.dao;

import com.shiji.springdwrdemo.stomp.domain.mo.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
