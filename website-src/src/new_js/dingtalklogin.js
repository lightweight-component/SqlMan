// import '../css/common.min.css';
import '../css/wechatlogin.css';
import '../lib/layer/layer.css';

import 'jquery';
import '../js/i18n/jquery.i18n.properties.js';
import '../js/i18n/language.js';
import 'layer';
import '../js/base64Util.js';
import '../js/Cryptojs-3.1.2.js';
import '../js/cims_encryptor.js';
import '../js/jsencrypt.js';

function parseQueryString(url) {
    var params = {};
    var arr = url.split("?");

    if (arr.length <= 2)
        return params;

    var arr1 = arr[1].split("&");
    for (var i = 0; i < arr1.length; i++) {
        arr2 = arr1[i].split('=');
        if (!arr2[1]) {
            params[arr2[0]] = 'true';
        } else {
            if (params[arr2[0]]) {
                var arr3 = [params[arr2[0]]];
                arr3.push(arr2[1]);
                params[arr2[0]] = arr3;
            } else {
                params[arr2[0]] = decodeURI(arr2[1]);
            }
        }
    }

    return params;
}


function closePageForm() {
    if (navigator.userAgent.indexOf("MSIE") > 0) {
        if (navigator.userAgent.indexOf("MSIE 6.0") > 0) {
            window.opener = null;
            window.close();
        } else {
            window.open('', '_top');
            window.top.close();
        }
    } else {
        window.opener = null;
        window.open('', '_self', '');
        window.close();
    }

}

function checkPassword() {
    var thisBtn = $(this);
    var usernameObj = thisBtn.parent().find("input[name='username']");
    var authcodeObj = thisBtn.parent().find("input[name='authCode']");

    if (usernameObj.attr('placeholder') != "undefined" && usernameObj.val() == '') {
        layer.msg(usernameObj.attr('placeholder'), { icon: 2 });

        if (usernameObj.attr('data-i18n-msg')) {
            layer.msg(usernameObj.attr('data-i18n-msg'), { icon: 2 });
        } else {
            layer.msg(usernameObj.attr('placeholder'), { icon: 2 });
        }

        return false;
    }

    if (authcodeObj.attr('placeholder') != "undefined" && authcodeObj.val() == '') {
        layer.msg(authcodeObj.attr('placeholder'), { icon: 2 });
        return false;
    }

    var username = usernameObj.val();
    var code = authcodeObj.val();
    var unionid = $("input[name='unionid']").val();
    var state = $("input[name='state']").val();
    var authtype = thisBtn.attr('data-index');
    var encryptorConfig = sessionStorage.getItem("dtlogin_encryptor");
    var publicKey = sessionStorage.getItem("dtlogin_publicKey");
    // 加解密
    var encryptor = new CimsEncryptor(encryptorConfig);
    code = encryptor.encrypt(publicKey, code);


    var sendData = {
        authtype: authtype,
        unionid: unionid,
        code: code,
        username: username,
        state: state
    };

    $.ajax({
        type: 'POST',
        url: 'api/dingtalk/authusercode',
        async: true,
        data: sendData,
        dataType: 'json',
        success: function (res) {
            if (res.status == 1000) {
                messages($.i18n.prop('wechat05'));
                closePageForm();
            } else
                layer.msg(res.msg, { icon: 2 });
        },
        error: function (msg) {
            layer.msg($.i18n.prop('wechat06'), { icon: 2 });
        }

    });
}


function messages(msg, opt) {
    if (msg) {
        var opt = opt || { time: 2500, shadeClose: true };
        layer.msg(msg, opt);
    }
}

function errorMessages(msg) {
    if (msg) {
        var opt = opt || { time: 2500, shadeClose: true };
        layer.msg(msg, { icon: 2 });
    }
}

//大确认按钮等待效果
function confirmBigBtnWaitingEffect(objectBtn, tip) {
    objectBtn.attr('disabled', 'disabled').css({ 'cursor': 'not-allowed' });
    var inerCd = 60;
    var oriText = objectBtn.text();
    objectBtn.text($.i18n.prop('resetPwd10'));

    var djs = window.setInterval(function () {
        inerCd--;
        objectBtn.text(inerCd + tip);

        if (inerCd == 0) {
            window.clearInterval(djs);
            objectBtn.attr('disabled', false).text(oriText).css({ 'cursor': '' });
        }
    }, 1000);
    return { index: djs, text: oriText };
}

function cancelCountDown(btn, obj) {
    window.clearInterval(obj.index);
    btn.attr('disabled', false).text(obj.text).css({ 'cursor': '' });
}

function sendAuthRequest(btn, countDownMsg, atype, username, successFun) {
    var countDown = confirmBigBtnWaitingEffect(btn, countDownMsg);

    var unionid = $("input[name='unionid']").val();
    var sendData = {
        username: username,
        unionid: unionid,
        authtype: atype
    };
    $.ajax({
        type: 'POST',
        url: 'api/dingtalk/dtVerify',
        data: sendData,
        dataType: 'json',
        success: function (data) {
            if (data.status == 1000) {
                successFun(data, btn, countDown);
            } else {
                cancelCountDown(btn, countDown);
                layer.msg(data.msg, { icon: 2 });
            }
        }
    });
}


$(document).ready(function () {
    var url = window.location.href;
    var params = parseQueryString(url);
    $(".submitButton").click(checkPassword);

    $(".sendsms").click(function () {
        var thisBtn = $(this);

        if (thisBtn.attr('disabled') == 'disabled') {
            return;
        }
        var username = thisBtn.parent().find("input[name='username']");

        if (username.attr('placeholder') != "undefined" && !username.val()) {
            layer.msg(username.attr('placeholder'), { icon: 2 });
            return false;
        }

        sendAuthRequest(thisBtn, 's', "phone", username.val(), function (data) {
            messages($.i18n.prop('resetPwd23'));
        });
    });

    $(".sendemail").click(function () {
        var thisBtn = $(this);

        if (thisBtn.attr('disabled') == 'disabled') {
            return;
        }
        var username = thisBtn.parent().find("input[name='username']");

        if (username.attr('placeholder') != "undefined" && !username.val()) {
            layer.msg(username.attr('placeholder'), { icon: 2, time: 1000 });
            return false;
        }
        sendAuthRequest(thisBtn, 's', "email", username.val(), function (data) {
            messages($.i18n.prop('dingtalk02'));
        });

    });

    $("input[name='state']").val(params.state);

    var sendData = {
        code: params.authCode || params.code,
        token: params.state
    };
    $.ajax({
        type: 'GET',
        url: 'api/idp/dingtalk/code',
        async: false,
        data: sendData,
        dataType: 'json',
        success: function (res) {
            if (res.status === 1000) {
                messages($.i18n.prop('wechat09'));
                closePageForm();
            } else {
                errorMessages(res.msg);
                setTimeout(function () {
                    closePageForm();
                }, 2500);
            }

        },
        error: function (msg) {
            layer.msg(msg, { icon: 2 });
        }

    });
    //自定义APP名称
    $.ajax({
        type: 'get',
        cache: false,
        url: 'api/authpage/index',
        success: function (data) {
            var resultdata = data.resp_data.authpage;
            if (data.status == "1000") {
                var appname = resultdata.appname;
                $('.app-name').html(appname);
            }
        }
    });
});