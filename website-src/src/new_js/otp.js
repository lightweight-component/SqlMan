import '../css/normalize.css';
import '../css/mobile.css';
import '../css/font_login/iconfont.css';

import 'jquery';
// import 'layer';
import '../js/totp.js';
import { urlParams } from './my-common';
import dd from '../lib/dingtalk.open.js';

var a, seed = '';
// var seed = '23TplPdS46Juzcyx'
// var index = layer.load();

$(function () {
    // layer.msg('加载中...');
    if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
        // alert('iPhone/平板')
        jQuery('head').append('<link>');
        a = jQuery('head').children(':last');
        a.attr({
            rel: 'stylesheet',
            type: 'text/css',
            href: './css/mobile.css'
        });
    } else if (/(Android)/i.test(navigator.userAgent)) {
        // alert('安卓端')
        jQuery('head').append('<link>');
        a = jQuery('head').children(':last');
        a.attr({
            rel: 'stylesheet',
            type: 'text/css',
            href: './css/mobile.css?t=' + Math.random()
        });
    } else {
        // alert('PC端')
        jQuery('head').append('<link>');
        a = jQuery('head').children(':last');
        a.attr({
            rel: 'stylesheet',
            type: 'text/css',
            href: './css/pc.css'
        });

        if ($(window).width() < 1280) {
            $("#logo").attr('src', './image/h5/mLogo.png')
        } else {
            $("#logo").attr('src', './image/h5/pLogo.png')
        }
    }

    seed = sessionStorage.getItem("seed");

    if (!seed) {
        var corpId = urlParams['corpId']
        if (corpId) { // 企业标识
            dd.ready(function () { // 钉钉
                dd.runtime.permission.requestAuthCode({
                    corpId: corpId, // 企业id
                    onSuccess: function (info) {
                        otpRun(info.code)
                        playUp(seed)
                        setInterval(timeLoop, 1000);
                    },

                    onFail: function (err) {
                        replacePage("./fail.html?type=8999&msg=" + encodeURIComponent(JSON.stringify(err)) + "&t=" + Math.random())
                    }
                });
            });
        } else {
            otpRun(urlParams['code'] || urlParams['authCode']);
            playUp(seed);
            setInterval(timeLoop, 1000);
        }
    } else {
        playUp(seed);
        setInterval(timeLoop, 1000);
    }
})


function otpRun(code) {
    $.ajax({
        type: "GET",
        url: "api/idp/otp/getSeed",
        data: {
            code,
            appKey: urlParams['state'],
        },
        async: false,
        dataType: "json",
        contentType: 'application/json',
        success: function (resp) {
            if (resp.status === 1000) {
                seed = resp.responseBody
                sessionStorage.setItem("seed", seed)
                playUp(seed);
            } else {
                if (resp.status === 8999) {
                    replacePage("./fail.html?type=" + resp.status + "&msg=" + encodeURIComponent(resp.msg) + "&t=" + Math.random())
                } else {
                    replacePage('./fail.html?type=' + resp.status + '&t=' + Math.random())
                }
            }
        },
        error: function () {
            replacePage("./fail.html?type=9997" + "&t=" + Math.random());
        }
    });

    playUp(seed);
}

var timeLoop = function () {
    var epoch = Math.round(new Date().getTime() / 1000.0);
    var countDown = 30 - (epoch % 30);
    playUp(seed, countDown)
}

function playUp(seed, countDown = 30) {
    var totp = new jsOTP.totp();
    var keyCode = totp.getOtp(seed);
    // $('.count_down').find("span").text(countDown)
    $('.g_progress').css({ 'width': (100 * countDown) / 30 + '%' });

    for (var i = 0; i < keyCode.length; i++) {
        $($('.code_group').children("li").get(i)).find("span").text(keyCode[i])
    }
}

function replacePage(url) {
    if (history.replaceState) {
        history.replaceState(null, document.title, url);
        history.go(0);
    } else {
        location.replace(url);
    }
}