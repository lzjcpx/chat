package com.tjise.service.impl;

import com.idworker.Sid;
import com.tjise.enums.SearchFriendsStatusEnum;
import com.tjise.mapper.FriendsRequestMapper;
import com.tjise.mapper.MyFriendsMapper;
import com.tjise.mapper.UserMapper;
import com.tjise.pojo.FriendsRequest;
import com.tjise.pojo.MyFriends;
import com.tjise.pojo.User;
import com.tjise.service.UserService;
import com.tjise.utils.FastDFSClient;
import com.tjise.utils.QRCodeUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

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

    @Resource
    QRCodeUtils qrCodeUtils;

    @Resource
    FastDFSClient fastDFSClient;

    @Resource
    MyFriendsMapper myFriendsMapper;

    @Resource
    FriendsRequestMapper friendsRequestMapper;

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
        String userId = sid.nextShort();
        //为每个注册用户生成一个唯一的二维码
//        String qrCodePath = "/usr/local/qrcode/"+userId+"qrcode.png";
//        //创建二维码对象信息
//        qrCodeUtils.createQRCode(qrCodePath,"bird_qrcode:"+user.getUsername());
//        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
//        String qrCodeURL ="";
//        try {
//            qrCodeURL = fastDFSClient.uploadQRCode(qrcodeFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        user.setId(userId);
//        user.setQrcode(qrCodeURL);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        User result = userMapper.selectByPrimaryKey(user.getId());
        return result;
    }

    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        if (user == null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        if (myUserId.equals(user.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        MyFriends myfriend = new MyFriends();
        myfriend.setMyUserId(myUserId);
        myfriend.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myfriend);
        if (myF != null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }

        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        MyFriends myfriend = new MyFriends();
        myfriend.setMyUserId(myUserId);
        myfriend.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myfriend);
        if (myF == null){
            FriendsRequest friendsRequest = new FriendsRequest();
            String requestId = sid.nextShort();
            friendsRequest.setId(requestId);
            friendsRequest.setSendUserId(myUserId);
            friendsRequest.setAcceptUserId(user.getId());
            friendsRequest.setRequestDateTime(new Date());
            friendsRequestMapper.insert(friendsRequest);
        }
    }
}
