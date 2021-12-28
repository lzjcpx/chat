package com.tjise.controller;

import com.tjise.pojo.User;
import com.tjise.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @RequestMapping("/getUser")
    public String getUserById(String id, Model model){
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user_list";
    }

}
