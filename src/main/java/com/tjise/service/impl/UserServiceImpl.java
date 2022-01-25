package com.tjise.service.impl;

import com.idworker.Sid;
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

    @Resource
    Sid sid;

    @Override
    public User getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public User queryUserNameIsExit(String username) {
        User user = userMapper.queryUserNameIsExit(username);
        return user;
    }

    @Override
    public User insert(User user) {
        user.setId(sid.nextShort());
        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        User result = userMapper.selectByPrimaryKey(user.getId());
        return result;
    }
}
