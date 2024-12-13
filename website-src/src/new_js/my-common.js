function parseQueryString(url) {
    var params = {};
    var arr = url.split("?");
    if (arr.length === 1)
        return params;

    var arr1 = arr[1].split("&");

    for (var i = 0; i < arr1.length; i++) {
        var arr2 = arr1[i].split('=');
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

var urlParams = parseQueryString(window.location.href);

function getPlatform() {
    var userAgent = navigator.userAgent;

    if (userAgent.toLocaleLowerCase().indexOf("DingTalk".toLocaleLowerCase()) > -1)
        return "dingtalk";

    if (userAgent.toLocaleLowerCase().indexOf("wxwork".toLocaleLowerCase()) > -1)
        return 'qywx';

    if (userAgent.toLocaleLowerCase().indexOf("Lark".toLocaleLowerCase()) > -1)
        return 'feishu';

    if (userAgent.toLocaleLowerCase().indexOf("WorkbenchType_1".toLocaleLowerCase()) > -1
        || userAgent.toLocaleLowerCase().indexOf("yidongbangong".toLocaleLowerCase()) > -1)
        return 'cmOa';

    return null;
}

function dingtalkBackListening() {
    if (getPlatform() !== 'dingtalk')
        return;

    var u = navigator.userAgent;
    var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);

    if (isiOS)
        dd.biz.navigtion.setLeft({
            control: true,
            onSuccess: function () {
                dd.biz.navigation.close()
            }
        });
    else
        document.addEventListener('backbutton', function () {
            dd.biz.navigation.close()
        });
}

function getErrorMsg(code) {
    switch (code) {
        case '8999':
            setErrorDetail();
            return '遇到未知的系统错误,<br/>请点按左上角按钮关闭此页面'
        case '7001':
            return '参数错误'
        case '7005':
            return '公司选择错误'
        case '9019':
            return '用户已被禁用'
        case '9074':
            return '用户已被锁定'
        case '9002':
            return '无法找到匹配用户'
        case '9017':
            return '前后用户不一致'
        case '9027':
            return '认证源不可用，请联系管理员'
        case '9024':
            return '当前环境不允许操作'
        case '9075':
            return '用户已被禁用'
        case '9204':
            return '认证已过期或者已完成'
        case '9997':
            return '请求失败'
        case '9998':
            return '认证源配置错误，请联系管理员'
        case '92248':
            return '租户已禁用或过期'
        default:
            if (urlParams['action'] === 'mutualTrust') {
                return '遇到未知的系统错误,<br/>请跳转至登录页面重新登录'
            }
            return '遇到未知的系统错误,<br/>请点按左上角按钮关闭此页面'
    }
}

function setErrorDetail() {
    var detailMsg = decodeURIComponent(urlParams['msg']);

    if (detailMsg) {
        $('.err_reason').show()
        $('#detail').html(detailMsg)
        $('.err_reason').unbind('click').bind('click', function () {
            var isHide = $('#detail').is(':hidden')
            if (isHide) {
                $('#detail').show()
                $(this).find('i').html('&#xe638;')
            } else {
                $('#detail').hide()
                $(this).find('i').html('&#xe639;')
            }
        });
    }
}

export { urlParams, getPlatform, dingtalkBackListening, getErrorMsg };