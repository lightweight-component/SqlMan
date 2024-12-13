import '../css/normalize.css';
import '../css/mobile.css';
import '../css/font_login/iconfont.css';
import '../lib/layer/layer.css';

import 'jquery';
import 'layer';
import { urlParams } from './my-common';
import '../js/i18n/jquery.i18n.properties.js';
import '../js/i18n/language.js';
import '../js/jquery-sortable.js';
import '../js/jquery.validate.js';
import '../js/jquery-validation-1.17.0/localization/messages_zh.min.js';
import '../js/jquery.validate.extend.js';
import '../js/base64Util.js';
import '../js/jsbn.js';
import '../js/sm-crypto.js';
import '../js/Cryptojs-3.1.2.js';
import '../js/cims_encryptor.js';
import '../js/jsencrypt.js';

{/* <script type="text/javascript" src="js/jquery-postmessage.min.js"></script>
<script type="text/javascript" src="./js/jquery.validate.extend.js"></script>
<script type="text/javascript" src="./js/base64Util.js"></script>
<script type="text/javascript" src="./js/jsbn.js"></script>
<script type="text/javascript" src="./js/sm-crypto.js"></script>
<script type="text/javascript" src="./js/Cryptojs-3.1.2.js"></script>
<script type="text/javascript" src="./js/cims_encryptor.js"></script>
<script type="text/javascript" src="./js/jsencrypt.js"></script> --> */}

var forgetPwdCaptcha = {
    imageCaptchaKey: ""
}

var a;

if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
    // alert('iPhone/平板')
    jQuery('head').append('<link>');
    a = jQuery('head').children(':last');
    a.attr({
        rel: 'stylesheet',
        type: 'text/css',
        href: './css/reset_pwd_H5.css2'
    });
    $("#backBtn").css('display', 'none')
} else if (/(Android)/i.test(navigator.userAgent)) {
    jQuery('head').append('<link>');
    a = jQuery('head').children(':last');
    a.attr({
        rel: 'stylesheet',
        type: 'text/css',
        href: './css/reset_pwd_H5.css2?t=' + Math.random()
    });
    $("#backBtn").css('display', 'none')
} else {
    jQuery('head').append('<link>');
    a = jQuery('head').children(':last');

    a.attr({
        rel: 'stylesheet',
        type: 'text/css',
        href: require('../css/sso_form.css2')
    });
    jQuery('head').append('<link>');
    a = jQuery('head').children(':last');
    a.attr({
        rel: 'stylesheet',
        type: 'text/css',
        href: require('../css/wechatlogin.css2')
    });
}

init();
// 这个方法需要配合
var isInApp = /(^|;\s)app\//.test(navigator.userAgent.toLowerCase());
if (isInApp) 
    $("#backBtn").css('display', 'none') //app去掉箭头


var params = {};
getImageCaptcha();

function init() {
    $.ajax({
        type: "post",
        url: "authorization/authPage",
        success: function (result) {
            if (result.code === 1000) {
                // layer.msg($.i18n.prop('resetPwd09'), { time: 2500, shadeClose: true });
                // 设置浏览器标签页title
                $('title').html(result.data.systemName)
                // 设置浏览器标签页ico
                var favicon = document.querySelector('link[rel="icon"]')
                if (result.data.loginIcoImg) {
                    favicon.href = result.data.loginIcoImg
                } else {
                    favicon.href = './favicon.ico'
                }
            } else {
                // layer.msg(result.msg, { time: 2500, shadeClose: true });
            }
        }
    });
}


var url = window.location.href;


