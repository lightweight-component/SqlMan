import '../css/normalize.css';
import '../css/mobile.css';
import '../css/font_login/iconfont.css';

import 'jquery';
import { urlParams, dingtalkBackListening } from './my-common';
import dd from '../lib/dingtalk.open.js';

var userToken = ''
$(function () {
    var corpId = urlParams['corpId']
    if (corpId) {
        dd.ready(function () { // 钉钉
            dd.runtime.permission.requestAuthCode({
                corpId: corpId, // 企业id
                onSuccess: function (info) {
                    getUser(info.code)
                },

                onFail: function (err) {
                    replacePage("./fail.html?type=8999&msg=" + encodeURIComponent(JSON.stringify(err)) + "&t=" + Math.random())
                }
            });

            dingtalkBackListening();
        });
    } else {
        getUser(urlParams['code'] || urlParams['authCode'])
    }
})

function getUser(code) {
    $.ajax({
        type: "GET",
        url: "api/idp/qrcode/user",
        data: {
            code,
            state: urlParams['state'],
        },
        async: false,
        dataType: "json",
        contentType: 'application/json',
        success: function (resp) {
            if (resp.status === 1000) {
                userToken = resp.responseBody['userToken']
                $('#title').text('您正在登录【' + resp.responseBody['appName'] + '】')
                $('#loginAccount').text(resp.responseBody['account'])
                $('#ipAddress').text(resp.responseBody['ip'])
                $('#loginSeat').text(resp.responseBody['location'])
                $('#loginTime').text(formatTime(resp.responseBody['loginTime']))
                // storage(resp.responseBody['clientKey'])
            } else {
                // clearStorage('clientKey')
                if (resp.status === 8999) {
                    replacePage("./fail.html?type=" + resp.status + "&msg=" + encodeURIComponent(resp.msg) + "&t=" + Math.random())
                } else {
                    replacePage("./fail.html?type=" + resp.status + "&t=" + Math.random())
                }
            }
        },
        error: function () {
            replacePage("./fail.html?type=9997&t=" + Math.random())
        }
    });
}

function storage(params) {
    localStorage.setItem('clientKey', params)
}

function clearStorage(key) {
    localStorage.removeItem(key)
}

function formatTime(timestamp) {
    let date = new Date(parseInt(timestamp));
    let Year = date.getFullYear();
    let Mouth = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1);
    let Day = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate());
    let Hour = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours());
    let Minute = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes());
    let Seconds = (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
    return Year + '-' + Mouth + '-' + Day + '   ' + Hour + ':' + Minute + ':' + Seconds;
}

function confirm() {
    $.ajax({
        type: "GET",
        url: "api/idp/qrcode/userConfirm",
        data: {
            state: urlParams['state'],
            userToken
        },
        async: false,
        dataType: "json",
        contentType: 'application/json',
        success: function (resp) {
            if (resp.status === 1000) {
                replacePage('./finish.html?t=' + Math.random())
            } else {
                replacePage('./fail.html?type=' + resp.status + "&t=" + Math.random())
            }
        },
        error: function () {
            replacePage("./fail.html?type=9997&t=" + Math.random())
        }
    })
}

function cancel() {
    $.ajax({
        type: "GET",
        url: "api/idp/qrcode/userCancel",
        data: {
            state: urlParams['state'],
            userToken
        },
        async: false,
        dataType: "json",
        contentType: 'application/json',
        success: function () {
            replacePage('./fail.html?type=cancel&t=' + Math.random())
        },
        error: function () {
            replacePage("./fail.html?type=9997&t=" + Math.random())
        }
    })
}

function replacePage(url) {
    if (history.replaceState) {
        history.replaceState(null, document.title, url);
        history.go(0);
    } else {
        location.replace(url);
    }
}

window.confirm = confirm;
window.cancel = cancel;