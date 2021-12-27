package com.tjise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @auther shkstart
 * @create 2021-12-26-22:19
 */
@Controller
public class TestController {

    @RequestMapping("/test")
    public String test(){
        return "test";
    }

    @RequestMapping("/userList")
    public String userList(){
        return "/user_list";
    }

}
