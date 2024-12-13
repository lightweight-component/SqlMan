import '../css/normalize.css';
import '../css/mobile.css';
import '../css/font_login/iconfont.css';

import 'jquery';
import { urlParams } from './my-common';

$(function () {
    var isClose = sessionStorage.getItem("close");

    if (isClose) {
        sessionStorage.removeItem("close");
        closeWindow();
        return;
    }

    initData();
})

function initData() {
    $.ajax({
        type: "GET",
        url: "api/idp/platform/selectOrg",
        data: {
            authcode: urlParams['token']
        },
        async: false,
        dataType: "json",
        contentType: 'application/json',
        success: function (resp) {
            if (resp.status === 1000) {
                // var clientKey = localStorage.getItem('clientKey')
                // if (clientKey) {
                //     let org = resp.responseBody.find(item => item.url && item.url.includes(clientKey))
                //     if (org) {
                //         window.location.href = org.url
                //         return
                //     }
                // }
                drawing(resp.responseBody)
            } else {
                replacePage('./fail.html?type=' + resp.status + '&t=' + Math.random())
            }
        },
    });
}

function drawing(params) {
    $('#title').html("选择组织");
    $('.org_page').append("<p>请先选择您所在的组织，再进行访问</p>");

    params.forEach(ele => {
        ele.name = ele.name.replace(/(["'<>]|&(?:(amp|lt|gt|#39|nbsp|quot|#\d+);)?)/g, (a, b, c) =>
            c ? a : {
                '<': '&lt;',
                '&': '&amp;',
                '"': '&quot;',
                '>': '&gt;',
                "'": '&#39;'
            }[a]);
        var html = '<div class="org_item"><span class="org_name">' + ele.name + '</span><span class="select_btn" onclick="selectItem(\'' + ele.url + '\')">选择</span></div>'
        $('.org_page').append(html);
    });
}

function selectItem(url) {
    sessionStorage.setItem("close", "1")
    replacePage(url)
}

function replacePage(url) {
    location.replace(url);
}

function closeWindow() {
    if (document.addEventListener)
        document.addEventListener("WeixinJSBridgeReady", function () {
            window.WeixinJSBridge && window.WeixinJSBridge.call("closeWindow")
            parent.WeixinJSBridge.call('closeWindow')
        }, false);
    else if (document.attachEvent)
        document.attachEvent("onWeixinJSBridgeReady", function () {
            //这个可以关闭ios系统的手机
            window.WeixinJSBridge && window.WeixinJSBridge.call("closeWindow");
        });

    // 非ios处理
    if (/(iPhone|iPad|iPod|iOS)/i.test(navigator.userAgent)) {
    } else
        windowWeixinJSBridge && window.WeixinJSBridge.call("closeWindow");

    // Pc端处理
    window.close && window.close()
}