$(document).ready(function () {
    //9009 9999
    initParameter(); //初始化全局参数
    var voiceCallTelNumber = ""; //有bug会导致拔打电话时不显示电话号码，注释掉
    var defaulteStatusCode = '9009'; //默认状态码
    waitAuthResult(defaulteStatusCode, null); // 默认等待二维码结果
    var operationRetrytime = 60; // 按钮操作倒计时重试时间
    var bigBtnWaitMsg = '秒后可重试'; // 全局消息
    var signExpireTime = 280; // 秒  要小于签名过期时间300秒
    var ajaxTimeOut = 0; // 请求超时时间
    var debug = true;
    reloadPage(); // 启动倒计时刷新
    //同步所有input value
    $("input[name='username']").change(function () {
        $("input[name='username']").not(':visible').val($(this).val());
    });

    function goQrpage(msg, countdown) {
        messages(msg, {
            time: countdown,
            shadeClose: true,
            shade: [0.8, '#393D49']
        });
        setTimeout(function () {
            $.postMessage('RELOAD_PAGE', $('#parent').val());
        }, countdown);
    }

    //点击下载图标，进入到下载页面
    $(document).on('click', ".download", function () {
        $(".eightSpeciesLoginUp p,.download,.QRCode,.eightLoginLogo,.appOauthIn,.dynamicPassword1,.ProvisionalAuthority,.telephone,.ProvisionalAuthority,.sendShortMessages,.voiceprintauthentication,.faceverification").hide();
        $(".universalIdentity,.universalIdentity p").show();
        $(".login").css({
            "height": "420px"
        });
    });
    //点击下载App页面二维码，回到扫二维码登录页面
    $(document).on('click', ".appdownload", function () {
        $(".universalIdentity").hide();
        $(".twoCode").addClass("selected");
        $(".twoCode").siblings().removeClass("selected");
        $(".eightSpeciesLoginUp p,.download,.QRCode,.eightLoginLogo").show();

    });
    /*右侧的按钮点击效果*/
    $(document).on('click', '.ssonewEightSpeciesMethods .ssonew-out', function () {
        if ($(this).hasClass('ssonew-out-disable')) return false;
        $(".universalIdentity").hide();
        $(".twoCode").addClass("selected");
        $(".twoCode").siblings().removeClass("selected");
        $(".eightSpeciesLoginUp p,.download,.QRCode,.eightLoginLogo").show();
        $(this).find(".icon").addClass("actived").next("p").addClass("actived");
        $(this).siblings().find(".icon").removeClass("actived").next("p").removeClass("actived");
        $(this).find('.biankuang_1').css({
            "border-left": "3px solid #8fc491"
        }).stop(true).animate({
            height: '120px'
        }, 600).parent().siblings().find('.biankuang_1,.biankuang_2,.biankuang_3,.biankuang_4').stop(true).delay(100).animate({
            height: '0px',
            width: '0px'
        }, 100);
        $(this).find('.biankuang_2,.biankuang_4').stop(true).delay(300).animate({
            width: '120px'
        }, 600);
        $(this).find('.biankuang_3').stop(true).delay(300).animate({
            height: '119px'
        }, 600);
        var index = $(this).index();
        $(".d").hide();
        $(".d:eq(" + index + ")").show();
        var username = $('div[data-ac="' + $(this).attr('data-id') + '"]').parent('div').find(".email-username:visible");
        if ('undefined' != username.val() && $('#email').val() != 'null' && $('#email').val() != '') {
            username.val($('.email-username').val());
            username.attr('readonly', 'readonly');
        }
    });

    $("#byPhone").click(function () {
        var thisBtn = $(this);
        var username = $(this).parent('div').parent('div').find(".email-username:visible");
        if (username.attr('placeholder') != "undefined" && username.val() == '') {
            layer.msg(username.attr('placeholder'), {
                icon: 2
            });
            return false;
        }
        messages('您将收到一个' + voiceCallTelNumber + '的语音电话,请填写收到的验证码');
        sendAuthRequest(thisBtn, 's', 4, username.val(), function (data) {
            phoneTelAuthToken = data.response_body.token;
        });
    });
    $("#bysms").click(function () {
        var thisBtn = $(this);
        var username = $(this).parent('div').parent('div').find(".email-username:visible");
        if (username.attr('placeholder') != "undefined" && username.val() == '') {
            layer.msg(username.attr('placeholder'), {
                icon: 2
            });
            return false;
        }
        messages('短信验证码已发送,请在手机查看');
        sendAuthRequest(thisBtn, 's', 3, username.val(), function (data) {
            phoneSMSAuthToken = data.response_body.token;
        });
    });
    $(".exeActionBtn").click(function () {
        var thisBtn = $(this);
        var ac = $(this).parent('div').attr('data-ac');
        eval('var exeActionBtnEvent_' + ac + '=thisBtn;');
        eval('exeActionBtnEventBtn = exeActionBtnEvent_' + ac);
        if (exeActionBtnEventBtn.attr('disabled') == 'disabled') return;
        authActionEvent();
    });

    function authActionEvent() {
        var postdata = getPostData(exeActionBtnEventBtn);
        if (!postdata) return;
        var type = $(exeActionBtnEventBtn).parent('div').attr('data-ac');
        switch (type) {
            case 'akey':
                akeyAuth(postdata); //一键登录
                break;
            case 'dynpwd':
                dynPwdAuth(postdata); //动态令牌
                break;
            case 'tempauthcode':
                tempAuthCodeAuth(postdata); //临时授权码
                break;
            case 'voiceauth':
                voiceAuthAuth(postdata); //语音验证码认证
                break;
            case 'smsauth':
                smsAuth(postdata); //短信验证
                break;
            case 'voiceprintauth':
                voicePrintAuth(postdata); //声纹验证
                break;
            case 'faceauth':
                faceAuth(postdata); //人脸验证
                break;

        }
    }

    //app一键登录
    function akeyAuth(postdata) {
        messages('正在发起推送,请在手机上确认登录');
        sendAuthRequest(exeActionBtnEventBtn, bigBtnWaitMsg, 1, postdata.username, function (data) {
            waitAuthResult(null, data.response_body.token);
            return;
        });
    }

    //app声纹验证
    function voicePrintAuth(postdata) {
        messages('正在发起推送,请在手机上确认登录');
        sendAuthRequest(exeActionBtnEventBtn, bigBtnWaitMsg, 7, postdata.username, function (data) {
            waitAuthResult(null, data.response_body.token);
            return;
        });
    }

    //app人脸认证
    function faceAuth(postdata) {
        messages('正在发起推送,请在手机上确认登录');
        sendAuthRequest(exeActionBtnEventBtn, bigBtnWaitMsg, 6, postdata.username, function (data) {
            waitAuthResult(null, data.response_body.token);
            return;
        });
    }

    //短信验证
    function smsAuth(postdata) {
        if (typeof(phoneSMSAuthToken) == 'undefined') {
            messages('令牌失效,请获取短信验证码');
            return;
        }
        checkAuthcode(3, postdata.authcode, (typeof(phoneSMSAuthToken) != 'undefined') ? phoneSMSAuthToken : '',
            function () {
                phoneSMSAuthToken = null;
            },
            function (data) {
                if (data.status == 9001) {
                    messages('请重新获取取短信验证码');
                    return false;
                } else {
                    showResult(data);
                }
            });
    }

    //语音验证码认证
    function voiceAuthAuth(postdata) {
        if (typeof(phoneTelAuthToken) == 'undefined') {
            messages('令牌失效,请拔打电话获取验证码');
            return;
        }
        checkAuthcode(4, postdata.authcode, (typeof(phoneTelAuthToken) != 'undefined') ? phoneTelAuthToken : '',
            function () {
                phoneTelAuthToken = null;
            },
            function (data) {
                if (data.status == 9001) {
                    messages('请重新拔打电话获取验证码');
                    return;
                } else {
                    showResult(data);
                }
            }
        );

    }

    //临时授权码
    function tempAuthCodeAuth(postdata) {
        sendAuthRequest(exeActionBtnEventBtn, bigBtnWaitMsg, 5, postdata.username, function (data, btn, countDownMsg) {
            checkAuthcode(5, postdata.authcode, data.response_body.token, null, null, btn, countDownMsg);
            return;
        });
    }

    //动态密码
    function dynPwdAuth(postdata) {
        sendAuthRequest(exeActionBtnEventBtn, bigBtnWaitMsg, 2, postdata.username, function (data, btn, countDownMsg) {
            checkAuthcode(2, postdata.authcode, data.response_body.token, null, null, btn, countDownMsg);
        });
    }

    function sendAuthRequest(btn, countDownMsg, atype, username, successFun) {
        var countDown = confirmBigBtnWaitingEffect(btn, countDownMsg);
        console.log(username);
        /**    if(username.indexOf('@')==-1){
				username += $("select[name='mailsuffix']:visible option:selected").val();
			}*/
        var sendData = {
            sign: $('#sign').val(),
            username: username,
            type: atype
        };
        $.ajax({
            type: 'POST',
            url: $('#ctx').val() + '/api/verify/othertype',
            data: sendData,
            timeout: ajaxTimeOut,
            dataType: 'json',
            success: function (data) {
                if (data.status == "1000") {
                    if (data.response_body.serialNumber) {
                        btn.parent('div').find("span[data-id='serialNumber']").text('登录序列号：' + data.response_body.serialNumber);
                    }
                    successFun(data, btn, countDown);
                } else if (data.status == defaulteStatusCode) {
                    waitAuthResult(null, data.response_body.token);
                } else {
                    cancelCountDown(btn, countDown);
                    showResult(data);
                }
            }
        });
    }

    function waitAuthResult(status_, token_) {
        token_ = token_ || $("#token").val();
        status_ = status_ || defaulteStatusCode;
        var ajaxOpt = {
            method: 'POST',
            type: 'json',
            timeout: ajaxTimeOut,
            url: $('#ctx').val() + '/api/verify/getauthresult',
            data: {
                status: status_,
                sign: $("#sign").val(),
                token: token_
            },
            success: function (data) {
                if (data.status == '1000') {
                    // parent.CIMS.go(result['response_body']['usersign']);
                    go(data.response_body.usersign);
                } else {
                    showResult(data);
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) { //alert(JSON.stringify(msg));
                console.log('请求QR Code 出错,code:' + JSON.stringify(XMLHttpRequest));
            }
        };
        $.ajax(ajaxOpt);
    }

    function checkAuthcode(atype, code, token, successFun, errorFun, btn, countDown) {
        if (!token) {
            console.log('令牌：' + token);
        }
        console.log('checkAuthcode token:' + token);
        console.log('checkAuthcode sign:' + $('#sign').val());
        var sendData = {
            sign: $('#sign').val(),
            token: token,
            authcode: code || '',
            type: atype
        };

        $.ajax({
            type: 'POST',
            url: $('#ctx').val() + '/api/verify/checkAuthcode',
            data: sendData,
            dataType: 'json',
            timeout: ajaxTimeOut,
            success: function (data) {
                if (data.status == "1000") {
                    if (successFun) {
                        successFun(data);
                    }
                    ;
                    //parent.CIMS.go(data['response_body']['usersign']);
                    go(data.response_body.usersign);
                } else {
                    if (btn && countDown) {
                        cancelCountDown(btn, countDown);
                    }
                    if (errorFun) {
                        errorFun(data);
                    } else {
                        showResult(data);
                    }
                }

            }
        });
    }

    function go(data) {
        $.postMessage('GO|' + data, $('#parent').val());
    }

    function getPostData() {
        var username = $(exeActionBtnEventBtn).parent('div').find(".email-username:visible");
        var authcode = $(exeActionBtnEventBtn).parent('div').find("#authcode:visible");
        if (username.attr('placeholder') != "undefined" && username.val() == '') {
            layer.msg(username.attr('placeholder'), {
                icon: 2
            });
            return false;
        }
        if (authcode.attr('placeholder') != "undefined" && authcode.val() == '') {
            layer.msg(authcode.attr('placeholder'), {
                icon: 2
            });
            return false;
        }
        return {
            username: username.val(),
            authcode: authcode.val()
        };
    }

    //大确认按钮等待效果
    function confirmBigBtnWaitingEffect(objectBtn, tip) {
        objectBtn.attr('disabled', 'disabled').css({
            'cursor': 'not-allowed'
        });
        if ($(objectBtn.attr('id') == 'bysms')) {
            objectBtn.addClass('inputbysms');
        } else if ($(objectBtn.attr('id') == 'byPhone')) {
            objectBtn.addClass('inputbyPhone');
        }
        $('.ssonew-out').addClass('ssonew-out-disable');
        var inerCd = operationRetrytime;
        var oriText = objectBtn.text();
        objectBtn.text('请稍候...');
        var djs = window.setInterval(function () {
            inerCd--;
            objectBtn.text(inerCd + tip);
            if (inerCd == 0) {
                if ($(objectBtn.attr('id') == 'bysms')) {
                    objectBtn.removeClass('inputbysms');
                } else if ($(objectBtn.attr('id') == 'byPhone')) {
                    objectBtn.removeClass('inputbyPhone');
                }
                window.clearInterval(djs);
                objectBtn.attr('disabled', false).text(oriText).css({
                    'cursor': ''
                });
                $('.ssonew-out').removeClass('ssonew-out-disable');
            }
        }, 1000);
        return {
            index: djs,
            text: oriText
        };
    }

    function cancelCountDown(btn, obj) {
        window.clearInterval(obj.index);
        btn.attr('disabled', false).text(obj.text).css({
            'cursor': ''
        });
        $('.ssonew-out').removeClass('ssonew-out-disable');
    }

    function reloadPage() {

        if (debug) {
            var count_down = signExpireTime;
            var cdit = setInterval(function () {
                count_down--;
                if (count_down <= 3) {
                    console.log('还剩余' + count_down + '秒过期');
                } else {
                    console.log(count_down);
                }
                if (count_down == 0) {
                    window.clearInterval(cdit);
                    console.log('签名已过期,跳到认证页面．');
                }
            }, 1000);
        }
        setTimeout(function () {
            goQrpage('会话马上过期,系统将自动跳到初始页面．', 2000);
        }, signExpireTime * 1000);
    }

    function showResult(data, opt) {
        console.log('返回状态:' + data.status + ',消息:' + data.msg);
        switch (data.status) {
            case 9003:
                messages('用户手机号不完整,请联系管理员.', opt);
                break;
            case 9005:
                messages('您操作频繁请稍后再试.', opt);
                break;
            case 9010:
                if (data.msg == 'faceinfo not found') {
                    messages('请先初始化该信息.', opt);
                } else if (data.msg == 'voice not found') {
                    messages('请先初始化该信息.', opt);
                } else {
                    messages('请在手机上绑定该用户.', opt);
                }
                break;
            case 9017:
                goQrpage('手机端已拒绝您的请求,系统将自动跳到初始页面．', 4000);
                break;
            case 9011:
                messages('验证码错误', opt);
                break;
            case 9001: //alert('返回状态:'+ data.status + ',消息:'+ data.msg);
                messages('无效签名', opt);
                break;
            case 9009:
                goQrpage('签名已过期,系统将自动跳到初始页面．', 4000);
                break;
            case 9019:
                messages('用户被禁用', opt);
                break;
            case 9020:
                messages(data.msg, opt);
                break;
            case 9999:
                goQrpage('会话马上过期,系统将自动跳到初始页面．', 4000);
                //$.postMessage('RELOAD_PAGE' , $('#parent').val());
                break;
            case 9024:
                messages('由于您没有该应用的访问权限，芯盾统一身份认证无法通过您的认证请求，请与管理员联系.', {
                    time: 5000,
                    shadeClose: true
                });
                break;
            default:
                messages(data.msg, opt);
                break;
        }
    }

    function messages(msg, opt) {
        if (msg) {
            var opt = opt || {
                time: 2500,
                shadeClose: true
            };
            layer.msg(msg, opt);
        }
    }

    function initParameter() {
        voiceCallTelNumber = $("#voiceCallTelNumber").val();
        if (voiceCallTelNumber != 'null' && voiceCallTelNumber.length > 0) {
            if (voiceCallTelNumber.length == 11) {
                voiceCallTelNumber = voiceCallTelNumber.substr(0, 3) + '-' + voiceCallTelNumber.substr(3);
            } else if (voiceCallTelNumber.length == 12) {
                voiceCallTelNumber = voiceCallTelNumber.substr(0, 4) + '-' + voiceCallTelNumber.substr(4);
            }
        } else {
            voiceCallTelNumber = '';
        }
    }
});