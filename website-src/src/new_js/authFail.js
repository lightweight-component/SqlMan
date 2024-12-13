import '../css/normalize.css';
import '../css/mobile.css';
import '../css/font_login/iconfont.css';

import 'jquery';
import { urlParams, getErrorMsg } from './my-common';
import dd from '../lib/dingtalk.open.js';

$(function () {
    $('.err_reason').hide()
    $('#detail').hide()
    $('.redirect').hide()

    let action = urlParams['action'];
    let type = urlParams['type'];

    if (type === 'cancel') {
        $('.fail_page h2').text('访问不成功！')
        $('.fail_page #err').html('您取消了此次访问，<br/>请点按左上角按钮关闭此页面');

        return;
    }

    // $('.fail_page h2').text('扫码认证提示')
    $('.fail_page #err').html(getErrorMsg(type))

    if (action === 'mutualTrust') {
        $(".redirect").attr("href", decodeURIComponent(urlParams['authUrl']));
        $('.redirect').show();

        return;
    }

    dd.ready(function () { // 钉钉
        dingtalkBackListening();
    });
})