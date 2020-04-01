// 是否打开通知
var openNotice = true;

// 窗口可见
var visible = true;
// 是否打开提示音
var opendSound = true;

var title = document.title;
// 用户名
var username = '';
// 用户id
var uid = null;

window.onload = function () {
    var userInfo = JSON.parse(window.localStorage.getItem("userInfo"));
    $('#userName').val(userInfo.username);
    uid = userInfo.userId;

    // 监听窗口切换
    document.addEventListener("visibilitychange", function () {
        if (document.visibilityState === "hidden") {
            // 窗口不可见
            visible = false;
        } else if (document.visibilityState === "visible") {
            // 窗口可见
            visible = true;
            document.title = title;
        }
    });

    // 页面加载完成监听回车事件
    document.getElementById("msg-need-send").addEventListener("keydown", function (e) {
        if (e.keyCode != 13) return;
        e.preventDefault();
        // 发送信息
        sendMsg('group');
    });
}

// var vConsole = new VConsole();

var stompClient = null;
var onlineUserList = [];

function connect() {
    var socket = new SockJS('/stomp-websocket');
    stompClient = Stomp.over(socket);

    // 每隔30秒做一次心跳检测
    stompClient.heartbeat.outgoing = 30000;
    // 客户端不接收服务器的心跳检测
    stompClient.heartbeat.incoming = 0;

    var user = {
        'username': username,
        'avatar': 'https://whycode.icu/user.jpeg',
        'address': "地球村"
    };
    stompClient.connect(user, function (frame) {
        $('#openSocket').attr("disabled", true);
        console.log('Connected: ' + frame);
        uid = frame.headers['user-name'];
        user.userId = uid;
        window.localStorage.setItem("userInfo", JSON.stringify(user));

        if (uid === undefined) {
            alert("建立连接失败，请重新连接！");
        }

        //订阅用户状态
        stompClient.subscribe('/topic/status', function (data) {
            data = JSON.parse(data.body).data;
            handleMessage(data);
            //刷新在线列表
            flushOnlineGroup(data);
        });

        //订阅发给自己的消息
        stompClient.subscribe('/user/' + uid + '/chat', function (data) {
            data = JSON.parse(data.body).data;
            handleMessage(data);
        });

        //订阅聊天室的消息
        stompClient.subscribe('/topic/chatRoom', function (data) {
            data = JSON.parse(data.body).data;
            handleMessage(data);
        });

        // 错误信息订阅
        stompClient.subscribe('/user/' + uid + '/error', function (data) {
            getData(data.body);
        });

        stompClient.onclose = function (e) {
            alert("掉线了！！！")
        }
    });

}

/**
 * 处理消息
 * @param data
 */
function handleMessage(data) {
    var msg = data.message;
    switch (data.type) {
        case 'USER':
            showUserMsg(data);
            break;
        case 'SYSTEM':
            showSystemMsg(msg);
            break;
        case 'REVOKE':
            showRevokeMsg(data);
            break;
        case 'ROBOT':
            showRobotMsg(data);
            break;
        default:
            break;
    }

    // 消息通知
    msgNotice(data);
}

function showSystemMsg(msg) {
    var msg = `<li style="color: #999; font-size: 0.22rem; text-align: center">${"系统消息: " + msg}</li>`;
    showMsg(msg);
}

function showUserMsg(data) {
    var msg = null;
    var content = data.message;
    if (data.image != null) {
        content = `<img style="width: 5rem" src="${data.image}"/>`
    }
    if (uid === data.user.userId) {
        data.user.username = '我';
        msg = `<li class="con-li flex-row user-li"><div class="li-info"><div class="info-name">${data.user.username}</div><div ondblclick="revokeMsg(this)" class="li-content" style="background-color: lightgreen" receiver="${data.receiver}" id="${data.messageId}">${content}</div></div><img src="images/avator.png" class="li-avator" ></li>`;
    } else {
        msg = `<li class="con-li flex-row"><img src="images/avator.png" class="li-avator" ><div class="li-info"><div class="info-name">${data.user.username}</div><div class="li-content" receiver="${data.receiver}" id="${data.messageId}">${content}</div></div></li>`;
    }

    showMsg(msg);
}

function showRevokeMsg(data) {
    var obj = document.getElementById(data.revokeMessageId);
    obj.innerHTML = "<span style='color: gray'>已撤回...</span>";
}

/**
 * 解析响应数据
 * @param data
 * @returns {*}
 */
function getData(data) {
    var obj = JSON.parse(data);
    codeMapping(obj);
    return obj.data;
}

/**
 * 响应码映射
 * @param date
 */
function codeMapping(date) {
    switch (date.code) {
        case 200:
            break;
        case 404:
            alert("404");
            break;
        default:
            alert(date.desc);
            break;
    }
}

function flushOnlineGroup(data) {
    onlineUserList = data.onlineUserList;
    console.log(onlineUserList);

    $(".online-layer .line-ul").empty();
    for (let index in onlineUserList) {
        if (onlineUserList[index].userId === uid) {
            $(".online-layer .line-ul").append(`<li class="line-li bdb-1px">我</li>`)
        } else {
            $(".online-layer .line-ul").append(`<li class="line-li bdb-1px">${onlineUserList[index].username}</li>`)
        }
    }
}

/**
 * 跳到聊天界面最底下
 */
function jumpToLow() {
    var roomUl = document.getElementById("room-ul");
    var roomContainer = document.getElementById("room-container");
    roomContainer.scrollTo({
        top: roomUl.scrollHeight,
        behavior: "smooth"
    })
}

