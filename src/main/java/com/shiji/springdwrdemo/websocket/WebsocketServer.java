//package com.shiji.springdwrdemo.websocket;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.collections4.CollectionUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.websocket.*;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//@ServerEndpoint("/wsServer/{userId}")
//@Component
//public class WebsocketServer {
//
//    private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);
//
//    /**
//     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
//     */
//    private static int onlineCount = 0;
//    /**
//     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
//     */
//    private static ConcurrentHashMap<String, WebsocketServer> webSocketMap = new ConcurrentHashMap<>();
//
//    private static List<String> onLineUserList = new ArrayList<>();
//    /**
//     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
//     */
//    private Session session;
//    /**
//     * 接收userId
//     */
//    private String userId = "";
//
//    /**
//     * 群组
//     */
//    private static Map<String, List<String>> groupMap = new ConcurrentHashMap<>();
//
//    public static void createGroup(String groupName) {
//        List<String> group = groupMap.get(groupName);
//        if (CollectionUtils.isEmpty(group)) {
//            groupMap.put(groupName, new ArrayList<>());
//        }
//    }
//
//    public static List<String> getGroupList() {
//        List<String> groups = new ArrayList<>();
//        groupMap.forEach((key, value) -> {
//            groups.add(key);
//        });
//        return groups;
//    }
//
//    public void joinGroup(String groupName) {
//        List<String> group = groupMap.get(groupName);
//        if (group == null) {
//            log.error("{}群不存在!!!", groupName);
//            return;
//        }
//        if (group.contains(this.userId)) {
//            log.warn("你已加入该群，被再加了！");
//            return;
//        }
//        group.add(this.userId);
//    }
//
//    /**
//     * 连接建立成功调用的方法
//     */
//    @OnOpen
//    public void onOpen(Session session, @PathParam("userId") String userId) throws IOException {
//        this.session = session;
//        this.userId = userId;
//        if (webSocketMap.containsKey(userId)) {
//            session.getBasicRemote().sendText(getRstStr("0", "服务器", userId + "已登录，将被强制下线！", getNowTimeStr()));
//            webSocketMap.get(userId).sendMessage(getRstStr("0", "服务器", "你已被强制下线！", getNowTimeStr()));
//            webSocketMap.get(userId).session.close();
//            webSocketMap.remove(userId);
//            webSocketMap.put(userId, this);
//            //加入set中
//        } else {
//            webSocketMap.put(userId, this);
//            onLineUserList.add(userId);
//            //加入set中
//            addOnlineCount();
//            //在线数加1
//        }
//
//        log.debug("用户连接:" + userId + ",当前在线人数为:" + getOnlineCount());
//
//        try {
//            sendMessage(getRstStr("1", "服务器", "连接成功", getNowTimeStr()));
//
//            // 推送离线消息
//            sendNotOnLineMsg(userId);
//        } catch (IOException e) {
//            log.error("用户:" + userId + ",网络异常!!!!!!");
//        }
//    }
//
//    /**
//     * 连接关闭调用的方法
//     */
//    @OnClose
//    public void onClose() {
//        if (webSocketMap.containsKey(userId)) {
//            webSocketMap.remove(userId);
//            onLineUserList.remove(userId);
//            //从set中删除
//            subOnlineCount();
//        }
//        log.debug("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
//    }
//
//    /**
//     * 收到客户端消息后调用的方法
//     *
//     * @param message 客户端发送过来的消息
//     */
//    @OnMessage
//    public void onMessage(String message, Session session) {
//        log.debug("用户消息:" + userId + ",报文:" + message);
//        //可以群发消息
//        //消息保存到数据库、redis
//        if (!StringUtils.isEmpty(message)) {
//            try {
//                //解析发送的报文
//                JSONObject jsonObject = JSON.parseObject(message);
//                //追加发送人(防止串改)
//                jsonObject.put("fromUserId", this.userId);
//                String toUserId = jsonObject.getString("toUserId");
//                String toGroupId = jsonObject.getString("toGroupId");
//                if (org.apache.commons.lang3.StringUtils.isNotBlank(toGroupId)) {
//                    joinGroup(toGroupId);
//                    sendGroupMsg(toGroupId, jsonObject.getString("msg"));
//                } else {
//                    //传送给对应toUserId用户的websocket
//                    if (!StringUtils.isEmpty(toUserId) && webSocketMap.containsKey(toUserId)) {
//                        webSocketMap.get(toUserId).sendMessage(getRstStr("1", this.userId, jsonObject.getString("msg"), getNowTimeStr()));
//                    } else {
//                        log.error("请求的userId:" + toUserId + "不在该服务器上");
//                        webSocketMap.get(userId).sendMessage(getRstStr("0", "服务器", toUserId + "不在线", getNowTimeStr()));
//                        saveNotOnLineMsg(toUserId, this.userId, jsonObject.getString("msg"), formatDate(LocalDateTime.now()));
//                        //否则不在这个服务器上，发送到mysql或者redis
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * @param session
//     * @param error
//     */
//    @OnError
//    public void onError(Session session, Throwable error) {
//        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
//        error.printStackTrace();
//    }
//
//    /**
//     * 实现服务器主动推送
//     */
//    public synchronized void sendMessage(String message) throws IOException {
//        this.session.getBasicRemote().sendText(message);
//    }
//
//    public static void setHearBeatMsg() {
//        webSocketMap.forEach((userId, ws) -> {
//            try {
//                ws.sendMessage(getRstStr("3", "服务器", "状态", ""));
//            } catch (IOException e) {
//                log.error("心跳异常", e);
//            }
//        });
//    }
//
//
//    /**
//     * 发送自定义消息
//     * @return
//     */
//    public static String sendInfo(String message, String userId) throws IOException {
//        if (!StringUtils.isEmpty(userId) && webSocketMap.containsKey(userId)) {
//            webSocketMap.get(userId).sendMessage(getRstStr("4", "服务器", message, webSocketMap.get(userId).getNowTimeStr()));
//            return "发送消息【" + message + "】给: " + userId;
//        } else {
//            return "用户: " + userId + ",不在线！";
//        }
//    }
//
//    public static synchronized int getOnlineCount() {
//        return onlineCount;
//    }
//
//    public static synchronized void addOnlineCount() {
//        WebsocketServer.onlineCount++;
//    }
//
//    public static synchronized void subOnlineCount() {
//        WebsocketServer.onlineCount--;
//    }
//
//    private static String getRstStr(String rstCode, String fromUserId, String msg, String sendTime) {
//        JSONObject rst = new JSONObject();
//        rst.put("fromUserId", fromUserId);
//        rst.put("rstcode", rstCode);
//        rst.put("msg", msg);
//        rst.put("sendTime", sendTime);
//        return rst.toJSONString();
//    }
//
//    //存放离线消息map
//    private static Map<String, List<String>> notOnLineMsg = new ConcurrentHashMap<>();
//
//    /**
//     * 保存离线消息
//     *
//     * @param toUserId
//     * @param fromUserId
//     * @param msg
//     * @param sendTime
//     */
//    public void saveNotOnLineMsg(String toUserId, String fromUserId, String msg, String sendTime) {
//        List<String> msgList = notOnLineMsg.get(toUserId);
//        if (CollectionUtils.isEmpty(msgList)) {
//            msgList = new LinkedList<>();
//        }
//        msgList.add(getRstStr("2", fromUserId, msg, sendTime));
//        notOnLineMsg.put(toUserId, msgList);
//    }
//
//    /**
//     * 发送离线消息
//     *
//     * @param userId
//     */
//    private void sendNotOnLineMsg(String userId) {
//        try {
//            List<String> msgList = notOnLineMsg.get(userId);
//            if (CollectionUtils.isNotEmpty(msgList)) {
//                Iterator<String> it = msgList.iterator();
//                while (it.hasNext()) {
//                    sendMessage(it.next());
//                    it.remove();
//                }
//            }
//        } catch (IOException e) {
//            log.error("发送离线消息异常！！！", e);
//        }
//    }
//
//    private String formatDate(LocalDateTime dateTime) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        return dateTime.format(formatter);
//    }
//
//    /**
//     * 获取当前时间字符串
//     * @return
//     */
//    private String getNowTimeStr() {
//        return formatDate(LocalDateTime.now());
//    }
//
//    /**
//     * 获取在线用户
//     * @return
//     */
//    public static List<String> getOnLineUserList() {
//        return onLineUserList;
//    }
//
//    /**
//     * T用户
//     * @param userId
//     */
//    public static boolean closeUser(String userId) {
//        try {
//            WebsocketServer ws = webSocketMap.get(userId);
//            if (ws == null) {
//                log.warn("用户未连接！userID: " + userId);
//                return false;
//            }
//            CloseReason reason = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "管理员强制下线用户");
//            ws.session.close(reason);
//            return true;
//        } catch (IOException e) {
//            log.error("强制下线用户异常！！！", e);
//        }
//        return false;
//    }
//
//    /**
//     * 发送群消息
//     * @param groupName
//     * @param msg
//     * @throws IOException
//     */
//    public void sendGroupMsg(String groupName, String msg) throws IOException {
//        List<String> group = groupMap.get(groupName);
//        if (group == null) {
//            log.error("该群[{}]不存在", groupName);
//            return;
//        }
//        if (org.apache.commons.lang3.StringUtils.isBlank(msg)) {
//            log.warn("消息为空！！！不推送");
//            return;
//        }
//        for (String userId : group) {
//            if (userId.equals(this.userId)) {
//                continue;
//            }
//            WebsocketServer ws = webSocketMap.get(userId);
//            ws.sendMessage(getRstStr("1", this.userId, msg, getNowTimeStr()));
//        }
//    }
//}
