/**
 * 动态加载JS
 * @param {string} url 脚本地址
 * @param {function} callback  回调函数
 */
function dynamicLoadJs(url, callback) {
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;
    if (typeof(callback) == 'function') {
        script.onload = script.onreadystatechange = function () {
            if (!this.readyState || this.readyState === "loaded" || this.readyState === "complete") {
                callback();
                script.onload = script.onreadystatechange = null;
            }
        };
    }
    head.appendChild(script);
}

/**
 * 动态加载CSS
 * @param {string} url 样式地址
 */
function dynamicLoadCss(url) {
    var head = document.getElementsByTagName('head')[0];
    var link = document.createElement('link');
    link.type = 'text/css';
    link.rel = 'stylesheet';
    link.href = url;
    head.appendChild(link);
}

var iever = 0;
var _jsver = 0;
/*@cc_on
    _jsver = @_jscript_version;
@*/
if (_jsver == 0) {
    // IE11 或者不是 IE

    if (!!window.MSInputMethodContext && !!document.documentMode) { // ie11
        iever = 11;
    }
} else {
    var docmode = document.documentMode;
    if (!!docmode && docmode > 5) {
        iever = docmode;
    } else if (_jsver == 5.7 && window.XMLHttpRequest) {
        iever = 7;
    } else if (_jsver == 5.6 || (_jsver == 5.7 && !window.XMLHttpRequest)) {
        iever = 6;
    } else {
        iever = 5;
    }
}

if (iever > 0) {
    if (iever <= 9) {
        // 小于ie9
        dynamicLoadCss('css/ssoie8.css');
        dynamicLoadJs('js/jquery.placeholder.min.js', function () {
            $('input, textarea').placeholder();
        });
        dynamicLoadJs('js/html5shiv.js');
        dynamicLoadJs('js/respond.min.js');
    }
    if (iever <= 8) {
        dynamicLoadJs('js/PIE.js');
        dynamicLoadJs('js/ssoie8.js');
    }
}
