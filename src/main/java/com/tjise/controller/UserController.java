package com.tjise.controller;

import com.tjise.bo.UserBo;
import com.tjise.pojo.User;
import com.tjise.service.UserService;
import com.tjise.utils.ChatJSONResult;
import com.tjise.utils.FastDFSClient;
import com.tjise.utils.FileUtils;
import com.tjise.utils.MD5Utils;
import com.tjise.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

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

    @RequestMapping("/getUser")
    public String getUserById(String id, Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user_list";
    }

}
