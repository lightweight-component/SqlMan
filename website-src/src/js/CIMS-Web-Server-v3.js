$(document).ready(function(){
    //判断跨浏览器插件是否启用
    $.ajax({
        type: "GET",
        url: "rest/v1/crossBrowser/getSwitch",
        async: false,
        success: function (obj) {
            if(obj != null && obj.hasOwnProperty("enabled") && obj.enabled == 1){
                //请求插件用户认证状态
                $.ajax({
                    type: "GET",
                    url: "http://127.0.0.1:19401/sso/get_trusfort_user?random=" + Math.random(),
                    async: false,
                    timeout : obj.timeout == null ? 100 : obj.timeout,
                    success: function (result) {
                        if(result.status == 0){
                            var uuid = result.resp.uuid;
                            //插件有用户认证
                            if(result.resp.login == 1) {
                                //获取未认证token
                                $.ajax({
                                    type: 'POST',
                                    url: 'rest/v1/crossBrowser/getToken',
                                    async: false,
                                    data: JSON.stringify({
                                        uuid: uuid
                                    }),
                                    dataType: 'json',
                                    contentType: "application/json; charset=utf-8",
                                    success: function (res) {
                                        if (res.status == 1000) {
                                            var token = res.response_body.token;
                                            waitCrossAuthResult(defaulteStatusCode, token);
                                            //请求插件认证接口
                                            $.ajax({
                                                type: 'POST',
                                                url: "http://127.0.0.1:19401/sso/token",
                                                async: false,
                                                data: JSON.stringify({"token": token}),
                                                dataType: 'json',
                                                success: function (d) {
                                                    if (d.status == 0) {
                                                        console.log("请求插件认证接口成功")
                                                    }
                                                },
                                                error: function () {
                                                    layer.msg(res.status, {icon: 2});
                                                }
                                            });
                                        }
                                    },
                                    error: function (res) {
                                        layer.msg(res.status, {icon: 2});
                                    }
                                });
                            }
                            $("#crossUuid").val(uuid);
                        }else{
                            console.log("请求插件用户认证状态接口异常")
                        }
                    }
                });
            }
        }
    });

    initImg();
    /*获取url后面的参数*/
    var params = {};
    function parseQueryString(url) {
        var params = {};
        var arr = url.split("?");
        if(arr.length < 2) return params;
        var arr1 = arr[1].split("&");
        for(var i = 0; i < arr1.length; i++) {
            arr2 = arr1[i].split('=');
            if(!arr2[1]) {
                params[arr2[0]] = 'true';
            } else {
                if(params[arr2[0]]) {
                    var arr3 = [params[arr2[0]]];
                    arr3.push(arr2[1]);
                    params[arr2[0]] = arr3;
                } else {
                    params[arr2[0]] = decodeURIComponent(arr2[1]);
                }
            }
        }
        return params;
    }
    var url = window.location.href;
    params = parseQueryString(url);
    if(params.reAuthError=='true'){
        goQrpage($.i18n.prop('v301'));
    }else if(params.reAuthError=='invalid'){
        goQrpage($.i18n.prop('v302'));
    }
    var defaulteStatusCode = '9009';                      //默认状态码
    var isQrAuthWaiting = false;
    /*将内容放到input里面*/
    function autoinputval() {
        $("input[name='sign']").val(params.sign);
        $("input[name='token']").val(params.token);
        $("input[name='parent']").val(params.parent);
        if (params.username) {
            $("input[name='username']").val(params.username).attr("readonly",true);
        }
    }
    autoinputval();
    function getUrl(name) {
        var reg = new RegExp("[^\?&]?" + encodeURI(name) + "=[^&]+");
        var arr = window.parent.document.getElementById("qrcont").contentWindow.location.search.match(reg);
        if (arr != null) {
            return decodeURI(arr[0].substring(arr[0].search("=") + 1));
        }
        return "";
    }
    //长连接查询token的状态
    function waitCrossAuthResult(status_,token_){

        var ajaxOpt = {
            method: 'POST',
            type  : 'json',
            url:'rest/v1/crossBrowser/verifyToken',
            data:{
                token: token_,
                sign : getUrl("sign")
            },
            success: function(data){

                if(data.status == 1000){
                    go(data.response_body.usersign);
                }else if (data.status == 9020) {
                    waitCrossAuthResult(status_, token_);
                }
            },
            error: function(XMLHttpRequest, textStatus, errorThrown){}
        };
        $.ajax(ajaxOpt);
    }

    //获取默认登录方式
    function obtainqrcode() {
        var $url = decodeURIComponent(params.parent);
        if($url) {
            var len = $url.indexOf('service=');
            if (len > -1) {//说明是cas对接
                $url = $url.substring(len + 8);
            } else {
                $url = "";
            }
        }

        var sendData = {
            type: params.type,
            sign: params.sign,
            username: params.username,
            urlCode: $url
        };

        if (params.userSign) {
            sendData.userSign = params.userSign;
        }

        if($("#crossUuid").val()){
            sendData.uuid = $("#crossUuid").val();
        }

        $.ajax({
            type: 'GET',
            url: 'api/verify/getqrcode',
            async: false,
            data: sendData,
            dataType: 'json',
            success: function(res) {
                if(res.status==1000){
                    var token = res.token;
                    $("#token").val(token);

                    //企业微信二维码前端调用方式
                    /*window.WwLogin({
                        "id" : "loginways13",
                        "appid" : "wx466de4d2ccef6fc1",
                        "agentid" : "1000073",
                        "redirect_uri" :"http%3A%2F%2Fxdauth.ncut.edu.cn%2Fauthn%2Fapi%2Fqywx%2Fwxlogin",
                        "state" : token,
                        "href" : "",
                    });

                    window.WwLogin({
                        "id" : "otherways13",
                        "appid" : "wx466de4d2ccef6fc1",
                        "agentid" : "1000073",
                        "redirect_uri" :"http%3A%2F%2Fxdauth.ncut.edu.cn%2Fauthn%2Fapi%2Fqywx%2Fwxlogin",
                        "state" : token,
                        "href" : "",
                    });*/

                    var loginways = res.authtypes.split(",");
                    switch(Number(res.policy)){
                        case 0:
                        case 1:
                            var src = "api/qrcode/";
                            var imgsrc = src + token;
                            $('#qrcodeImg1').attr('src', imgsrc);
                            $('#qrcodeImg2').attr('src', imgsrc);
                            $('#qrcodeImg3').attr('src', imgsrc);

                            $("#loginways" + loginways[0]).show();
                            if (loginways[0] == 0 || loginways[0] == 9 || loginways[0] == 11 || loginways[0] == 12) {
                                waitAuthResult(defaulteStatusCode, null,0);
                            }
                            if(params.username!=undefined){
                                $("#otherways" + loginways[0]).addClass("active");
                                if(loginways.length > 1) {
                                    $("#moreways").show();
                                    for (i = 0; i < loginways.length; i++) {
                                        if (i == 0) {
                                            $("#otherwaysButton" + loginways[i]).addClass("active");
                                        }
                                        $("#otherwaysButton" + loginways[i]).show();
                                    }
                                }
                            }else{
                                if(loginways.length > 1) {
                                    $("#moreways").show();
                                    for (i = 1; i < loginways.length; i++) {
                                        if (i == 1) {
                                            $("#otherwaysButton" + loginways[i]).addClass("active");
                                            $("#otherways" + loginways[i]).addClass("active");
                                        }
                                        $("#otherwaysButton" + loginways[i]).show();
                                    }
                                }
                            }

                            if(res.wx_qr_uri) {
                                $('.weixinUrl').attr('href',res.wx_qr_uri);
                            }
                            if(res.dingtalk_qr_uri) {
                                $('.dingtalkUrl').attr('href',res.dingtalk_qr_uri);
                            }
                            $('.adAuthUrl').attr('href', "/ad-auth/auth?adToken=" + token);
                            break;
                        case 2://选择双因子认证策略
                            $("#policy1").addClass("conlogin-rightout-display");
                            $("#policy2").show();
                            $("#loginways_1_" + loginways[1]).show();
                            $("input[name='doubleMobileVerify']").val(loginways[1]);
                            break;
                    }
                } else if (res.status === "9900"){
                    $.postMessage('HREF_PAGE|' + res.appUrl, res.appUrl);
                } else if (res.status === "9001") {
                    goQrpage($.i18n.prop('v303'));
                }else{
                    layer.msg(res.status,{icon:2});
                }

            },
            error: function() {
                layer.msg(res.status,{icon:2});
            }

        });
    }
    obtainqrcode();

    //9009 9999
    initParameter();  //初始化全局参数

    var voiceCallTelNumber ="";                         //有bug会导致拔打电话时不显示电话号码，注释掉

    var operationRetrytime       = 60;                 // 按钮操作倒计时重试时间
    var bigBtnWaitMsg            = '';        // 全局消息
    var signExpireTime           = 280;                // 秒  要小于签名过期时间300秒
    var signedPageIsExpired      = false;              // 页面签名是否过期，当过期时该值设为true
    var ajaxTimeOut              = 0;   // 请求超时时间
    var debug                    = false;
    var appName                  = '善认';
    reloadPage();                                    // 启动倒计时刷新

    //同步所有input value
    $("input[name='username']").change(function() {
        $("input[name='username']").val($(this).val());
    });


    function goQrpage(msg){
        layer.open({
            type: 1,
            content: "<div style='margin:20px'>" + msg + "</div>",
            closeBtn: 0,
            btn: ["确定"],
            yes: function(index) {
                layer.close(index);
                if ($('#parent').val().indexOf("oauth2.0") != -1) {
                    window.location.reload()
                } else {
                    $.postMessage('RELOAD_PAGE', decodeURIComponent($('#parent').val()));
                }
            }
        });
    }

    $(".byPhone").click(function() {
        var thisBtn = $(this);
        if(thisBtn.attr('disabled') =='disabled') {
            return;
        }
        var username = thisBtn.parent().find("input[name='username']");
        if(username.attr('placeholder') != "undefined" && username.val() == '' ){
            layer.msg(username.attr('placeholder'),{icon:2});
            return false;
        }
        sendAuthRequest(thisBtn, 's', 4, username.val() ,function(data){
            phoneTelAuthToken = data.response_body.token;
        },$.i18n.prop('v328'));
    });

    $(".bysms").click(function() {
        //发送短信
        var thisBtn = $(this);

        if(thisBtn.attr('disabled') =='disabled') {
            return;
        }
        var username = thisBtn.parent().find("input[name='username']");

        if(username.attr('placeholder') != "undefined" && !username.val()){
            layer.msg(username.attr('placeholder'),{icon:2});
            return false;
        }
        sendAuthRequest(thisBtn, 's', 3, username.val() ,function(data){
            phoneSMSAuthToken = data.response_body.token;
        }, $.i18n.prop('resetPwd23'));
    });


    $(".login-icon .login-img").on("click", function () {
        var index = $(this).parents(".button-content").attr("data-index");
        if (index == 0 || index == 9 || index == 11 || index == 12) {
            waitAuthResult(defaulteStatusCode, null,0);
        }
    });

    $(".moreways span").on("click", function () {
        if ($("#otherways0").hasClass("active") || $("#otherways9").hasClass("active") || $("#otherways11").hasClass("active") || $("#otherways12").hasClass("active")) {
            waitAuthResult(defaulteStatusCode, null,0);
        }
    });

    $(".exeActionBtn").click(function(e) {
        var thisBtn = $(this);
        if(thisBtn.attr('disabled') =='disabled') {
            return;
        }
        authActionEvent(thisBtn);
        e.preventDefault();
    });


    function authActionEvent(thisBtn){
        var postdata = getPostData(thisBtn);
        if(!postdata){
            return;
        }
        var type = $(thisBtn).parent('[data-index]').attr('data-index');
        switch(type){
            case 'akey'  :
                akeyAuth(thisBtn,postdata);         //一键登录
                break;
            case 'dynpwd'  :
                dynPwdAuth(thisBtn,postdata);        //动态令牌
                break;
            case 'tempauthcode'  :
                tempAuthCodeAuth(thisBtn,postdata);  //临时授权码
                break;
            case 'voiceauth'  :
                voiceAuthAuth(thisBtn,postdata);     //语音验证码认证
                break;
            case 'smsauth'  :
                smsAuth(thisBtn,postdata);           //短信验证
                break;
            case 'voiceprintauth'  :
                voicePrintAuth(thisBtn,postdata);    //声纹验证
                break;
            case 'faceauth'  :
                faceAuth(thisBtn,postdata);          //人脸验证
                break;
            case 'pwd'  :
                pwdAuth(thisBtn,postdata,'pwd');        //密码
                break;
            case 'pwd2'  :
                pwdAuth(thisBtn,postdata,'pwd2');        //语音二次认证
                break;
            case 'pwd3'  :
                pwdAuth(thisBtn,postdata,'pwd3');        //短信二次认证
                break;

        }
    }

    //双因子认证策略验证密码
    $('.verifition').click(function () {
        var thisBtn = $(this);
        var postdata = getPostData(thisBtn);
        if(!postdata){
            return;
        }
        pwdAuth(thisBtn,postdata,'veri');
    })
    //app一键登录
    function akeyAuth(thisBtn,postdata){
        sendAuthRequest(thisBtn, bigBtnWaitMsg, 1, postdata.username ,function(data){
            waitAuthResult(null, data.response_body.token,1);
            return;
        },$.i18n.prop('v329'));
    }

    //app声纹验证
    function voicePrintAuth(thisBtn,postdata){
        sendAuthRequest(thisBtn, bigBtnWaitMsg, 7, postdata.username ,function(data){
            waitAuthResult(null, data.response_body.token,7);
            return;
        },$.i18n.prop('v329'));


    }
    //app人脸认证
    function faceAuth(thisBtn,postdata){
        sendAuthRequest(thisBtn, bigBtnWaitMsg, 6, postdata.username ,function(data){
            waitAuthResult(null, data.response_body.token,6);
            return;
        },$.i18n.prop('v329'));
    }

    //短信验证
    function smsAuth(thisBtn,postdata){
        if(typeof(phoneSMSAuthToken)=='undefined'){
            messages($.i18n.prop('v330'));
            return;
        }
        checkAuthcode(thisBtn, 3, postdata.authcode, (typeof(phoneSMSAuthToken)!='undefined')?phoneSMSAuthToken:'',
            function(){phoneSMSAuthToken = null;}
            ,function(data){
                if(data.status == 9001){
                    messages($.i18n.prop('v331')); return false;
                }else{
                    showResult(data);
                }
            });
    }

    function updateCode() {
        if ($(".mycode").css('display') === 'block') {
            var timestamp = (new Date()).valueOf();
            var uuid = getuuid();
            $(".captchacode").attr("src", "api/verify/getcode?timestamp=" + timestamp + "&uuid=" + uuid);
            $("input[name=uuid]").val(uuid);
        }
    }

//短信双因子验证
    function smsAuth2(thisBtn,postdata){
        if(typeof(phoneSMSAuthToken)=='undefined'){
            messages($.i18n.prop('v330'));
            return;
        }
        checkAuthcode(thisBtn, 8, postdata.authcode, (typeof(phoneSMSAuthToken)!='undefined')?phoneSMSAuthToken:'',
            function(){phoneSMSAuthToken = null;}
            ,function(data){
                if(data.status == 90091){
                    $(".mycode").css('display','block');
                    updateCode();
                    messages(data.msg);
                    return;
                }

                updateCode();
                if(data.status == 9001){
                    messages($.i18n.prop('v331')); return false;
                }else if(data.status == 9401){
                    // 获取返回的用户字段信息
                    var info = data.response_body || {};
                    var initPwd_layer =  layer.open({
                        title: false,
                        closeBtn: 1,
                        type: 2,
                        scrollbar: false,
                        area:['600px','400px'],
                        shade: [0.8, '#3f3f3f'],
                        cancel:function(){
                            if(info.usersign){
                                if(info.dev_ticket){
                                    //向插件传输设备票据
                                    $.ajax({
                                        type: "POST",
                                        url: "http://127.0.0.1:19401/sso/ticket",
                                        dataType: 'json',
                                        data:JSON.stringify({"dev_ticket" : info.dev_ticket}),
                                        success: function (result) {
                                            if(result.status == 1){
                                                console.log("向插件发送设备票据成功")
                                            }
                                        }
                                    });
                                }
                                go(info.usersign);
                            }
                        },
                        anim: 2,
                        content: ['reset_initOrExp_pwd.html?token=' + (info.token || '')
                        + '&warningCount=' + (info.warningCount || '')
                        + '&userId=' + (info.userId || '')
                        + '&parent=' + ($('#parent').val() || '')]
                    });
                }else if(data.status == 9011){
                    if (data.msg === "Verification Code Error"){
                        messages($.i18n.prop('v313')); return false;
                    }
                    messages($.i18n.prop('v332')); return false;
                }else{
                    showResult(data);
                }
            });
    }

    //语音验证码认证
    function voiceAuthAuth(thisBtn,postdata){
        if(typeof(phoneTelAuthToken)=='undefined'){
            messages($.i18n.prop('v333'));
            return;
        }
        checkAuthcode(thisBtn, 4, postdata.authcode, (typeof(phoneTelAuthToken)!='undefined')?phoneTelAuthToken:'',
            function(){phoneTelAuthToken = null;}
            ,function(data){
                if(data.status == 9001){
                    messages($.i18n.prop('v333')); return;
                }else{
                    showResult(data);
                }
            }
        );

    }

    //语音双因子验证码认证
    function voiceAuthAuth2(thisBtn,postdata){
        if(typeof(phoneTelAuthToken)=='undefined'){
            messages($.i18n.prop('v333'));
            return;
        }
        checkAuthcode(thisBtn, 8, postdata.authcode, (typeof(phoneTelAuthToken)!='undefined')?phoneTelAuthToken:'',
            function(){phoneTelAuthToken = null;}
            ,function(data){
                if(data.status == 90091){
                    $(".mycode").css('display','block');
                    updateCode();
                    messages(data.msg);
                    return;
                }

                updateCode();
                if(data.status == 9001){
                    messages($.i18n.prop('v333')); return;
                }else if (data.status === 9011){
                    if (data.msg === "Verification Code Error"){
                        messages($.i18n.prop('v313')); return false;
                    }
                    messages($.i18n.prop('v332')); return false;
                }else if(data.status == 9401){
                    // 获取返回的用户字段信息
                    var info = data.response_body || {};
                    var initPwd_layer =  layer.open({
                        title: false,
                        closeBtn: 1,
                        type: 2,
                        scrollbar: false,
                        area:['600px','400px'],
                        shade: [0.8, '#3f3f3f'],
                        cancel:function(){
                            if(info.usersign){
                                if(info.dev_ticket){
                                    //向插件传输设备票据
                                    $.ajax({
                                        type: "POST",
                                        url: "http://127.0.0.1:19401/sso/ticket",
                                        dataType: 'json',
                                        data:JSON.stringify({"dev_ticket" : info.dev_ticket}),
                                        success: function (result) {
                                            if(result.status == 1){
                                                console.log("向插件发送设备票据成功")
                                            }
                                        }
                                    });
                                }
                                go(info.usersign);
                            }
                        },
                        anim: 2,
                        content: ['reset_initOrExp_pwd.html?token=' + (info.token || '')
                        + '&warningCount=' + (info.warningCount || '')
                        + '&userId=' + (info.userId || '')
                        + '&parent=' + ($('#parent').val() || '')]
                    });
                }else{
                    showResult(data);
                }
            }
        );
    }

    //临时授权码
    function tempAuthCodeAuth(thisBtn,postdata){
        sendAuthRequest(thisBtn, bigBtnWaitMsg, 5, postdata.username ,function(data, btn, countDownMsg){
            checkAuthcode(thisBtn, 5, postdata.authcode, data.response_body.token, null,
                function(data){
                    if(data.status == 9011){
                        messages($.i18n.prop('v334')); return;
                    }else{
                        showResult(data);
                    }
                }, btn, countDownMsg);
            return;
        });
    }

    //动态密码
    function dynPwdAuth(thisBtn,postdata){
        sendAuthRequest(thisBtn, bigBtnWaitMsg, 2, postdata.username ,function(data, btn, countDownMsg){
            checkAuthcode(thisBtn, 2, postdata.authcode, data.response_body.token, null,
                function(data){
                    if(data.status == 90091){
                        $(".mycode").css('display','block');
                        updateCode();
                        messages(data.msg)
                        return;
                    }

                    updateCode();
                    if(data.status == 9011){
                        messages($.i18n.prop('v335')); return;
                    }else if(data.status == 9401){
                        // 获取返回的用户字段信息
                        var info = data.response_body || {};
                        var initPwd_layer =  layer.open({
                            title: false,
                            closeBtn: 1,
                            type: 2,
                            scrollbar: false,
                            area:['600px','400px'],
                            shade: [0.8, '#3f3f3f'],
                            cancel:function(){
                                if(info.usersign){
                                    if(info.dev_ticket){
                                        //向插件传输设备票据
                                        $.ajax({
                                            type: "POST",
                                            url: "http://127.0.0.1:19401/sso/ticket",
                                            dataType: 'json',
                                            data:JSON.stringify({"dev_ticket" : info.dev_ticket}),
                                            success: function (result) {
                                                if(result.status == 1){
                                                    console.log("向插件发送设备票据成功")
                                                }
                                            }
                                        });
                                    }
                                    go(info.usersign);
                                }
                            },
                            anim: 2,
                            content: ['reset_initOrExp_pwd.html?token=' + (info.token || '')
                            + '&warningCount=' + (info.warningCount || '')
                            + '&userId=' + (info.userId || '')
                            + '&parent=' + ($('#parent').val() || '')]
                        });
                    } else{
                        showResult(data);
                    }
                }, btn, countDownMsg);
        });
    }

    $(".captchacode").on("click",function () {
        var timestamp = (new Date()).valueOf();
        var uuid = getuuid();
        $(this).attr("src","api/verify/getcode?timestamp=" + timestamp+"&uuid="+uuid);
        $("input[name=uuid]").val(uuid);
    });

    function getuuid() {
        return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }

    //密码
    function pwdAuth(thisBtn,postdata,type){
        if('pwd' === type){
            sendAuthRequest(thisBtn, bigBtnWaitMsg, 8, postdata.username ,function(data, btn, countDownMsg){
                checkAuthcode(thisBtn, 8, postdata.authcode, data.response_body.token, null, function(data){
                    if(data.status == 90091){
                        $(".mycode").css('display','block');
                        updateCode();
                        messages(data.msg)
                        return;
                    }

                    updateCode();
                    if(data.status == 9011){
                        messages($.i18n.prop('v332')); return;
                    }else if(data.status == 9401){
                        // 获取返回的用户字段信息
                        var info = data.response_body || {};
                        var initPwd_layer =  layer.open({
                            title: false,
                            closeBtn: 1,
                            type: 2,
                            scrollbar: false,
                            area:['600px','400px'],
                            shade: [0.8, '#3f3f3f'],
                            cancel:function(){
                                if(info.usersign){
                                    if(info.dev_ticket){
                                        //向插件传输设备票据
                                        $.ajax({
                                            type: "POST",
                                            url: "http://127.0.0.1:19401/sso/ticket",
                                            dataType: 'json',
                                            data:JSON.stringify({"dev_ticket" : info.dev_ticket}),
                                            success: function (result) {
                                                if(result.status == 1){
                                                    console.log("向插件发送设备票据成功")
                                                }
                                            }
                                        });
                                    }
                                    go(info.usersign);
                                }
                            },
                            anim: 2,
                            content: ['reset_initOrExp_pwd.html?token=' + (info.token || '')
                            + '&warningCount=' + (info.warningCount || '')
                            + '&userId=' + (info.userId || '')
                            + '&parent=' + ($('#parent').val() || '')]
                        });
                    } else{
                        showResult(data);
                    }
                }, btn, countDownMsg);
            });
        }else if('pwd2' === type){//语音二次认证
            voiceAuthAuth2(thisBtn,postdata)
        }else if('pwd3' === type){//短信二次认证
            smsAuth2(thisBtn,postdata)
        }else{
            sendAuthRequest3(thisBtn, bigBtnWaitMsg, 8, postdata.username ,function(data, btn, countDownMsg){
                checkAuthcode2(thisBtn, 8, postdata.authcode, data.response_body.token, null, function(data){
                    btn.text($.i18n.prop('abmLogin07'))
                    cancelCountDown(btn, null);

                    if(data.status == 90091){
                        $(".mycode").css('display','block');
                        updateCode();
                        messages(data.msg)
                        return;
                    }

                    updateCode();
                    if(data.status == 9011){
                        messages($.i18n.prop('v332')); return;
                    }else if(data.status == 9401){
                        // 获取返回的用户字段信息
                        var info = data.response_body || {};
                        var initPwd_layer =  layer.open({
                            title: false,
                            closeBtn: 1,
                            type: 2,
                            scrollbar: false,
                            area:['600px','400px'],
                            shade: [0.8, '#3f3f3f'],
                            cancel:function(){
                                if(info.usersign){
                                    btn.text($.i18n.prop('v304'));
                                    btn.next().css({"display":'block','margin-left': '74px'})
                                }
                            },
                            anim: 2,
                            content: ['reset_initOrExp_pwd.html?token=' + (info.token || '')
                            + '&warningCount=' + (info.warningCount || '')
                            + '&userId=' + (info.userId || '')
                            + '&parent=' + ($('#parent').val() || '')]
                        });
                    }else{
                        showResult(data);
                    }
                }, btn, countDownMsg);
            });
            //双因子验证
            var appUrl = decodeURIComponent(params.parent);
            var len = appUrl.indexOf('service=');

        }


    }
    function sendAuthRequest3(btn, countDownMsg, atype, username, successFun,msg){
        confirmBigBtnWaitingEffect2(btn, countDownMsg);
        var sendData = {
            sign    : $('#sign').val(),
            username: username,
            type    : atype
        };
        $.ajax({
            type : 'POST',
            url  :  'api/verify/othertype',
            data :  sendData,
            timeout  : ajaxTimeOut,
            dataType : 'json',
            success : function(data) {
                if(data.status == "1000"){
                    $("#publicKey").val(data.response_body.publicKey);
                    $("#encryptor").val(data.response_body.encryptor);
                    successFun(data, btn, null);
                }else if(data.status == defaulteStatusCode){
                    waitAuthResult(null, data.response_body.token,atype);
                }else{
                    btn.text($.i18n.prop('abmLogin07'))
                    cancelCountDown(btn, null);
                    showResult(data);
                }
            }
        });

    }
    function sendAuthRequest(btn, countDownMsg, atype, username, successFun,msg){
        var countDown = confirmBigBtnWaitingEffect(btn, countDownMsg);
        var appUrl = decodeURIComponent(params.parent);
        if(appUrl) {
            var len = appUrl.indexOf('service=');
            if (len > -1) {//说明是cas对接
                appUrl = appUrl.substring(len + 8);
            } else {
                appUrl = "";
            }
        }
        var sendData = {
            sign    : $('#sign').val(),
            username: username,
            type    : atype,
            appUrl  : appUrl
        };
        if($("#crossUuid").val()){
            sendData.uuid = $("#crossUuid").val();
        }
        $.ajax({
            type : 'POST',
            url  :  'api/verify/othertype',
            data :  sendData,
            timeout  : ajaxTimeOut,
            dataType : 'json',
            success : function(data) {
                if(data.status == "1000"){
                    $("#publicKey").val(data.response_body.publicKey);
                    $("#encryptor").val(data.response_body.encryptor);
                    //messages(msg);
                    if(data.response_body.serialNumber){

                        btn.parent('[data-index]').find("span[data-id='serialNumber']").text($.i18n.prop('v336'));
                        btn.next().css("display",'block')
                    }
                    successFun(data, btn, countDown);
                }else if(data.status == defaulteStatusCode){
                    waitAuthResult(null, data.response_body.token,atype);
                }else{
                    cancelCountDown(btn, countDown);
                    showResult(data);
                }
            }
        });
    }
    //双因子发送等待
    function sendAuthRequest2(btn, countDownMsg, atype, username, successFun,msg){
        var sendData = {
            sign    : $('#sign').val(),
            username: username,
            type    : atype
        };
        if($("#crossUuid").val()){
            sendData.uuid = $("#crossUuid").val();
        }
        $.ajax({
            type : 'POST',
            url  :  'api/verify/othertype',
            data :  sendData,
            timeout  : ajaxTimeOut,
            dataType : 'json',
            success : function(data) {
                if(data.status == "1000"){
                    $("#publicKey").val(data.response_body.publicKey);
                    $("#encryptor").val(data.response_body.encryptor);
                    messages(msg);
                    successFun(data, btn, '');
                }else if(data.status == defaulteStatusCode){
                    waitAuthResult(null, data.response_body.token,atype);
                }else{
                    showResult(data);
                }
            }
        });
    }
    function waitAuthResult(status_, token_,atype){
        if (atype === 0) {
            if (isQrAuthWaiting) {
                return;
            } else {
                isQrAuthWaiting = true;
            }
        }
        token_ = token_ || $("#token").val();
        status_ = status_ || defaulteStatusCode;
        var ajaxOpt = {
            method: 'POST',
            type  : 'json',
            url:'api/verify/getauthresult',
            data:{
                status: status_,
                sign: $("#sign").val(),
                token: token_,
                atype:atype
            },
            success: function(data){
                if(data.status == '1000'){
                    if(data.response_body.dev_ticket){
                        //向插件传输设备票据
                        $.ajax({
                            type: "POST",
                            url: "http://127.0.0.1:19401/sso/ticket",
                            dataType: 'json',
                            data:JSON.stringify({"dev_ticket" : data.response_body.dev_ticket}),
                            success: function (result) {
                                if(result.status == 1){
                                    console.log("向插件发送设备票据成功")
                                }
                            }
                        });
                    }
                    go(data.response_body.usersign);
                }else if (signedPageIsExpired){
                    // do nothing, 页面签名已过期，不处理非成功的结果
                    return false;
                }else if (data.status === 9020 && data.msg === 'try_again'){
                    waitAuthResult(status_, token_);
                }else if (data.status === 9900){
                    //service=地址错误时前端跳转取消签名错误提示页面
                }else if (data.status === 9001) {
                    goQrpage($.i18n.prop('v303'));
                }else {
                    showResult(data);
                }
            },
            error: function(XMLHttpRequest, textStatus, errorThrown){
                // do nothing
            }
        };
        $.ajax(ajaxOpt);
    }

    //双因子认证策略验证用户名密码
    function checkAuthcode2(thisBtn, atype, code, token, successFun, errorFun, btn, countDown){
        var username = thisBtn.parent().find("input[name='username']").val();
        var vericode = thisBtn.parent().find("input[name='vericode']").val();
        var verificationcode = thisBtn.parent().find("input[name='verificationcode']").val();
        var uuid = $("input[name=uuid]").val();
        var appUri = $("input[name='parent']").val();
        var appUrl = decodeURIComponent(appUri);
        if(appUrl) {
            var len = appUrl.indexOf('service=');
            if (len > -1) {//说明是cas对接
                appUrl = appUrl.substring(len + 8);
            } else {
                appUrl = "";
            }
        }

        //取出所有div，并遍历
        // $(".loginways div").each(function (i, element) {
        //     if($(element).css("display")=='block'){ //通过$(this).css("css名") 来获取当前遍历元素的display值
        //         //这里就是display=block的
        //         type = element.id;
        //     }
        // });
        var doubleMobileWay = $("input[name='doubleMobileVerify']").val();
        var nolog = "nolog";
        //使用公钥加密
        var publicKey = $("#publicKey").val();
        var encryptorConfig = $("#encryptor").val();
        var encryptor = new CimsEncryptor(encryptorConfig);
        if(code!=null && code!=""){
            code = encryptor.encrypt(publicKey,code + '|' + token);
        }
        if(vericode!=null && vericode!=""){
            vericode = encryptor.encrypt(publicKey,vericode + '|' + token);
        }
        var sendData = {
            sign    : $('#sign').val(),
            token   : token,
            authcode: code || '',
            type    : atype,
            username: username,
            vericode: vericode,
            nolog: nolog,
            doubleMobileWay: doubleMobileWay,
            verificationcode:verificationcode,
            uuid:uuid,
            appUrl: appUrl
        };

        $.ajax({
            type : 'POST',
            url  :  'api/verify/checkAuthcode',
            data :  sendData,
            dataType : 'json',
            timeout  : ajaxTimeOut,
            success : function(data) {
                if(data.status == "1000"){
                    btn.text($.i18n.prop('v304'));
                    btn.next().css({"display":'block','margin-left': '74px'})
                }else{
                    if(btn && countDown){
                        cancelCountDown(btn, countDown);
                    }
                    if(errorFun){
                        errorFun(data);
                    }else{
                        showResult(data);
                    }
                }

            }
        });
    }

    function checkAuthcode(thisBtn, atype, code, token, successFun, errorFun, btn, countDown){
        var username = thisBtn.parent().find("input[name='username']").val();
        var vericode = thisBtn.parent().find("input[name='vericode']").val();
        var verificationcode = thisBtn.parent().find("input[name='verificationcode']").val();
        var uuid = $("input[name=uuid]").val();
        var appUrl = decodeURIComponent(params.parent);
        if(appUrl) {
            var len = appUrl.indexOf('service=');
            if (len > -1) {//说明是cas对接
                appUrl = appUrl.substring(len + 8);
            } else {
                appUrl = "";
            }
        }
        //使用公钥加密
        var publicKey = $("#publicKey").val();
        var encryptorConfig = $("#encryptor").val();
        var encryptor = new CimsEncryptor(encryptorConfig);
        if(code!=null && code!=""){
            code = encryptor.encrypt(publicKey,code + '|' + token);
        }
        if(vericode!=null && vericode!=""){
            vericode = encryptor.encrypt(publicKey,vericode + '|' + token);
        }
        var sendData = {
            sign    : $('#sign').val(),
            token   : token,
            authcode: code || '',
            type    : atype,
            username: username,
            vericode: vericode,
            verificationcode:verificationcode,
            uuid    :uuid,
            appUrl: appUrl
        };
        $.ajax({
            type : 'POST',
            url  :  'api/verify/checkAuthcode',
            data :  sendData,
            dataType : 'json',
            timeout  : ajaxTimeOut,
            success : function(data) {
                if(data.status == "1000"){
                    if(successFun){successFun(data);};
                    if(data.response_body.dev_ticket){
                        //向插件传输设备票据
                        $.ajax({
                            type: "POST",
                            url: "http://127.0.0.1:19401/sso/ticket",
                            dataType: 'json',
                            async: false,
                            data:JSON.stringify({"dev_ticket" : data.response_body.dev_ticket}),
                            success: function (result) {
                                if(result.status == 1){
                                    console.log("向插件发送设备票据成功")
                                }
                            }
                        });
                    }
                    go(data.response_body.usersign);
                }else{
                    if(btn && countDown){
                        cancelCountDown(btn, countDown);
                    }
                    if(errorFun){
                        errorFun(data);
                    }else{
                        showResult(data);
                    }
                }

            }
        });
    }



    //验证成功后跳转到门户首页
    function go(data){
        $.postMessage('GO|' + data  , decodeURIComponent($('#parent').val()));
    }


    function getPostData(thisBtn){
        var username = $(thisBtn).parent('[data-index]').find("input[name='username']");
        var authcode = $(thisBtn).parent('[data-index]').find("input[name='authcode']");
        var vericode = $(thisBtn).parent('[data-index]').find("input[name='vericode']");
        var verificationcode = $(thisBtn).parent('[data-index]').find("input[name='verificationcode']");
        if(username.attr('placeholder') != "undefined" && username.val() == '' ){
            layer.msg(username.attr('placeholder'),{icon:2});
            return false;
        }
        if(authcode && authcode.attr('placeholder') != "undefined" && authcode.val() == '' ){
            layer.msg(authcode.attr('placeholder'),{icon:2});
            return false;
        }
        if(vericode && vericode.attr('placeholder') != "undefined" && vericode.val() == '' ){
            layer.msg(vericode.attr('placeholder'),{icon:2});
            return false;
        }
        if(verificationcode.is(":visible")==true){
            if(verificationcode && verificationcode.attr('placeholder') != "undefined" && verificationcode.val() == '' ){
                layer.msg(verificationcode.attr('placeholder'),{icon:2});
                return false;
            }
        }
        return {username:username.val(), authcode:authcode.val(),vericode:vericode.val,verificationcode:verificationcode.val()};
    }
    //双因子认证验证策略
    function confirmBigBtnWaitingEffect2(objectBtn, tip){
        objectBtn.attr('disabled','disabled').css({'cursor':'not-allowed'});
        var oriText = objectBtn.text();
        objectBtn.text($.i18n.prop('resetPwd10'));
    }

    //大确认按钮等待效果
    function confirmBigBtnWaitingEffect(objectBtn, tip){
        objectBtn.attr('disabled','disabled').css({'cursor':'not-allowed'});
        if($(objectBtn.attr('id') == 'bysms')){
            objectBtn.addClass('inputbysms');
        }else if($(objectBtn.attr('id') == 'byPhone')){
            objectBtn.addClass('inputbyPhone');
        }
        $('.ssonew-out').addClass('ssonew-out-disable');
        var inerCd = operationRetrytime;
        var oriText = objectBtn.text();
        objectBtn.text($.i18n.prop('resetPwd10'));
        var btnType = $(objectBtn).parent('[data-index]').attr('data-index');
        var djs = window.setInterval(function(){
            inerCd--;
            if (btnType != 'pwd') {
                objectBtn.text(inerCd + tip);
            }
            // objectBtn.text('请稍候...');
            if(inerCd == 0){
                if($(objectBtn.attr('id') == 'bysms')){
                    objectBtn.removeClass('inputbysms');
                }else if($(objectBtn.attr('id') == 'byPhone')){
                    objectBtn.removeClass('inputbyPhone');
                }
                window.clearInterval(djs);
                objectBtn.attr('disabled',false).text(oriText).css({'cursor':''});
                $('.ssonew-out').removeClass('ssonew-out-disable');
            }
        }, 1000);
        return {index:djs,text:oriText};
    }

    function cancelCountDown(btn, obj){
        if(obj){
            window.clearInterval(obj.index);
            btn.attr('disabled',false).text(obj.text).css({'cursor':''});
        }
        btn.attr('disabled',false).css({'cursor':''});
        $('.ssonew-out').removeClass('ssonew-out-disable');
    }
    function showResult(data, opt){
        switch(data.status){
            case 9003:
                messages($.i18n.prop('v305'), opt);
                break;
            case 9005:
                messages($.i18n.prop('v306'), opt);
                break;
            case 9010:
                if(data.msg=='faceinfo not found') {
                    messages($.i18n.prop('v307')+'“ '+ appName +' ”'+$.i18n.prop('v308'), opt);
                } else if(data.msg=='voice not found') {
                    messages($.i18n.prop('v307')+'“ '+ appName +' ”'+$.i18n.prop('v309'), opt);
                } else {
                    messages($.i18n.prop('v311'), opt);
                }
                break;
            case 9017:
                goQrpage($.i18n.prop('v312'));
                break;
            case 9011:
                messages($.i18n.prop('v313'), opt);
                break;
            case 9001: //alert('返回状态:'+ data.status + ',消息:'+ data.msg);
                messages($.i18n.prop('v314'), opt);
                break;
            case 9009:
                goQrpage($.i18n.prop('v315'));
                break;
            case 9019:
                messages($.i18n.prop('v316'), opt);
                break;
            case 9029:
                messages($.i18n.prop('v317'), opt);
                break;
            case 9020:
                messages(data.msg, opt);
                break;
            case 9999:
                goQrpage($.i18n.prop('v318'));
                //$.postMessage('RELOAD_PAGE' , $('#parent').val());
                break;
            case 9024:
                messages($.i18n.prop('v319'), {time: 5000, shadeClose: true});
                break;
            case 9002:
                if (data.msg == 'token not found') {
                    // sign失效情况
                    goQrpage($.i18n.prop('v318'));
                } else {
                    messages(data.msg?data.msg:$.i18n.prop('v320'), {time: 5000, shadeClose: true});
                }
                break;
            case 9051:
                messages($.i18n.prop('v321'), {time: 5000, shadeClose: true});
                break;
            case 9052:
                messages($.i18n.prop('v322'), {time: 5000, shadeClose: true});
                break;
            case 9061:
                messages($.i18n.prop('v323'), {time: 5000, shadeClose: true});
                break;
            case 9062:
                messages($.i18n.prop('v324'), {time: 5000, shadeClose: true});
                break;
            case 9030:
                messages($.i18n.prop('v325'), {time: 5000, shadeClose: true});
                break;
            case 9032:
                messages($.i18n.prop('v326'), {time: 5000, shadeClose: true});
                break;
            default :
                messages(data.msg, opt);
                break;
        }
    }


    function reloadPage(){
        if(debug){
            var count_down = signExpireTime;
            var cdit = setInterval(function(){
                count_down--;
                if(count_down == 0){
                    window.clearInterval(cdit);}
            }, 1000);
        }
        setTimeout(function(){
            signedPageIsExpired = true;
            goQrpage($.i18n.prop('v318'));
        },signExpireTime * 1000);
    }

    function messages(msg, opt){
        if(msg){
            var opt = opt || {time: 2500, shadeClose: true};
            layer.msg(msg, opt);
        }
    }

    function initParameter(){
        voiceCallTelNumber = $("#voiceCallTelNumber").val();
        if(voiceCallTelNumber != 'null' && voiceCallTelNumber.length >0 ){
            if(voiceCallTelNumber.length == 11){
                voiceCallTelNumber = voiceCallTelNumber.substr(0, 3) + '-' + voiceCallTelNumber.substr(3);
            }else if(voiceCallTelNumber.length == 12){
                voiceCallTelNumber = voiceCallTelNumber.substr(0, 4) + '-' + voiceCallTelNumber.substr(4);
            }
        }else{
            voiceCallTelNumber = '';
        }
    }

    //倒计时
    var countdown1=60;
    //短信验证码发送
    $('#pwd1').click(function () {
        if(smsCode()){
            settime();
        }
    })

    function settime() {
        var val = $('#pwd1');
        if (countdown1 == 0) {
            val.prop("disabled",false);
            val.attr('value',$.i18n.prop('v327'))
            countdown1 = 60;
            return;
        } else {
            val.prop("disabled", true);
            val.attr('value',"(" + countdown1 + ")"+$.i18n.prop('abmLogin20'));
            countdown1--;
        }
        setTimeout(function() {
            settime()
        },1000)

    }

    function smsCode() {
        var thisBtn = $('#pwd1');
        var username = thisBtn.parents('#loginways_1_3').find("input[name='username']");
        if(username.attr('placeholder') != "undefined" && !username.val()){
            layer.msg(username.attr('placeholder'),{icon:2});
            return false;
        }
        sendAuthRequest2(thisBtn, 's', 3, username.val() ,function(data){
            phoneSMSAuthToken = data.response_body.token;
        }, $.i18n.prop('resetPwd23'));
        return true;
    }

    //语音验证码发送
    $('#phone1').click(function () {
        if(verifitionCode()){
            settime_phone();
        }
    })

    function settime_phone() {
        var val = $('#phone1');
        if (countdown1 == 0) {
            val.prop("disabled",false);
            val.attr('value',$.i18n.prop('appLoginway18'))
            countdown1 = 60;
            return;
        } else {
            val.prop("disabled", true);
            val.attr('value',"(" + countdown1 + ")"+$.i18n.prop('abmLogin20'));
            countdown1--;
        }
        setTimeout(function() {
            settime_phone()
        },1000)

    }

    function verifitionCode() {
        var thisBtn = $('#phone1');
        var username = thisBtn.parents('#loginways_1_4').find("input[name='username']");
        if(username.attr('placeholder') != "undefined" && username.val() == '' ){
            layer.msg(username.attr('placeholder'),{icon:2});
            return false;
        }
        sendAuthRequest2(thisBtn, 's', 4, username.val() ,function(data){
            phoneTelAuthToken = data.response_body.token;
        },$.i18n.prop('v328'));
        return true;
    }


    //自定义首页的图片
    function initImg(){
        $.ajax({
            type : 'get',
            cache : false,
            url  :  'api/authpage/index',
            success : function(data) {
                var resultdata=data.resp_data.authpage;
                if(data.status == "1000"){
                    var banner= resultdata.bannerimg;
                    var logo = resultdata.logoimg;
                    var backcolor = resultdata.backcolor;
                    var appname = resultdata.appname;
                    var sysname = resultdata.systemname;
                    if(banner){
                        $("#banner").attr("src","api/file/get?id="+banner);
                    } else {
                        $('#banner').attr('src',"image/lunbo1.png");
                    }
                    if(logo){
                        $('#main-logo').attr('src',"api/file/get?id="+logo);
                    } else {
                        $('#main-logo').attr('src',"image/logo1.png");
                    }
                    if(sysname!=undefined){
                        $('#sys-name').html(sysname);
                    }

                    if(backcolor){
                        $('.M_bg').css('backgroundColor',backcolor);
                    } else {
                        backcolor = $('.M_bg').css('backgroundColor');
                    }
                    $("<style type='text/css'>" +
                        ".layui-layer-title,.layui-layer-btn .layui-layer-btn0 {" +
                        "background-color: " + backcolor + ";" +
                        "border-color: " + backcolor +
                        "}" +
                        "</style>").appendTo($('head'));

                    if(appname!=undefined){
                        $('.app-name').html(appname);
                        appName = appname;
                        $('.app-name-placeholder').attr('placeholder', $.i18n.prop('reauht03')+'“'+appname+'”'+$.i18n.prop('reauht04'));
                        if (IEVersion() <=9){
                            $('.app-name-placeholder').val('');
                        }
                    }else{
                        $('.app-name').html($.i18n.prop('appLoginway27'));
                    }
                    //用户手册下载
                    // var authManual=data.resp_data.auth_manual;
                    // if(authManual!=null){
                    //     var res=$.parseJSON(authManual).authManualUrl;
                    //     if(res=='1'){
                    //         $(".download_user_manual").css("display", "block");
                    //     }else{
                    //         $(".download_user_manual").css("display", "none");
                    //     }
                    // }
                }else if(data.status == "1002"){
                    $('.app-name').html($.i18n.prop('appLoginway27'));
                }
            }
        });
    }

    /**
     * 用户认证手册下载
     */
    // $(".download_user_manual a").click(function () {
    //         $.ajax({
    //             type : 'get',
    //             url  :  'api/authpage/appspinfo',
    //             success : function(data) {
    //                 if(data.status == "1000"){
    //                     //用户手册下载
    //                     var authManual=data.resp_data.auth_manual;
    //                     var url=data.resp_data.authManualUrl;
    //                     if(authManual!=null){
    //                         var res=$.parseJSON(authManual).authManualUrl;
    //                         if(res!='1'){
    //                             layer.msg("暂无权限下载,请联系管理员！",{icon:2});
    //                             return;
    //                         }
    //                         if(url==null){
    //                             layer.msg("暂无用户认证手册,请联系管理员！",{icon:2});
    //                             return;
    //                         }
    //                         if(res=='1' && url!=null){
    //                             window.location.href="api/file/get?id="+url
    //                         }else{
    //                             layer.msg("暂时无法下载,请联系管理员！",{icon:2});
    //                         }
    //                     }else{
    //                         layer.msg("暂时无法下载,请联系管理员！",{icon:2});
    //                     }
    //                 }else{
    //                     layer.msg("下载请求失败！",{icon:2});
    //                 }
    //
    //             }
    //         });
    //     });

    /**
     * 判断IE版本
     * @return {*}
     * @constructor
     */
    function IEVersion() {
        var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
        var isIE = userAgent.indexOf("compatible") > -1 && userAgent.indexOf("MSIE") > -1; //判断是否IE<11浏览器
        var isEdge = userAgent.indexOf("Edge") > -1 && !isIE; //判断是否IE的Edge浏览器
        var isIE11 = userAgent.indexOf('Trident') > -1 && userAgent.indexOf("rv:11.0") > -1;
        if(isIE) {
            var reIE = new RegExp("MSIE (\\d+\\.\\d+);");
            reIE.test(userAgent);
            var fIEVersion = parseFloat(RegExp["$1"]);
            if(fIEVersion == 7) {
                return 7;
            } else if(fIEVersion == 8) {
                return 8;
            } else if(fIEVersion == 9) {
                return 9;
            } else if(fIEVersion == 10) {
                return 10;
            } else {
                return 6;//IE版本<=7
            }
        } else if(isEdge) {
            return 'edge';//edge
        } else if(isIE11) {
            return 11; //IE11
        }else{
            return -1;//不是ie浏览器
        }
    }
});


