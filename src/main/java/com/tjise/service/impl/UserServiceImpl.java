package com.tjise.service.impl;

import com.tjise.mapper.UserMapper;
import com.tjise.pojo.User;
import com.tjise.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @auther shkstart
 * @create 2021-12-28-23:20
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper userMapper;

    @Override
    public User getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }
}
