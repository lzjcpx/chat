package com.tjise.service.impl;

import com.idworker.Sid;
import com.tjise.enums.MsgActionEnum;
import com.tjise.enums.MsgSignFlagEnum;
import com.tjise.enums.SearchFriendsStatusEnum;
import com.tjise.mapper.*;
import com.tjise.netty.ChatMsg;
import com.tjise.netty.DataContent;
import com.tjise.netty.UserChanelRel;
import com.tjise.pojo.FriendsRequest;
import com.tjise.pojo.MyFriends;
import com.tjise.pojo.User;
import com.tjise.service.UserService;
import com.tjise.utils.FastDFSClient;
import com.tjise.utils.JsonUtils;
import com.tjise.utils.QRCodeUtils;
import com.tjise.vo.FriendsRequestVO;
import com.tjise.vo.MyFriendsVO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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

    @Resource
    UserMapperCustom userMapperCustom;

    @Resource
    ChatMsgMapper chatMsgMapper;

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

    @Override
    public List<FriendsRequestVO> queryFriendRequestList(String acceptUserId) {
        return userMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Override
    public void deleteFriendRequest(FriendsRequest friendsRequest) {
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId, sendUserId);

        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setSendUserId(sendUserId);
        friendsRequest.setAcceptUserId(acceptUserId);
        deleteFriendRequest(friendsRequest);

        Channel sendChannel = UserChanelRel.get(sendUserId);
        if (sendChannel != null){
            //使用websocket 主动推送消息到请求发起者，更新它的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            //消息的推送
            sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }

    }

    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return userMapperCustom.queryMyFriends(userId);
    }

    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.tjise.pojo.ChatMsg msgDB = new com.tjise.pojo.ChatMsg();
        String msgId = sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);
        return msgId;
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        userMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Override
    public List<com.tjise.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        List<com.tjise.pojo.ChatMsg> result = chatMsgMapper.getUnReadMsgListByAcceptUid(acceptUserId);
        return result;
    }

    //通过好友请求并保存数据到数据到myFriends表中
    private void saveFriends(String sendUserId, String acceptUserId){
        MyFriends myFriends = new MyFriends();
        String recordId = sid.nextShort();

        myFriends.setId(recordId);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);
    }
}
