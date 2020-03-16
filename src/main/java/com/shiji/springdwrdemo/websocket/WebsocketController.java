package com.shiji.springdwrdemo.websocket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

@Controller
public class WebsocketController {

    @GetMapping("/index")
    public ModelAndView index() throws UnknownHostException {
        String ip = Inet4Address.getLocalHost().getHostAddress();
        System.out.println(ip);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("wsClient");
        mv.addObject("ip", ip);
        return mv;
    }

    /**
     * 获取当前在线用户
     * @return
     */
    @GetMapping("/getOnlineUser")
    @ResponseBody
    public List<String> getOnLineUser(){
        return WebsocketServer.getOnLineUserList();
    }
}
