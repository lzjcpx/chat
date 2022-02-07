package com.tjise.service;

import com.tjise.netty.ChatMsg;
import com.tjise.pojo.FriendsRequest;
import com.tjise.pojo.User;
import com.tjise.vo.FriendsRequestVO;
import com.tjise.vo.MyFriendsVO;

import java.util.List;

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

    //搜索好友的前置条件
    Integer preconditionSearchFriends(String myUserId, String friendUserName);

    //发送好友请求
    void sendFriendRequest(String myUserId, String friendUserName);

    //好友请求列表查询
    List<FriendsRequestVO> queryFriendRequestList(String acceptUserId);

    //处理好友请求-忽略好友请求
    void deleteFriendRequest(FriendsRequest friendsRequest);

    //处理好友请求-通过好友请求
    void passFriendRequest(String sendUserId, String acceptUserId);

    //好友列表查询
    List<MyFriendsVO> queryMyFriends(String userId);

    //保存用户聊天消息
    String saveMsg(ChatMsg chatMsg);

    void updateMsgSigned(List<String> msgIdList);

    List<com.tjise.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}