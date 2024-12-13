import 'jquery';
import '../js/i18n/jquery.i18n.properties.js';
import '../js/i18n/language.js';

import { urlParams } from './my-common';

var sendData = {
    code: urlParams['code'],
    state: urlParams['state']
};
console.log(sendData);

$.ajax({
    type: 'GET',
    url: 'api/idp/wechat/code',
    async: false,
    data: sendData,
    dataType: 'json',
    success: function (res) {
        if (res.status === 1000) {
            $("#content").html("登录成功");

            setTimeout(function () {
                window.close()
            }, 1000);
        } else {
            $("#content").html(res.msg)
        }
    },
});