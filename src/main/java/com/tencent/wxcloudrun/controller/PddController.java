package com.tencent.wxcloudrun.controller;

import com.tencent.wxcloudrun.service.PddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多宝客api
 */
@RestController
public class PddController {

    PddService pddService;

    public PddController(@Autowired PddService pddService) {
        this.pddService = pddService;
    }

    @PostMapping(value = "/pdd/authUrl")
    @ResponseBody
    public Object pdd() {
        return pddService.pddAuthUrl();
    }

}
