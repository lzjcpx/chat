package com.tjise.controller;

import com.tjise.pojo.User;
import com.tjise.service.UserService;
import com.tjise.utils.ChatJSONResult;
import com.tjise.utils.MD5Utils;
import com.tjise.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    //用户登录注册一体化方法
    @RequestMapping("/registerOrLogin")
    @ResponseBody
    public ChatJSONResult registerOrLogin(User user){
        User userResult = userService.queryUserNameIsExit(user.getUsername());
        if (userResult != null){//此用户存在，可登录
            if (userResult.getPassword().equals(MD5Utils.getPwd(user.getPassword()))){
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

    @RequestMapping("/getUser")
    public String getUserById(String id, Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user_list";
    }

}
