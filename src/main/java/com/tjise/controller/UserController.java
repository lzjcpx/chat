package com.tjise.controller;

import com.tjise.bo.UserBo;
import com.tjise.enums.OperatorFriendRequestTypeEnum;
import com.tjise.enums.SearchFriendsStatusEnum;
import com.tjise.pojo.ChatMsg;
import com.tjise.pojo.FriendsRequest;
import com.tjise.pojo.User;
import com.tjise.service.UserService;
import com.tjise.utils.ChatJSONResult;
import com.tjise.utils.FastDFSClient;
import com.tjise.utils.FileUtils;
import com.tjise.utils.MD5Utils;
import com.tjise.vo.FriendsRequestVO;
import com.tjise.vo.MyFriendsVO;
import com.tjise.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @auther shkstart
 * @create 2021-12-28-23:22
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    FastDFSClient fastDFSClient;

    //用户登录注册一体化方法
    @RequestMapping("/registerOrLogin")
    @ResponseBody
    public ChatJSONResult registerOrLogin(User user){
        User userResult = userService.queryUserNameIsExit(user.getUsername());
        if (userResult != null){//此用户存在，可登录
            if (!userResult.getPassword().equals(MD5Utils.getPwd(user.getPassword()))){
                return ChatJSONResult.errorMap("密码不正确");
            }
        }else {//注册
            user.setNickname("王文");
            user.setQrcode("");
            user.setPassword(MD5Utils.getPwd(user.getPassword()));
            user.setFaceImage("");
            user.setFaceImageBig("");

            userResult = userService.insert(user);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userResult, userVO);
        return ChatJSONResult.ok(userVO);
    }

    @RequestMapping("/uploadFaceBase64")
    @ResponseBody
    //用户头像上传访问方法
    public ChatJSONResult uploadFaceBase64(@RequestBody UserBo userBo) throws Exception {
        //获取前端传过来的base64的字符串，然后转为文件对象在进行上传
        String base64Data = userBo.getFaceData();
        String userFacePath = "/usr/local/face/"+userBo.getUserId()+"userFaceBase64.png";
        //调用FileUtils 类中的方法将base64 字符串转为文件对象
        FileUtils.base64ToFile(userFacePath, base64Data);
        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath);
        //获取fastDFS上传图片的路径
        String url = fastDFSClient.uploadBase64(multipartFile);
        System.out.println(url);
        String thump = "_150x150.";
        String[] arr = url.split("\\.");
        String thumpImgUrl = arr[0]+thump+arr[1];
//        String bigFace = "dssdklsdjsdj3498458.png";
//        String thumpFace = "dssdklsdjsdj3498458_150x150.png";
        //更新用户头像
        User user = new User();
        user.setId(userBo.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);
        User result = userService.updateUserInfo(user);
        return  ChatJSONResult.ok(result);
    }

    @RequestMapping("/setNickName")
    public ChatJSONResult setNickName(User user){
        User userResult = userService.updateUserInfo(user);
        return ChatJSONResult.ok(userResult);
    }

    //搜索好友的请求方法
    @RequestMapping("/searchFriend")
    @ResponseBody
    public ChatJSONResult searchFriend(String myUserId, String friendUserName){
        /**
         * 前置条件：
         * 1.搜索的用户如果不存在，则返回【无此用户】
         * 2.搜索的账号如果是你自己，则返回【不能添加自己】
         * 3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
         */
        Integer status = userService.preconditionSearchFriends(myUserId, friendUserName);
        if (status == SearchFriendsStatusEnum.SUCCESS.status){
            User user = userService.queryUserNameIsExit(friendUserName);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return ChatJSONResult.ok(userVO);
        }else {
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return ChatJSONResult.errorMsg(msg);
        }
    }

    //添加好友请求
    @RequestMapping("/addFriendRequest")
    @ResponseBody
    public ChatJSONResult addFriendRequest(String myUserId, String friendUserName){
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUserName)){
            return ChatJSONResult.errorMsg("好友信息为空");
        }
        Integer status = userService.preconditionSearchFriends(myUserId, friendUserName);
        if (status == SearchFriendsStatusEnum.SUCCESS.status){
            userService.sendFriendRequest(myUserId, friendUserName);
        }else {
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return ChatJSONResult.errorMsg(msg);
        }
        return ChatJSONResult.ok();
    }

    @RequestMapping("/getUser")
    public String getUserById(String id, Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user_list";
    }

    @RequestMapping("/queryFriendRequest")
    @ResponseBody
    public ChatJSONResult queryFriendRequest(String userId){
        List<FriendsRequestVO> friendsRequestVOS = userService.queryFriendRequestList(userId);
        return ChatJSONResult.ok(friendsRequestVOS);
    }

    @RequestMapping("/operFriendRequest")
    @ResponseBody
    public ChatJSONResult operFriendRequest(String acceptUserId,String sendUserId, Integer operType){
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequest.setSendUserId(sendUserId);
        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type){
            //满足此条件将需要对好友请求表中的数据进行删除
            userService.deleteFriendRequest(friendsRequest);
        }else if (operType == OperatorFriendRequestTypeEnum.PASS.type){
            //向好友表中添加一条记录，删除好友请求表中对应的记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }
        //查询好友表中的列表数据
        List<MyFriendsVO> myFriends = userService.queryMyFriends(acceptUserId);
        return ChatJSONResult.ok(myFriends);
    }

    @RequestMapping("/myFriends")
    @ResponseBody
    public ChatJSONResult myFriends(String userId){
        if (StringUtils.isBlank(userId)){
            return ChatJSONResult.errorMsg("用户id为空");
        }
        //数据库中查询好友
        List<MyFriendsVO> myFriends = userService.queryMyFriends(userId);
        return ChatJSONResult.ok(myFriends);
    }

    @RequestMapping("/getUnReadMsgList")
    @ResponseBody
    public ChatJSONResult getUnReadMsgList(String acceptUserId){
        if (StringUtils.isBlank(acceptUserId)){
            return ChatJSONResult.errorMsg("接收者ID不能为空");
        }
        //根据接收者ID查找未签收的消息列表
        List<ChatMsg> unReadMsgList = userService.getUnReadMsgList(acceptUserId);
        return ChatJSONResult.ok(unReadMsgList);
    }

}
