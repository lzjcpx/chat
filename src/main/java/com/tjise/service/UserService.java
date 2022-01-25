package com.tjise.service;

import com.tjise.pojo.User;

/**
 * @auther shkstart
 * @create 2021-12-28-23:19
 */
public interface UserService {

    User getUserById(String id);

    //根据用户名查找指定用户对象
    User queryUserNameIsExit(String username);

    //保存
    User insert(User user);

    //更新用户
    User updateUserInfo(User user);
}