$(document).ready(function () {
    /*$.getJSON("api/verify/fetchModifyPwdRule", function (result) {
        if (result.ok) {
            $("#pwdRule").html(result.data);
        }
    });*/
    var myreg = /^1[0-9]{10}$/;
    $("#phone-error").hide();
    $("#resetPwdform").validate({
        rules: {
            // username: {
            //     required: true,
            //     noSpace: false
            // },
            newPwd: {
                required: true,
                noSpace: false
            },
            imageCaptchaValue: {
                required: true,
            },
            confirmNewPwd: {
                required: true,
                equalTo: "#newPwd",
                noSpace: false
            },
            /*empNum:{
                required:true,
                phoneNumber:true
            },*/
            checkCode: {
                required: true
            },
            token: {
                required: true
            },
            publicKey: {
                required: true
            }
        },
        onkeyup: false,
        messages: {
            // username: {
            //     required: $.i18n.prop('register05'),
            //     noSpace: $.i18n.prop('resetPwd17')
            // },
            imageCaptchaValue: {
                required: "请输入图形验证码",
            },
            newPwd: {
                required: $.i18n.prop('authApp10'),
                noSpace: $.i18n.prop('resetPwd18')
            },
            confirmNewPwd: {
                required: $.i18n.prop('authApp11'),
                equalTo: $.i18n.prop('resetPwd08'),
                noSpace: $.i18n.prop('resetPwd18')
            },
            checkCode: {
                required: $.i18n.prop('login08')
            },
            token: {
                required: $.i18n.prop('resetPwd20')
            },
            publicKey: {
                required: $.i18n.prop('resetPwd20')
            }
        },
        submitHandler: function (form) {
            var publicKey = $("#input-publicKey").val();
            var alg = $("#input-alg").val();
            if (!alg) {
                layer.msg($.i18n.prop('resetPwd21'), { time: 2500, shadeClose: true });
                return
            }
            var encryptor = new CimsEncryptor(alg);
            var sendData = {
                phoneNum: $("#empNum").val(),
                authCode: encryptor.encrypt(publicKey, $("#checkCode").val()),
                newPwd: encryptor.encrypt(publicKey, $("#newPwd").val()),
                token: $("#input-token").val(),
                captchaId: forgetPwdCaptcha.imageCaptchaKey,
                code: $("#imageCaptchaValue").val()
            };
            $.ajax({
                type: "post",
                url: "api/idp/pwd/resetPwd",
                data: sendData,
                dataType: 'json',
                success: function (result) {
                    if (result.status === 1000) {
                        layer.msg($.i18n.prop('resetPwd09'), { time: 2500, shadeClose: true });
                        if (/(Android)/i.test(navigator.userAgent)) {
                            androidFunction.close()
                        } else if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
                            window.webkit.messageHandlers.iosFunction.postMessage("")
                        } else {
                            setTimeout(reload, 2500);
                        }
                    } else {
                        getImageCaptcha()
                        layer.msg(result.msg, { time: 2500, shadeClose: true });
                    }
                }
            });
        }
    });
    $("#submitBtn").click(function (e) {
        /*xif (countdown === 60) {
             layer.msg('请先获取验证码', {time: 2500, shadeClose: true});
             return;
         }*/
        if ($("#imageCaptchaValue").val() === "" || $("#imageCaptchaValue").val() == null) {
            layer.msg("请输入图形验证码", { time: 2500, shadeClose: true });
            return;
        }
        e.preventDefault();
        if (!myreg.test($("#empNum").val())) {
            $("#phone-error").text($.i18n.prop('resetPwd22'));
            $("#phone-error").show();
            return;
        }
        if ($("#resetPwdform").valid()) {
            $("#resetPwdform").submit();
        }
    });

    $("#empNum").blur(function () {
        if (!myreg.test($("#empNum").val()) && $("#empNum").val().length > 0) {
            $("#phone-error").text($.i18n.prop('resetPwd22'));
            $("#phone-error").show();
        } else {
            $("#phone-error").hide();
        }
    });


    $("#btnSendVeryCode").click(function (e) {
        // if ($("#username").val() === "" || $("#username").val() == null) {
        //     layer.msg($.i18n.prop('resetPwd12'), { time: 2500, shadeClose: true });
        //     return;
        // }

        if ($("#imageCaptchaValue").val() === "" || $("#imageCaptchaValue").val() == null) {
            layer.msg("请输入图形验证码", { time: 2500, shadeClose: true });
            return;
        }
        e.preventDefault();
        if (myreg.test($("#empNum").val())) {
            $("#phone-error").hide();
            var sendData = {
                phoneNum: $("#empNum").val(),
                // username: $("#username").val(),
                captchaId: forgetPwdCaptcha.imageCaptchaKey,
                code: $("#imageCaptchaValue").val(),
            };
            var btnCD = btnCountDown(document.getElementById("btnSendVeryCode"));
            $.ajax({
                type: "post",
                url: "api/idp/pwd/sendSms",
                data: sendData,
                dataType: 'json',
                success: function (result) {
                    if (result.status === 1000) {
                        btnCD.start();
                        layer.msg($.i18n.prop('resetPwd23'), { time: 2500, shadeClose: true });
                        $("#input-publicKey").val(result.responseBody.publicKey);
                        $("#input-alg").val(result.responseBody.alg);
                        $("#input-token").val(result.responseBody.token);
                    } else {
                        layer.msg(result.msg, { time: 2500, shadeClose: true });
                        btnCD.reset();
                    }
                    getImageCaptcha()
                }

            });
        } else {
            $("#phone-error").text($.i18n.prop('resetPwd22'));
            $("#phone-error").show();
        }

    });

});

