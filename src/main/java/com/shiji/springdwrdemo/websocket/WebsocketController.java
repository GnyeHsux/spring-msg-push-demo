package com.shiji.springdwrdemo.websocket;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.websocket.server.PathParam;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

@Controller
public class WebsocketController {

    @GetMapping("/index")
    public ModelAndView index() {
        String ip = getServerIp();
        System.out.println(ip);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("wsClient");
        mv.addObject("ip", ip);
        return mv;
    }

    /**
     * 获取当前在线用户
     *
     * @return
     */
    @GetMapping("/getOnlineUser")
    @ResponseBody
    public List<String> getOnLineUser() {
        return WebsocketServer.getOnLineUserList();
    }

    public static String getServerIp() {

        String SERVER_IP = null;
        try {
            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                ip = ni.getInetAddresses().nextElement();
                SERVER_IP = ip.getHostAddress();
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                        && !ip.getHostAddress().contains(":")) {
                    SERVER_IP = ip.getHostAddress();
                    break;
                } else {
                    ip = null;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return SERVER_IP;
    }

    /**
     * 强制用户离线
     * @param userId
     * @return
     */
    @GetMapping("/tUser/{userId}")
    @ResponseBody
    public ResponseEntity<String> tUser(@PathVariable("userId") String userId) {
        boolean rst = WebsocketServer.closeUser(userId);
        return rst ? ResponseEntity.ok("success") : ResponseEntity.ok("fail");
    }

    /**
     * 服务推送消息给用户
     * @param message
     * @param toUserId
     * @return
     * @throws IOException
     */
    @RequestMapping("/push/{toUserId}")
    public ResponseEntity<String> pushToWeb(String message, @PathVariable String toUserId) throws IOException {
        WebsocketServer.sendInfo(message, toUserId);
        return ResponseEntity.ok("MSG SEND SUCCESS");
    }
}
