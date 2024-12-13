define(['layer', 'jquery', 'i18n'], function (layer, $, i18n) {
    function getQueryFromSearch(search) {
        if (!search) {
            return {};
        }

        var urlSplit = search.split('?');
        var url_data = urlSplit.length > 1 ? urlSplit[1] : null;
        if (!url_data) {
            return {};
        }
        var res = {};
        var params_arr = url_data.split('&');
        $.each(params_arr, function (i, item) {
            if (!item) {
                return;
            }
            var splitItem = item.split('=');
            var key = splitItem[0];
            res[key] = decodeURIComponent(splitItem[1]);
        });

        return res;
    }

    function getQueryByLocalSearch() {
        return getQueryFromSearch(window.location.search);
    }

    function warn(content, options) {
        if (!options) {
            options = { icon: 2 };
        }
// debugger
        // alert(content);
        layer.msg(content, options);
    }

    function info(content, options) {
        if (!options) {
            options = { icon: 1 };
        }
        layer.msg(content, options);
    }


    /**
     * 按钮倒计时方法
     * @param {jQuery|HTMLElement} domBtn
     * @param {number} [second] 倒计时时间，单位：秒；默认60s
     * @param {function} [btnMsgFormatter] 按钮信息格式化方法，返回按钮的文字信息；默认显示：“xxs后重试”
     * @param {number} btnMsgFormatter.count 剩余倒计时的秒数；
     */
    var currentBtnId = 0;
    var btnEntitys = {};
    function btnCountDown(domBtn, second, btnMsgFormatter) {
        var btn = domBtn instanceof $ ? domBtn : $(domBtn);
        var btnId = btn.attr('data-btn-id');
        var originVal = $(btn).html();
        btn.attr("disabled", true);
        if (btnId && btnEntitys[btnId]) {
            return btnEntitys[btnId];
        }

        if (!second) {
            second = 60;
        }
        var endTime;

        if (!btnMsgFormatter) {
            btnMsgFormatter = defaultBtnMsgFormatter;
        }

        var stop = false;
        var currentTimeoutId;
        function countDown() {
            if (stop) {
                reset();
                return;
            }
            var dis = endTime - new Date().getTime();
            if (dis > 0) {
                var count = parseInt(dis / 1000);
                btn.html(btnMsgFormatter(count));
                currentTimeoutId = setTimeout(countDown, 150);
            } else {
                reset();
            }
        }

        function defaultBtnMsgFormatter(count) {
            return count + 's ' + i18n.propLocal('abmLogin20')
        }

        function reset() {
            btn.html(originVal);
            btn.attr("disabled", false);
            if (currentTimeoutId) {
                clearTimeout(currentTimeoutId);
            }
            stop = true;
            return this;
        }

        function start() {
            endTime = new Date().getTime() + second * 1000;
            stop = false;
            btn.attr("disabled", true);
            countDown();
            return this;
        }

        function ready() {
            btn.html(i18n.propLocal('resetPwd19'));
            return this;
        }

        ready();

        var result = {
            reset: reset,
            start: start,
            ready: ready
        };

        btnId = currentBtnId++;
        btnEntitys[btnId] = result;
        btn.attr('data-btn-id', btnId);
        return result;

    }

    function placeholderOfIE() {
        if (!_isIE8()) {
            return;
        }
        requirejs(['placeholder'], function () {
            $('input, textarea').placeholder();
        });
    }

    function _isIE8() {
        return !!window['isIE8'];
    }

    return {
        getQueryByLocalSearch: getQueryByLocalSearch,
        warn: warn,
        info: info,
        btnCountDown: btnCountDown,
        placeholderOfIE: placeholderOfIE
    };
});