function getImageCaptcha() {
    // 拿到验证码数据，展示验证码图片
    let url
    if (forgetPwdCaptcha.imageCaptchaKey !== "") {
        url = "api/idp/pwd/getImageCaptcha?captchaId=" + forgetPwdCaptcha.imageCaptchaKey + "&id=" + Math.random()
    } else {
        url = "api/idp/pwd/getImageCaptcha?id=" + Math.random()
    }

    $.ajax({
        type: "GET",
        url: url, // 请求被缓存，添加随机数及时刷新
        success: function (resp) {
            if (resp.status === 1000) {
                forgetPwdCaptcha.imageCaptchaKey = resp.responseBody.captchaId;
                $("#imageCaptchaRender").attr("src", resp.responseBody.img);
                // 成功后把图形验证码输入框置空
                $("#imageCaptchaValue").val('');

            } else {
                layer.msg(resp.msg, { time: 2500, shadeClose: true });
            }
        }
    });
}

/**
 * 按钮倒计时方法
 * @param {HTMLElement} btn
 * @param {number} [second] 倒计时时间，单位：秒；默认60s
 * @param {function} [btnMsgFormatter] 按钮信息格式化方法，返回按钮的文字信息；默认显示：“xxs后重试”
 * @param {number} btnMsgFormatter.count 剩余倒计时的秒数；
 */
function btnCountDown(btn, second, btnMsgFormatter) {
    var originVal = btn.value;


    btn.setAttribute("disabled", "disabled");
    if (!second) {
        second = 60;
    }

    var endTime;

    if (!btnMsgFormatter) {
        btnMsgFormatter = defaultBtnMsgFormatter;
    }

    var stop = false;

    function countDown() {
        if (stop) {
            reset();
            return;
        }
        var dis = endTime - new Date().getTime();
        if (dis > 0) {
            var count = parseInt(dis / 1000);
            btn.value = btnMsgFormatter(count);
            setTimeout(countDown, 150);
        } else {
            reset();
        }
    }

    function defaultBtnMsgFormatter(count) {
        return count + "s " + $.i18n.prop('abmLogin26') + $.i18n.prop('abmLogin20')
    }

    function reset() {
        btn.value = originVal;
        btn.removeAttribute("disabled");
        // btn.setAttribute("disabled", false);
    }

    function start() {
        endTime = new Date().getTime() + second * 1000;
        stop = false
        countDown();
        return this;
    }

    function ready() {
        btn.value = $.i18n.prop('resetPwd19');
        return this;
    }

    ready();

    return {
        reset: reset,
        start: start,
        ready: ready
    };
}

function reload() {
    var params = window.location.href.split("?");
    params = params.length > 1 ? params[1] : "";
    $(location).attr('href', './index.html?' + params);
}