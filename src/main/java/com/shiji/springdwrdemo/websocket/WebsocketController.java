//package com.shiji.springdwrdemo.websocket;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.ModelAndView;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.util.Enumeration;
//import java.util.List;
//
//@Controller
//public class WebsocketController {
//
//    @GetMapping("/index")
//    public ModelAndView index() {
//        String ip = getServerIp();
//        System.out.println(ip);
//        ModelAndView mv = new ModelAndView();
//        mv.setViewName("wsClient");
//        mv.addObject("ip", ip);
//        return mv;
//    }
//
//    @GetMapping("/group")
//    public String group() {
//        return "initGroup";
//    }
//
//    /**
//     * 获取当前在线用户
//     *
//     * @return
//     */
//    @GetMapping("/getOnlineUser")
//    @ResponseBody
//    public List<String> getOnLineUser() {
//        return WebsocketServer.getOnLineUserList();
//    }
//
//    public static String getServerIp() {
//
//        String SERVER_IP = null;
//        try {
//            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
//            InetAddress ip = null;
//            while (netInterfaces.hasMoreElements()) {
//                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
//                ip = ni.getInetAddresses().nextElement();
//                SERVER_IP = ip.getHostAddress();
//                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
//                        && !ip.getHostAddress().contains(":")) {
//                    SERVER_IP = ip.getHostAddress();
//                    break;
//                } else {
//                    ip = null;
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//
//        return SERVER_IP;
//    }
//
//    /**
//     * 强制用户离线
//     * @param userId
//     * @return
//     */
//    @GetMapping("/tUser/{userId}")
//    @ResponseBody
//    public ResponseEntity<String> tUser(@PathVariable("userId") String userId) {
//        boolean rst = WebsocketServer.closeUser(userId);
//        return rst ? ResponseEntity.ok("success") : ResponseEntity.ok("fail");
//    }
//
//    /**
//     * 服务推送消息给用户
//     * @param message
//     * @param toUserId
//     * @return
//     * @throws IOException
//     */
//    @PostMapping("/push/{toUserId}")
//    @ResponseBody
//    public ResponseEntity<String> pushToWeb(@RequestParam String message, @PathVariable String toUserId) throws IOException {
//        return ResponseEntity.ok(WebsocketServer.sendInfo(message, toUserId));
//    }
//
//    @PostMapping("/group/createGroup")
//    @ResponseBody
//    public ResponseEntity<String> createGroup(@RequestParam String groupName) {
//        WebsocketServer.createGroup(groupName);
//        return ResponseEntity.ok("SUCCESS");
//    }
//
//    @GetMapping("/group/getGroups")
//    @ResponseBody
//    public ResponseEntity<List<String>> getGroup() {
//        return ResponseEntity.ok(WebsocketServer.getGroupList());
//    }
//}
