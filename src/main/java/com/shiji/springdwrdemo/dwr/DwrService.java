//package com.shiji.springdwrdemo.dwr;
//
//import org.directwebremoting.Browser;
//import org.directwebremoting.ScriptBuffer;
//import org.directwebremoting.ScriptSession;
//import org.directwebremoting.WebContextFactory;
//import org.directwebremoting.annotations.RemoteMethod;
//import org.directwebremoting.annotations.RemoteProxy;
//import org.springframework.stereotype.Service;
//
//import java.util.Collection;
//
//@Service
//@RemoteProxy
//public class DwrService {
//
//    @RemoteMethod
//    public String hello(){
//        return "Hello DWR!!!";
//    }
//
//    @RemoteMethod
//    public String echo(String str) {
//        return "Echo: " + str;
//    }
//
//    //保存scriptSession ， 这个方法需要在页面刚已加载的时候调用，为了前端和后端建立连接。
//    @RemoteMethod
//    public void onPageLoad(String tag) throws InterruptedException {
//        //获取当前的ScriptSession
//        try {
//            ScriptSession scriptSession =  WebContextFactory.get().getScriptSession();
//            if(scriptSession != null){
//                scriptSession.setAttribute("key", tag);
//            }
//            DwrScriptSessionManagerUtil dwrScriptSessionManagerUtil = new DwrScriptSessionManagerUtil() ;
//            dwrScriptSessionManagerUtil.init("key",tag);
//        } catch (Exception e) {
//
//        }
//        System.out.println("onPageLoad 被调用 ：" + tag);
//
//        for (int i = 0; i < 5; i++) {
//            Thread.sleep(1000);
//            onMessage((i + 1) + "：消息");
//        }
//    }
//
//    private void onMessage(String msg) {
//        System.out.println("接收到消息：" + msg);
//
//        Browser.withAllSessionsFiltered(scriptSession -> {
//            // 这块判断是否合法 ，可以在这块验证用户的合法性，为了简单我直接返回true
//            String key = (String) scriptSession.getAttribute("key");
//            if ("123".equals(key)) {
//                return true;
//            }
//            return false;
//        }, new Runnable() {
//            private ScriptBuffer script = new ScriptBuffer();
//            @Override
//            public void run() {
//                //设定前台接收消息的方法和参数  在前台js里定义getmessage (data) 的方法，就会自动被调用
//                script.appendCall("getmessage", msg);
//                Collection<ScriptSession> sessions = Browser.getTargetSessions();
//                for (ScriptSession scriptSession : sessions) {
//                    scriptSession.addScript(script);
//                }
//                System.out.println("dwrtool  showmessage 调用 ");
//            }
//        });
//    }
//
//}
