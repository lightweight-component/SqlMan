import './css/normalize.css';
import './css/font_login/iconfont.css';
import './app/main.css';
import './js/layui/css/layui.css';
import './lib/layer/layer.css';

import 'jquery';
// import './lib/jquery-3.7.1.min.js';
import './js/i18n/jquery.i18n.properties.js';
import './js/i18n/language.js';
import './app/scrollbar.js';

import './collect-privity-v5.1.12.js';
// import './lib/require.js';

(function (win, export_obj) {
    win['LogAnalyticsObject'] = export_obj;
    if (!win[export_obj]) {
        var _collect = function () {
            _collect.q.push(arguments);
        }
        _collect.q = _collect.q || [];
        win[export_obj] = _collect;
    }
    win[export_obj].l = +new Date();
})(window, 'collectEvent');

var app_id_send_domain = location.origin == "https://authcenter.cmccsim.com" ? 10000041 : 10000040;
var app_id_send_domain_origin = location.origin;

window.collectEvent('init', {
    app_id: app_id_send_domain, // 参考2.1节获取，注意类型是number而非字符串
    channel_domain: 'https://dc.cmicapm.com:7080', // 设置数据上送地址
    log: true, // true:开启日志，false:关闭日志
    autotrack: false, // 全埋点开关，true开启，false关闭
});

switch (app_id_send_domain_origin) {
    case "https://authcenter.cmccsim.com":
        // 此处可添加设置uuid、设置公共属性等代码
        window.collectEvent('start'); // 通知SDK设置完毕，可以真正开始发送事件了
        break;
    case "http://10.19.240.1:8100":
        // 此处可添加设置uuid、设置公共属性等代码
        window.collectEvent('start'); // 通知SDK设置完毕，可以真正开始发送事件了
        break;
    default:
        // 此处可添加设置uuid、设置公共属性等代码
        // window.collectEvent('start'); // 通知SDK设置完毕，可以真正开始发送事件了
        break;
}

document.querySelector('.app-version').textContent = '版本：ids-3.2.0.0';

window.setAgreementRead = function () {
    var agreementIcon = $("#agreement i")
    if (agreementIcon.hasClass("agreement-active")) {
        agreementIcon.removeClass("agreement-active")
        agreementIcon.removeClass("icon-fuxuanxuanzhong1")
        agreementIcon.addClass("icon-fuxuanmoren1")
    } else {
        agreementIcon.removeClass("icon-fuxuanmoren1")
        agreementIcon.addClass("agreement-active")
        agreementIcon.addClass("icon-fuxuanxuanzhong1")
    }
}

console.log('hi--------');