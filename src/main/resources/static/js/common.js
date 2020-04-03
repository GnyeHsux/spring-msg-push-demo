// 是否打开通知
let openNotice = true;

// 窗口可见
let visible = true;
// 是否打开提示音
let opendSound = true;

let title = document.title;
// 用户名
let username = '';
// 用户id
let uid = null;

window.onload = function () {
    
    let userInfo = JSON.parse(window.localStorage.getItem("userInfo"));
    if (userInfo !== null) {
        $('#userName').val(userInfo.username);
        $("#avatar").attr("src", userInfo.avatar);
    }

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
        sendMsg();
    });
}

// let vConsole = new VConsole();

let stompClient = null;
let onlineUserList = [];

function connect() {
    let socket = new SockJS('/stomp-websocket');
    stompClient = Stomp.over(socket);

    // 每隔30秒做一次心跳检测
    stompClient.heartbeat.outgoing = 30000;
    // 客户端不接收服务器的心跳检测
    stompClient.heartbeat.incoming = 0;

    let user = {
        'username': username,
        'avatar': $("#avatar")[0].src,
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
    let msg = data.message;
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

function showSystemMsg(msgStr) {
    let msg = `<li style="color: #999; font-size: 0.22rem; text-align: center">${"系统消息: " + msgStr}</li>`;
    showMsg(msg);
}

function showUserMsg(data) {
    let msg = null;
    let content = data.message;
    if (data.image != null) {
        content = `<img style="width: 5rem" src="${data.image}" onclick="showImage(this)"/>`
    }
    let sendTime = data.sendTime;
    let messageId = data.messageId;
    if (data.offlineMsg) {
        sendTime = data.realSendTime;
        messageId = data.realMessageId;
    }
    if (uid === data.user.userId) {
        msg = `<li class="con-li flex-row user-li"><div class="li-info"><div class="info-name"><span style="margin-right: .1rem;">${sendTime}</span>${data.user.username}</div><div ondblclick="revokeMsg(this)" class="li-content" style="background-color: lightgreen" receiver="${data.receiver}" id="${messageId}">${content}</div></div><img src="${data.user.avatar}" class="li-avator" ></li>`;
    } else {
        msg = `<li class="con-li flex-row"><img src="${data.user.avatar}" class="li-avator" ><div class="li-info"><div class="info-name">${data.user.username}<span style="margin-left: .1rem;">${sendTime}</span></div><div class="li-content" receiver="${data.receiver}" id="${data.messageId}">${content}</div></div></li>`;
    }

    showMsg(msg);
}

function showRevokeMsg(data) {
    let obj = document.getElementById(data.revokeMessageId);
    obj.innerHTML = "<span style='color: gray'>已撤回...</span>";
}

/**
 * 解析响应数据
 * @param data
 * @returns {*}
 */
function getData(data) {
    let obj = JSON.parse(data);
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

    let userList = window.localStorage.getItem('chatuserList');
    if (userList == null) {
        userList = [{id: uid, name: username}]
    } else {
        userList = JSON.parse(userList);
    }

    $(".online-layer .line-ul").empty();
    for (let index in onlineUserList) {
        if (onlineUserList[index].userId === uid) {
            $(".online-layer .line-ul").append(`<li class="line-li bdb-1px">我</li>`)
        } else {
            $(".online-layer .line-ul").append(`<li class="line-li bdb-1px" userId="${onlineUserList[index].userId}">${onlineUserList[index].username}</li>`)
        }

        let user = userList.find(function (item) {
            return item.id === onlineUserList[index].userId;
        });
        console.log("user --->>>> " + user);
        if (user == null) {
            userList.push({
                id: onlineUserList[index].userId,
                name: onlineUserList[index].username
            })
        } else {
            user.name = onlineUserList[index].username;
        }
    }
    window.localStorage.setItem('chatuserList', JSON.stringify(userList))
}

/**
 * 跳到聊天界面最底下
 */
function jumpToLow() {
    let roomUl = document.getElementById("room-ul");
    let roomContainer = document.getElementById("room-container");
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
}

function showMsg(data) {
    $('.room-ul').append(data);
    jumpToLow();
}

function sendMsg() {
    let type = 'group';
    // 获取发送的内容
    let content = $("#msg-need-send").val();
    // 内容不能为空
    if (content.trim().length < 1) {
        return;
    }

    let toUser = [uid];
    let names = content.split('@');
    console.log("names: " + names);

    for (let name of names) {
        let index = name.indexOf(' ');
        let userId = getUserIdByName(name.substr(0, index !== -1 ? index : name.length));
        // userId不能是空的，且toUser数组中不存在该userId
        if (userId !== undefined && userId !== '' && toUser.indexOf(userId) === -1) {
            toUser.push(userId);
        }
    }

    let data = {
        "message": content
    };
    let pub = '/chatRoom';
    console.log(toUser)
    if (toUser.length > 1) {
        pub = '/chat';
        data.receiver = toUser;
    }

    data = JSON.stringify(data);
    console.log(data);
    sendMessage(pub, {}, data);

    $("#msg-need-send").val('');
}

function revokeMsg(e) {
    let dom = $(e);
    let messageId = dom.attr("id");
    let receiver = dom.attr("receiver")

    if (receiver === null || receiver === '' || receiver === 'null' || messageId === undefined) {
        receiver = null;
    } else {
        receiver = receiver.split(',');
    }

    let data = JSON.stringify({
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
    if (name === '') {
        return '';
    }
    let userList = window.localStorage.getItem('chatuserList');
    userList = JSON.parse(userList);

    for (let i = 0; i < userList.length; i++) {
        let obj = userList[i];
        if (obj.id !== uid && obj.name === name) {
            return obj.id;
        }
    }
}

/**
 * 选择文件
 */
function selectFile(id) {
    $('#' + id).click();
}

function sendImage(id) {
    let image = $("#" + id).val();
    if (image === '' || image === undefined) {
        return;
    }

    let filename = image.replace(/.*(\/|\\)/, "");
    let fileExt = (/[.]/.exec(filename)) ? /[^.]+$/.exec(filename.toUpperCase()) : '';

    let file = $('#' + id).get(0).files[0];
    let fileSize = file.size;
    let mb = 30;
    let maxSize = mb * 1024 * 1024;

    if (fileExt != 'PNG' && fileExt != 'GIF' && fileExt != 'JPG' && fileExt != 'JPEG' && fileExt != 'BMP') {
        alert('发送失败，图片格式有误！');
        return;
    } else if (parseInt(fileSize) > parseInt(maxSize)) {
        alert('上传的图片不能超过' + mb + 'MB');
        return;
    } else {
        let data = new FormData();
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
                let rep = data.data;
                if (id === 'file') {
                    sendImageToChatRoom(rep.path);
                } else {
                    $("#avatar").attr("src", rep.path);

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
    let data = JSON.stringify({
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
    let beep = document.getElementById('beep');
    beep.play();
}


/*      志熊js代码          */
function hideMask() {
	$('.mask').hide()
	$('.online-layer').removeClass('show-layer')
}

$(document).ready(function () {
	
	var self = this;
	var longClick =0;
	$(".li-info .li-content").on({
	    touchstart: e=>{
			console.log($(e).text())
			console.log('touchstart')
	        longClick=0;//设置初始为0
	        timeOutEvent = setTimeout(()=>{
	            //此处为长按事件-----在此显执行撤销
	            longClick=1;//假如长按，则设置为1
				$(e).remove()
	        },500);
	    },
	    touchmove: () =>{
			console.log('touchmove')
	        clearTimeout(timeOutEvent);
	        timeOutEvent = 0;
	        e.preventDefault();
	    },
	    touchend: e=>{
			console.log('touchend')
	        clearTimeout(timeOutEvent);
	        if(timeOutEvent!=0 && longClick==0){//点击
	            //此处为点击事件
	        }
	        return false;
	    }
	});
	
	/*$('.room-ul .content-img').on('click',function(){	// 预览图片
		const docEl = document.documentElement
		let cwidth = docEl.clientWidth
		let cheight = docEl.clientHeight
		let imgObj = $(this).attr('src')
		
		$("<img/>").attr("src", imgObj).on('load',function(){
			let imgW = this.width,imgH = this.height;
			let preImg = $('.img-preview .pre-img')
			if((cwidth/cheight) >= (imgW/imgH)){
				preImg.css('width',(cheight*imgW)/imgH)
				preImg.css('height',cheight)
			} else{
				preImg.css('width',cwidth)
				preImg.css('height',(cwidth*imgH)/imgW)
			}
			
			$('.img-preview .pre-img').attr('src',$(this).attr('src'))
			$('.img-preview').show()
		})
	})*/
	$('.img-preview').on('click',function(){
		$(this).hide()
	})
	
	
	
	
	
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
            window.localStorage.setItem('customName', JSON.stringify(username))
            $(".page-pre-enter").addClass('hide-enter')
            connect()
        }
    })
});


function showImage(obj){	// 预览图片
    const docEl = document.documentElement;
    let cwidth = docEl.clientWidth;
    let cheight = docEl.clientHeight;
    let imgObj = $(obj).attr('src');

    $("<img/>").attr("src", imgObj).on('load',function(){
        let imgW = this.width,imgH = this.height;
        let preImg = $('.img-preview .pre-img')
        if((cwidth/cheight) >= (imgW/imgH)){
            preImg.css('width',(cheight*imgW)/imgH)
            preImg.css('height',cheight)
        } else{
            preImg.css('width',cwidth)
            preImg.css('height',(cwidth*imgH)/imgW)
        }

        $('.img-preview .pre-img').attr('src',$(obj).attr('src'))
        $('.img-preview').show()
    })
}