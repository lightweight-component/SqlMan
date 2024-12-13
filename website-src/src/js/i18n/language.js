//cookie操作
var getCookie = function (name, value, options) {
    if (typeof value != 'undefined') { // name and value given, set cookie
        options = options || {};
        if (value === null) {
            value = '';
            options.expires = -1;
        }
        var expires = '';
        if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
            var date;
            if (typeof options.expires == 'number') {
                date = new Date();
                date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
            } else {
                date = options.expires;
            }
            expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
        }
        var path = options.path ? '; path=' + options.path : '';
        var domain = options.domain ? '; domain=' + options.domain : '';
        var s = [cookie, expires, path, domain, secure].join('');
        var secure = options.secure ? '; secure' : '';
        var c = [name, '=', encodeURIComponent(value)].join('');
        var samsite = ';SameSite=Strict'
        var cookie = [c, expires, path, domain, secure, samsite].join('')
        document.cookie = cookie;


    } else { // only name given, get cookie
        var cookieValue = null;
        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                // Does this cookie string begin with the name we want?
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }
        return cookieValue;
    }
};

//获取浏览器语言类型
var getNavLanguage = function () {
    if (navigator.appName == "Netscape") {
        var navLanguage = navigator.language;
        return navLanguage.substr(0, 2);
    }
    return false;
}

// 设置语言类型： 默认为中文
var i18nLanguage = "zh-CN";

//设置一下网站支持的语言种类
var webLanguage = ['zh-CN', 'en'];

//执行页面i18n方法
var execI18n = function () {
    //获取一下资源文件名
    var optionEle = $("#i18n_pagename");
    if (optionEle.length < 1) {
        return false;
    };

    var sourceName = optionEle.attr('content');
    sourceName = sourceName.split('-');

    //首先获取用户浏览器设备之前选择过的语言类型
    if (getCookie("userLanguage")) {
        i18nLanguage = getCookie("userLanguage");
    }

    // 需要引入 i18n 文件
    if ($.i18n == undefined) {
        return false;
    };

    //这里需要进行i18n的翻译
    jQuery.i18n.properties({
        name: sourceName, //资源文件名称
        path: 'i18n/' + i18nLanguage + '/', //资源文件路径
        mode: 'map', //用Map的方式使用资源文件中的值
        language: i18nLanguage,
        callback: function () {
            //加载成功后设置显示内容
            var insertEle = $(".i18n");
            insertEle.each(function () {
                // 根据i18n元素的 name 获取内容写入
                $(this).html($.i18n.prop($(this).attr('name')));
            });

            //跟上边的区别就是可以给html标签的任何属性可以赋值，
            var insertInputEle = $(".i18n-input");
            insertInputEle.each(function () {
                var selectAttr = $(this).attr('selectattr');
                if (!selectAttr) {
                    selectAttr = "value";
                };
                $(this).attr(selectAttr, $.i18n.prop($(this).attr('selectname')));
            });

            //一般情况下，我们标签里面的内容如果要做国际化，需要使用 $('#id').text($.i18n.prop('proName')); 来给标签赋值，
            // 现在问题来了，我们开发一个界面，有很多地方都需要去做国际化，我们总不能这样每一个页面每一个标签通过这种方式去赋值吧，
            // 这样工作量不是一点大，于是乎博主想，有没有一种比较好的通用的解决方案去给这些需要做国际化的标签统一赋值呢。
            // html的data属性似乎是一个不错的选择！它具有可读性强、可维护性强、兼容jquery的data()方法等优点。
            // 比如我们修改国际化组件的方法如下
            //<input class="typeahead" type="text" id="menu_search" data-i18n-placeholder = "searchPlaceholder"/>
            //<span data-i18n-text="setting"></span>

            var placeholderEle = $('[data-i18n-placeholder]');
            placeholderEle.each(function () {
                var attr = $(this).attr('data-i18n-dyna');
                if (attr) {
                    //有占位符
                    var originalStr = $.i18n.prop($(this).data('i18n-placeholder'))
                    originalStr = originalStr.replace("{0}", attr);
                    $(this).attr('placeholder', originalStr);

                    var attrDyn = $(this).attr('data-i18n-msg');
                    if (attrDyn) {
                        attrDyn = attrDyn.replace("{0}", attr);
                        $(this).attr('data-i18n-msg', attrDyn);
                    }

                } else {
                    $(this).attr('placeholder', $.i18n.prop($(this).data('i18n-placeholder')));
                }
            });

            var textEle = $('[data-i18n-text]')
            textEle.each(function () {
                //如果text里面还有html需要过滤掉
                var html = $(this).html();
                var reg = /<(.*)>/;
                if (reg.test(html)) {
                    var htmlValue = reg.exec(html)[0];
                    $(this).html(htmlValue + $.i18n.prop($(this).data('i18n-text')));
                }
                else {
                    $(this).text($.i18n.prop($(this).data('i18n-text')));
                }
            });

            var valueEle = $('[data-i18n-value]');
            valueEle.each(function () {
                $(this).val($.i18n.prop($(this).data('i18n-value')));
            });

        }
    });
}

/*页面执行加载执行*/
$(function () {

    /*执行I18n翻译*/
    execI18n();

    /*将语言选择默认选中缓存中的值*/
    if (!i18nLanguage || i18nLanguage == "zh-CN") {
        i18nLanguage = "zh-CN";
        // $("#language").text("英");
    } else {
        // $("#language").text("中");
    }

    /*将语言选择默认选中缓存中的值*/

    /* 选择语言 */
    /* 选中中文 */
    $("#language").on('click', function () {
        var language = $("#language").text();
        console.log(language, '中文');
        if ("En" == language) {
            language = 'en';
            // $("#language").text("中");
        } else if ("中" == language) {
            language = 'zh-CN';
            // $("#language").text("英");
        }

        getCookie("userLanguage", language, {
            expires: 30,
            path: '/'
        });

        location.reload();

    });
    /* 选中英文 */
    $("#language_E").on('click', function () {
        var language = $("#language_E").text();
        console.log(language, '英文');
        if ("En" == language) {
            language = 'en';
            // $("#language").text("中");
        } else if ("中" == language) {
            language = 'zh-CN';
            // $("#language").text("英");
        }

        getCookie("userLanguage", language, {
            expires: 30,
            path: '/'
        });

        location.reload();
    });
});
