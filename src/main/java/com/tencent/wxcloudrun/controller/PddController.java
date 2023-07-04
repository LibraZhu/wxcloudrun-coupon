package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 多宝客api
 */
@Api(tags = "PddController", description = "多宝客api")
@RestController
public class PddController {

    UserService userService;

    public PddController(@Autowired UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/pdd")
    public Object pdd(Map<String, Object> request) {
        return "";
    }

}
