/**
 * 第三方应用对接js-sdk
 */
(function () {
    function Cims(config) {
        var me = this;

        // 初始化参数
        this.resetConfig();
        checkParam(config);
        this._setAllConfig(config);

        var sign = this.getConfig("sign");
        this._setConfig("appSig", sign.substr(sign.indexOf("APP\|")));

        // 渲染页面
        this.renderIframe();

        // 认证页面 数据传递
        window.addEventListener('message', function(e) {
            var message = e.data;
            // 拦截认证页面传来的数据
            if (new RegExp("^" + e.origin).test(me.getConfig("cimsUrl"))) {
                var data = JSON.parse(e.data);
                var status = data.status;
                if (status === 1000) {
                    me._publish("onAuthSuccess", data.data.sign);
                } else if(status === 8000){
                    window.location.href = data.data.url + "&url=" + window.location.href;
                } else {
                    me._publish("onAuthFailed", status, data);
                }
            }
        });

        return this;
    }

    /**
     * 渲染Iframe
     */
    Cims.prototype.renderIframe = function () {
        var iFrame = this.getConfig("iframe"),
            sign = this.getConfig("sign"),
            cimsUrl = this.getConfig("cimsUrl"),
           authType = this.getConfig("authType");
        var domIframe = isDOM(iFrame) ? iFrame : document.getElementById(iFrame);
        if (!domIframe) {
            throw new Error("找不到iFrame: {" + iFrame + "}");
        }

        // 组装url
        domIframe.src = [
            cimsUrl,
            (cimsUrl[cimsUrl.length - 1] === '/' ? '' : '/'),
            'frame',
            '?sign=' + encodeURIComponent(sign),
            '&originHref=' + encodeURIComponent(window.location.href),
            '&authType=' + authType
        ].join("");
    };

    Cims.prototype.successAuthCallback = function (callback) {
        if (callback) {
            this.subscribe("onAuthSuccess", callback);
        }
    };

    Cims.prototype.failedCallback = function (callback) {
        if (callback) {
            this.subscribe('onAuthFailed', callback);
        }
    };

    /**
     * 检查参数
     * @param config
     */
    function checkParam(config) {
        if (!config) {
            throw new Error("参数不能为空");
        }

        if (!config.sign) {
            throw new Error("sign 不能为空");
        }

        if (!config.cimsUrl) {
            throw new Error("cimsUrl 不能为空");
        }

        if (!config.iframe) {
            throw new Error("iframe 不能为空");
        }
    }

    Cims.config = function(config) {
        return new Cims(config);
    };

    Cims.prototype._setConfig = function (name, value) {
        this._config[name] = value;
        return value;
    };

    Cims.prototype._setAllConfig = function (config) {
        this._config = extend(this._config, config)
    };

    Cims.prototype.getConfig = function (name) {
        return this._config[name];
    };

    Cims.prototype.resetConfig = function () {
        this._config = {};
    };


    /*******************
     * 事件注册 *
     *******************/

    /**
     * 事件订阅
     * @param topic 主题
     * @param func 事件回调
     */
    Cims.prototype.subscribe = function (topic, func) {
        var functions = this.getConfig("eventFunctions");
        if (!functions) {
            functions = this._setConfig("eventFunctions", {});
        }

        if (!functions[topic]) {
            functions[topic] = [];
        }

        functions[topic].push(func);
    };

    /**
     * 事件发布
     * @param topic 主题
     * @param args 参数
     * @returns {boolean} 返回false 则没人订阅
     */
    Cims.prototype._publish = function (topic) {
        var functions = this.getConfig("eventFunctions");
        if (!functions || !functions[topic]) {
            return false;
        }

        var args = Array.prototype.slice.call(arguments, 1);

        setTimeout(function () {
            var subscribers = functions[topic],
                len = subscribers ? subscribers.length : 0;

            while (len--) {
                subscribers[len].apply(this, args);
            }
        }, 0);

        return true;
    };

    /**
     * 退订
     * @param topic 主题
     * @param func 方法
     */
    Cims.prototype.unSubscribe = function (topic, func) {

        var functions = this.getConfig("eventFunctions");
        if (!functions || !functions[topic]) {
            return;
        }

        var subscribers = functions[topic];

        for (var i = subscribers.length - 1; i >= 0; i--) {
            if (subscribers[i] === callback) {
                delete (subscribers[i]);
            }
        }
    };


    function extend ( defaults, options ) {
        var extended = {};
        var prop;
        for (prop in defaults) {
            if (Object.prototype.hasOwnProperty.call(defaults, prop)) {
                extended[prop] = defaults[prop];
            }
        }
        for (prop in options) {
            if (Object.prototype.hasOwnProperty.call(options, prop)) {
                extended[prop] = options[prop];
            }
        }
        return extended;
    }

    function isDOM(item) {
        return (typeof HTMLElement === 'function')
            ? (item instanceof HTMLElement)
            : (item && (typeof item === 'object') && (item.nodeType === 1) && (typeof item.nodeName === 'string'));
    }

    window.Cims = Cims;
})();
