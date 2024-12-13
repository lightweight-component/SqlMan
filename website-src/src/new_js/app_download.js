import '../css/normalize.css';
import '../css/app_download.css';

import 'jquery';
import '../js/i18n/jquery.i18n.properties.js';
import '../js/i18n/language.js';

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
            // 设置标题名称
            $('#title').html(result.data.systemName);

            // 设置LOGO图片
            if (result.data && result.data.loginLogoImg) {
                $('#imgLogo').attr("src", result.data.loginLogoImg);
            } else {
                $('#imgLogo').attr("src", 'image/logo.png');
            }

        } else {
            // layer.msg(result.msg, { time: 2500, shadeClose: true });
        }
    }
});