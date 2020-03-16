package com.shiji.springdwrdemo.websocket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class WebsocketController {

    @GetMapping("/index")
    public String index(){
        return "wsClient"; //当浏览器输入/index时，会返回 /templates/home.html页面
    }

    @GetMapping("/getOnlineUser")
    @ResponseBody
    public List<String> getOnLineUser(){
        return WebsocketServer.getOnLineUserList();
    }
}
