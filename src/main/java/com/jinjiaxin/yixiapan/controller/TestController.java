package com.jinjiaxin.yixiapan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jjx
 * @Description
 * @create 2023/9/13 22:00
 */

@RestController
public class TestController {

    @GetMapping("/test")
    public String test(){
        return "test";
    }

}
