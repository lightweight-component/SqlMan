(function ($) {

    function CimsService(config) {

        // 初始化参数
        var me = this;
        this.resetConfig();
        checkParam(config);
        this._setAllConfig(config);

        var sign = this.getConfig("sign");
        this._setConfig("signApp", sign.substr(sign.indexOf("APP\|")));
        this._setConfig("signXd", sign.substr(0, sign.indexOf(":APP\|")));

        // 监控扫码认证结果
        this._getAsyncAuthResult(config.token, "qrcode");


        // 页面过期跟踪
        var expireDateTime = this.getConfig("expireDateTime");
        if (expireDateTime) {
            monitorDate(expireDateTime, function () {
                me.messageParent({
                    status: 9103,
                    message: "sign expired",
                    data: {
                        expireDate: expireDateTime
                    }
                });
            });
        }

        return this;
    }

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

        if (!config.token) {
            throw new Error("Token 不能为空");
        }
    }

    /**
     * 到期后调用回调
     * @param strDate 时间 日期
     * @param callback 回调
     */
    function monitorDate(strDate, callback) {
        var mDate = new Date(strDate);
        var timeStep = 200;

        setTimeout(f, timeStep);

        function f() {
            var now = new Date();
            if (now.getTime() > mDate.getTime()) {
                    callback();
            } else {
                setTimeout(f, timeStep);
            }
        }
    }

    /**
     * 异步认证成功后调用
     */
    CimsService.prototype.onAsyncAuthSuccess = function (callback) {
        this.subscribe("asyncAuthSuccess", callback);
    };


    CimsService.config = function (config) {
        return new CimsService(config);
    };


    /*******************
     * 认证实现
     * 认证分类:
     * 1. 密码, 动态密码, 短信验证码, 语音验证码, 临时授权码... 直接返回认证结果
     * 2. 一键, 人脸, 声纹, 扫码, 微信... 发送登录请求 待认证完成返回认证结果
     *******************/

    /**
     * 验证动态令牌
     * @param account 账号
     * @param code 动态令牌
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authDynpwd = function (account, code, successCb, failedCb) {
        this._checkAuthCode(account, code, "dynpwd", successCb, failedCb);
    };

    /**
     * 验证临时验证码
     * @param account 账号
     * @param code 临时验证码
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authTempauthcode = function (account, code, successCb, failedCb) {
        this._checkAuthCode(account, code, "tempauthcode", successCb, failedCb);
    };

    /**
     * 验证短信验证码
     * @param account 账号
     * @param code 短信验证码
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authSms = function (account, code, successCb, failedCb) {
        var token = this.getConfig('last_sms_token');
        if (!token) {
            failedCb({msg: "请先发送验证码！"})

        }
        this._apiCheckAuthCode(account, code, "smsauth", token, successCb, failedCb);
    };

    /**
     * 验证语音验证码
     * @param account 账号
     * @param code 短信验证码
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authVoice = function (account, code, successCb, failedCb) {
        var token = this.getConfig('last_voice_token');
        if (!token) {
            failedCb({msg: "请先发送语音！"})

        }
        this._apiCheckAuthCode(account, code, "voiceauth", token, successCb, failedCb);
    };

    /**
     * 验证静态密码
     * @param account 账号
     * @param code 口令
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authStaticpwd = function (account, code, successCb, failedCb) {
        this._checkAuthCode(account, code, "staticpwd", successCb, failedCb);
    };

    /**
     * 唤起善认APP认证
     * @param successCb
     */
    CimsService.prototype.callupApp = function () {
        // window.location.href = 'trusfort://cims?op=callupApp&cimsToken=' + this.getConfig("callupAppToken");
        var token = this.getConfig("token");
        this.messageParent({
            status: 8000,
            data: {
                url: 'trusfort://cims?op=callupApp&cimsToken=' + token
            }
        })
    };

    /**
     * 发送一键推送请求
     * @param account 账号
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authAkey = function (account, successCb, failedCb) {
        var me = this;
        this._applyForToken(account, 'akey', function (token, data) {
            me._getAsyncAuthResult(token, 'akey');
            if (successCb) {
                successCb(token, data);
            }
        }, failedCb);
    };

    /**
     * 发送人脸验证请求
     * @param account 账号
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authFace = function (account, successCb, failedCb) {
        var me = this;
        this._applyForToken(account, 'faceauth', function (token, data) {
            me._getAsyncAuthResult(token, 'faceauth');
            if (successCb) {
                successCb(token, data);
            }
        }, failedCb);
    };

    /**
     * 发送声纹验证请求
     * @param account 账号
     * @param successCb 成功回调
     * @param failedCb 失败回调
     */
    CimsService.prototype.authVoiceprint = function (account, successCb, failedCb) {
        var me = this;
        this._applyForToken(account, 'voiceprintauth', function (token, data) {
            me._getAsyncAuthResult(token, 'voiceprintauth');
            if (successCb) {
                successCb(token, data);
            }
        }, failedCb);
    };

    /**
     * 发送短信验证码
     */
    CimsService.prototype.smsSend = function (account, successCb, failedCb) {
        var me = this;
        me._applyForToken(account, 'smsauth', function (token) {
            me._setConfig('last_sms_token', token);
            if ($.isFunction(successCb)) {
                successCb(token);
            }
        }, failedCb);
    };


    /**
     * 发送短信验证码
     */
    CimsService.prototype.voiceSend = function (account, successCb, failedCb) {
        var me = this;
        me._applyForToken(account, 'voiceauth', function (token) {
            me._setConfig('last_voice_token', token);
            if ($.isFunction(successCb)) {
                successCb(token);
            }
        }, failedCb);
    };

    /**
     * 发送认证请求, 并获得一个token
     *
     * @param account 账号
     * @param authType 认证类型
     * @param successCb 成功回调
     * @param failedCb 失败回调
     * @private
     */
    CimsService.prototype._applyForToken = function (account, authType, successCb, failedCb) {
        var me = this;
        var data = {
            sign: me.getConfig("sign"),
            username: account,
            type: me._authType2AuthTypeCode(authType)
        };

        $.ajax({
            type: 'POST',
            url: 'api/verify/othertype',
            data: data,
            dataType: 'json',
            success: function (data) {
                var status = data.status;
                if (status === 1000 || status === 9009) {
                    me._setConfig("publicKey", data.response_body.publicKey);
                    if ($.isFunction(successCb)) {
                        successCb(data.response_body.token, data);
                    }
                } else {
                    if ($.isFunction(failedCb)) {
                        failedCb(data);
                    }
                    me._publish("ajaxError", data);
                }
            }

        });
    };

    /**
     * 传递数据到父页面
     * @param data 传递的数据
     */
    CimsService.prototype.messageParent = function (data) {
        var me = this;
        var originHref = me.getConfig("originHref");
        if (!originHref) {
            originHref = "*";
        }
        parent.window.postMessage(JSON.stringify(data), originHref);
    };

    /**
     * 异步认证接口
     * @param token
     * @param authType 认证类型
     * @private
     */
    CimsService.prototype._getAsyncAuthResult = function (token, authType) {
        var me = this;
        $.ajax({
            method: 'POST',
            type  : 'json',
            url:'api/verify/getauthresult',
            data:{
                status: 9009,
                sign: me.getConfig("signXd"),
                token: token,
                atype: me._authType2AuthTypeCode(authType)
            },
            success: function(data){
                if(data.status === 1000) {
                    var sign = data.response_body.userInfoSign + ':' + me.getConfig("signApp");
                    me.messageParent({
                        status: 1000,
                        data: {
                            sign: sign
                        }
                    });
                    me._publish("asyncAuthSuccess", sign, data);
                } else if (data.status === 9020 && data.msg === 'try_again'){
                    me._getAsyncAuthResult(token);
                } else if (data.status === 9017) {
                    // 用户拒绝时 没有返回错误类型 此处填写错误类型
                    var errorData = {
                        status: 9017,
                        msg: '移动端已拒绝认证请求。',
                        message: '移动端已拒绝认证请求。'
                    };
                    me._publish("ajaxError", errorData);
                    me.messageParent(errorData)

                } else if (data.status === 9900) {
                    //service=地址错误时前端跳转取消签名错误提示页面
                } else {
                    me._publish("ajaxError", data);
                }
            },
            error: function(XMLHttpRequest, textStatus, errorThrown){
                me._publish("ajaxError", XMLHttpRequest);
            }
        });
    };

    /**
     * 立即返回的认证
     * @param account 账号
     * @param code code
     * @param authType 认证类型
     * @param successCb 认证成功回调
     * @param failedCb 认证失败回调
     * @private
        */
    CimsService.prototype._checkAuthCode = function (account, code, authType, successCb, failedCb) {
        var me = this;
        // 先申请token 再认证
        me._applyForToken(account, authType, function (token) {
            me._apiCheckAuthCode(account, code, authType, token, successCb, failedCb);
        }, function (respData) {
            if ($.isFunction(failedCb)) {
                failedCb(respData);
            }
        });
    };

    CimsService.prototype._apiCheckAuthCode = function (account, code, authType, token, successCb, failedCb) {
        var me = this;
        var encrypt = new JSEncrypt();
        var publicKey = me.getConfig("publicKey");
        encrypt.setPublicKey(publicKey);
        code = encrypt.encrypt(code + '|' + token);
        $.ajax({
            type: 'POST',
            url: 'api/verify/checkAuthcode',
            data: {
                sign: me.getConfig('signXd'),
                token: token,
                authcode: code || '',
                type: me._authType2AuthTypeCode(authType),
                username: account
            },
            dataType: 'json',
            success: function (data) {
                if (data.status === 1000) {
                    me.messageParent({
                        status: 1000,
                        data: {
                            sign: data.response_body.userInfoSign + ":" + me.getConfig("signApp")
                        }
                    });
                    if ($.isFunction(successCb)) {
                        successCb(data);
                    }
                } else {
                    if ($.isFunction(failedCb)) {
                        failedCb(data);
                    }
                    me._publish("ajaxError", data);
                }
            }
        });
    };

    /**
     * 认证类型转换为认证类型代码
     * 原代码接口只认数字认证类型
     * @param authType 字符串类型的认证类型
     * @returns {number} 数字类型的认证类型
     * @private
     */
    CimsService.prototype._authType2AuthTypeCode = function (authType) {
        switch (authType) {
            // 扫码登录
            case "qrcode":
                return 0;
            // 一键登录
            case "akey":
                return 1;
            // 动态密码
            case "dynpwd":
                return 2;
            // 语音验证码验证
            case "smsauth":
                return 3;
            // 语音验证码验证
            case "voiceauth":
                return 4;
            // 临时授权码
            case "tempauthcode":
                return 5;
            // 人脸
            case "faceauth":
                return 6;
            // 声纹
            case "voiceprintauth":
                return 7;
            // 静态密码
            case "staticpwd":
                return 8;
            // 微信
            case "wechat":
                return 9;
            case "callupApp":
                return 10;
            case "dingtalk":
                return 11;
        }
    };



    /*******************
     * 配置相关
     *******************/

    /**
     * 设置参数
     * @param name 名称
     * @param value 值
     * @returns {*}
     * @private
     */
    CimsService.prototype._setConfig = function (name, value) {
        this._config[name] = value;
        return this._config[name];
    };

    /**
     * 设置全部参数
     * @param config
     * @private
     */
    CimsService.prototype._setAllConfig = function (config) {
        this._config = $.extend(this._config, config)
    };

    /**
     * 获取参数
     * @param name 名称
     * @returns {*}
     */
    CimsService.prototype.getConfig = function (name) {
        return this._config[name];
    };

    /**
     * 重置参数
     */
    CimsService.prototype.resetConfig = function () {
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
    CimsService.prototype.subscribe = function (topic, func) {
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
    CimsService.prototype._publish = function (topic) {
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
    CimsService.prototype.unSubscribe = function (topic, func) {

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

    window.CimsService = CimsService;


})($);


