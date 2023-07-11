package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 多宝客api
 */
@RestController
public class PddController {

    UserService userService;

    public PddController(@Autowired UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/pdd/authUrl")
    public Object pdd(Map<String, Object> request) {
        return userService.pddAuthUrl();
    }

}