function disConnect() {
    // 客户端主动关闭连接
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    $('#openSocket').attr("disabled", false);
}

function showMsg(data) {
    $('.room-ul').append(data);
    jumpToLow();
}

function sendMsg(type) {
    // 获取发送的内容
    var content = $("#msg-need-send").val();
    // 内容不能为空
    if (content.trim().length < 1) {
        return;
    }

    var data = {
        "message": content
    };
    var pub = '/chatRoom';
    if ('user' === type) {
        pub = '/chat';
        data.receiver = [$("#toUserId").val(), uid];
    }

    data = JSON.stringify(data);
    sendMessage(pub, {}, data);

    $("#msg-need-send").val('');
}

function revokeMsg(e) {
    var dom = $(e);
    var messageId = dom.attr("id");
    var receiver = dom.attr("receiver")

    if (receiver === null || receiver === '' || receiver === 'null' || messageId === undefined) {
        receiver = null;
    } else {
        receiver = receiver.split(',');
    }

    var data = JSON.stringify({
        'messageId': messageId,
        'receiver': receiver
    });

    sendMessage('/chatRoom/revoke', {}, data);
}

/**
 * 发送信息到指定地址
 * @param pub 发布地址
 * @param header 设置请求头
 * @param data 发送的内容
 */
function sendMessage(pub, header, data) {
    stompClient.send(pub, header, data);
}

/**
 * 通过name获取userid
 * @param name
 * @returns {Document.userId|string}
 */
function getUserIdByName(name) {
    if (name == '') {
        return '';
    }

    for (var i = 0; i < onlineUserList.length; i++) {
        var obj = onlineUserList[i];
        if (obj.userId !== uid && obj.username === name) {
            return obj.userId;
        }
    }
}

/**
 * 选择文件
 */
function selectFile() {
    $('#file').click();
}

function sendImage(id) {
    var image = $("#" + id).val();
    if (image === '' || image === undefined) {
        return;
    }

    var filename = image.replace(/.*(\/|\\)/, "");
    var fileExt = (/[.]/.exec(filename)) ? /[^.]+$/.exec(filename.toUpperCase()) : '';

    var file = $('#' + id).get(0).files[0];
    var fileSize = file.size;
    var mb = 30;
    var maxSize = mb * 1024 * 1024;

    if (fileExt != 'PNG' && fileExt != 'GIF' && fileExt != 'JPG' && fileExt != 'JPEG' && fileExt != 'BMP') {
        alert('发送失败，图片格式有误！');
        return;
    } else if (parseInt(fileSize) > parseInt(maxSize)) {
        alert('上传的图片不能超过' + mb + 'MB');
        return;
    } else {
        var data = new FormData();
        data.append('file', file);
        $.ajax({
            url: "/api/upload/image",
            type: 'POST',
            data: data,
            dataType: 'JSON',
            cache: false,
            processData: false,
            contentType: false,
            success: function (data) {
                codeMapping(data);
                var rep = data.data;
                if (id === 'file') {
                    sendImageToChatRoom(rep.path);
                } else {
                    // todo 设置头像
                }
                $("#" + id).val('');
            }
        });
    }
}

/**
 * 发送图片到聊天室
 */
function sendImageToChatRoom(image) {
    var data = JSON.stringify({
        "image": image
    });
    sendMessage('/chatRoom', {}, data);
}

/**
 * 消息通知
 * @param data
 */


function msgNotice(data) {
    // 已开启通知且窗口不可见才进行消息通知
    if (openNotice && !visible) {
        // 通过标题通知
        msgNoticeByTitle();
        // 通过浏览器的消息通知支持进行通知
        // msgNoticeByBrowser(data);
    }
}

function msgNoticeByTitle() {
    if (!openNotice || visible) {
        // 未开启通知或窗口可见，不进行提醒
        return;
    }

    if (opendSound) {
        // 提示音
        beep();
    }
    // 窗口不可见显示提醒
    document.title = '有新消息啦！！！';
}

/**
 * 播放提示音
 */
function beep() {
    var beep = document.getElementById('beep');
    beep.play();
}


/*      志熊js代码          */


function hideMask() {
    $('.mask').hide()
    $('.online-layer').removeClass('show-layer')
}

$(document).ready(function () {
    $('.room-head .head-msg').on('click', function () {
        $('.mask').show()
        $('.online-layer').addClass('show-layer')
    })

    $('.menu-add').on('click', function () {
        $('.chat-bot .menu-layer').toggleClass('show-layer')
    })

    $('.enter-sec .sec-btn').on('click', function () {
        username = $('.enter-sec .sec-input').val().trim();
        if (username === '') {
            return
        } else {
            let userList = window.localStorage.getItem('userList')
            if (userList != null) {
                userList = JSON.parse(userList)
                if (userList.length > 0) {
                    const checkUser = () => {
                        for (let i in userList) {
                            if (userList[i].name === username) {
                                return true
                            }
                            return false
                        }
                    }
                    if (!checkUser()) {
                        userList.push({
                            id: userList.length + 1,
                            name: username
                        })
                    }
                }
            } else {
                userList = [{id: 1, name: username}]
            }
            window.localStorage.setItem('userList', JSON.stringify(userList))
            window.localStorage.setItem('customName', JSON.stringify(username))
            $(".page-pre-enter").addClass('hide-enter')
            connect()
        }
    })
})
