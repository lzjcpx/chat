package com.tjise.mapper;

import com.tjise.vo.FriendsRequestVO;
import com.tjise.vo.MyFriendsVO;

import java.util.List;

/**
 * @auther shkstart
 * @create 2022-01-31-19:08
 */
public interface UserMapperCustom {
    List<FriendsRequestVO> queryFriendRequestList(String acceptUserId);
    List<MyFriendsVO> queryMyFriends(String userId);
    void batchUpdateMsgSigned(List<String> msgIdList);
}
