package com.jrzx.platform.cxcbiz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by yanhuan on 2021/11/19 16:16
 */
@RestController
@RequestMapping("/biz")
public class Test {

    @GetMapping("/tt")
    public String test(){
        return "aaaa";
    }
}
