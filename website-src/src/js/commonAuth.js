(function() {
  $(document).ready(function() {
    if (!Array.prototype.indexOf){
      Array.prototype.indexOf = function(elt /*, from*/) {
        var len = this.length >>> 0;
        var from = Number(arguments[1]) || 0;
        from = (from < 0)
            ? Math.ceil(from)
            : Math.floor(from);
        if (from < 0)
          from += len;
        for (; from < len; from++) {
          if (from in this && this[from] === elt)
            return from;
        }
        return -1;
      };
    }
    if (!Array.prototype.filter) {
      Array.prototype.filter = function(fun /*, thisp */) {
        "use strict";
        if (this === void 0 || this === null)
          throw new TypeError();
        var t = Object(this);
        var len = t.length >>> 0;
        if (typeof fun !== "function")
          throw new TypeError();
        var res = [];
        var thisp = arguments[1];
        for (var i = 0; i < len; i++) {
          if (i in t)
          {
            var val = t[i]; // in case fun mutates this
            if (fun.call(thisp, val, i, t))
              res.push(val);
          }
        }
        return res;
      };
    }

    var allAuthway = [];
    var qrParam = {};
    var param = {};
    var paramsmsauth = {};
    var paramvoiceauth = {};
    var hasSendsms = false;
    var homeActionCount = null;


    $(document).on("click", ".qr-code-fresh", function() {
      responseHandler.freshQRCode(true);
    });


    // 忘记密码
    $('.forgetPassword').click(function () {
      var params = window.location.href.split("?");
      params = params.length > 1 ? params[1] : "";
      $(location).attr('href', './reset_pwd.html?' + params);
    });
    // 切换默认登录项
    $(document).on("click", ".default-title-body>div.tab", function() {
      if(homeActionCount == 1) {
        return;
      }
      var currLoginIndex = $(this).attr("id").split("_")[1];
      $(this).addClass("active").siblings().removeClass("active");
      $("#defaultway_"+currLoginIndex).addClass("active").siblings(".otherways-login").removeClass("active");
      remoteCall.currLoginway(currLoginIndex);
    });
    window.loginPage = {
      allAuthway: allAuthway,
      qrParam: qrParam,
      param: param,
      paramsmsauth: paramsmsauth,
      paramvoiceauth: paramvoiceauth,
      hasSendsms: hasSendsms,
      homeActionCount: homeActionCount,
    }
    // 发送验证码
    $(document).on("click", ".countdown", function (e) {
      e.preventDefault();
      var _this = $(this);
      var name = _this.parents(".otherways-login").find("input[name='username']");
      var captcha = _this.parents('[data-index]').find("input[name='captcha']").val();
      var currAction = _this.parents(".otherways-login").attr("id").split("_")[1];
      if(!name.val()){
        layer.msg(name.attr('placeholder'),{icon:2});
        return;
      }
      _this.html("请稍等...");
      _this.css({"background": "#C4E1FF", "pointer-events": "none"});
      _this.attr("disabled", "disabled");
      var sendHtml = "发送短信";
      if(currAction === 'voiceauth') {
        sendHtml = $.i18n.prop('appLoginway18');
      }
      remoteCall.preauth(currAction, name.val(), function(status, result) {
        if(status === 1000) {
          hasSendsms = true;
          loginPage.param = result;
          loginPage['param'+currAction] = result;
          var count = 59;
          _this.html(count);
          remoteCall['timer'+currAction] = setInterval(function() {
            count--;
            _this.html(count);
            if(count == 0) {
              clearInterval(remoteCall['timer'+currAction]);
              _this.html(sendHtml);
              _this.css({"background": "#3C9AFF", "pointer-events": "auto"});
              _this.removeAttr("disabled");
            }
          }, 1000);
        }else {
          _this.html(sendHtml);
          _this.css({"background": "#3C9AFF", "pointer-events": "auto"});
          _this.removeAttr("disabled");
        }
      }, captcha);
    });
    $(document).on("click", ".login-submit, .otherways-login .moreways", function(e) {
      e.preventDefault();
      var _this = $(this);
      var postdata = remoteCall.getPostData(_this);
      if(!postdata){
        return;
      }
      var titleHtml = _this.parents(".otherways-login").children(".title").html();
      var btnHtml = _this.html();
      // ["ad", "qrcode", "akey", "dynpwd", "smsauth", "voiceauth", "tempauthcode", "faceauth", "voiceprintauth", "staticpwd", "wechat", "dingtalk"];
      var currAction = _this.parents(".otherways-login").attr("id").split("_")[1];
      var name = _this.parents('[data-index]').find("input[name='username']").val();
      var code = _this.parents('[data-index]').find("input[name='authcode']").val();
      var captcha = _this.parents('[data-index]').find("input[name='captcha']").val();
      _this.attr('disabled', 'disabled');
      _this.css({"background": '#C4E1FF'});
      if(currAction === 'staticpwd' || currAction === 'dynpwd' || currAction === 'tempauthcode') {
        remoteCall.preauth(currAction, name, function(status, result) {
          if(status === 1000) {
            var publicKey = result.publicKey;
            var encryptorConfig = result.alg;
            var encryptor = new CimsEncryptor(encryptorConfig);
            if(code!=null && code!=""){
              code = encryptor.encrypt(publicKey, code + "|" + result.salt);
            }
            setTimeout(function() {
              _this.removeAttr('disabled');
              _this.css({"background": "#3C9AFF"});
            }, 2000);
            remoteCall.action(currAction, name, code, function(status, nextActions) {
              if(status === 1000) {
                if(nextActions[0] === 'password_change') {
                  remoteCall.preauth("password_change", name, function(status, result) {
                    if(status === 1000) {
                      var resetTitle = "您的密码已过期";
                      $(".noModify").hide();
                      if(result.modifyPwdRemainingDays && result.modifyPwdRemainingDays !== 0) {
                        resetTitle = "您的密码还有" + result.modifyPwdRemainingDays + "天过期";
                        $(".noModify").show();
                      }
                      loginPage.param = result;
                      $(".reset-pwd .title1").text(resetTitle);
                      $(".reset-pwd").addClass('fadeInRight animated infinite');
                      $(".default-way, .iframe-box").addClass('fadeOutRightBig animated infinite');
                      $(".reset-pwd .password_change").find("input[name='oldPassword']").val("");
                      $(".reset-pwd .password_change").find("input[name='newPassword']").val("");
                      $(".reset-pwd .password_change").find("input[name='newPasswordConf']").val("");
                      $(".reset-pwd").show();
                      if(_this.parents(".otherways-login").parent(".loginway-body").parent().hasClass("default-way")) {
                        $(".default-way").addClass("changePwdMark");
                      }else {
                        $(".iframe-box").addClass("changePwdMark");
                      }
                      $(".changePwdMark").hide();
                      setTimeout(function() {
                        remoteCall.removeClass();
                      }, 500);
                    }
                  });
                  return;
                }
                loginPage.allAuthway = nextActions;
                remoteCall.onNextAction();
              }
            }, null, captcha);
          }else {
            _this.removeAttr('disabled');
            _this.css({"background": "#3C9AFF"});
          }
        }, captcha);
      }else if(currAction == 'qrcode') {
      }else if(currAction == 'smsauth' || currAction == 'voiceauth') {
        if(!hasSendsms) {
          layer.msg( $.i18n.prop('resetPwd21'),{icon:2});
          _this.removeAttr('disabled');
          _this.css({"background": "#3C9AFF"});
          return;
        }
        var result = loginPage['param'+currAction];
        var publicKey = result.publicKey;
        var encryptorConfig = result.alg;
        var encryptor = new CimsEncryptor(encryptorConfig);
        if(code!=null && code!=""){
          code = encryptor.encrypt(publicKey, code + "|" + result.salt);
        }
        setTimeout(function() {
          _this.removeAttr('disabled');
          _this.css({"background": "#3C9AFF"});
        }, 2000);
        remoteCall.action(currAction, name, code, function(status, nextActions) {
          if(status === 1000) {
            loginPage.allAuthway = nextActions;
            remoteCall.onNextAction();
          }
        }, null, captcha);
      }else if(currAction == 'ad' || currAction == 'wechat' || currAction == 'dingtalk') {
        remoteCall.preauth(currAction, name, function(status, result) {
          _this.removeAttr('disabled');
          _this.css({"background": "#fff"});
        }, captcha);
      }else if(currAction == 'akey' || currAction == 'faceauth' || currAction == 'voiceprintauth') {
        _this.html("请稍等...");
        remoteCall.preauth(currAction, name, function(status, result) {
          if(status === 1000) {
            responseHandler.countDown(currAction, _this, btnHtml, titleHtml);
            _this.next(".login-mess-one").children("p").html($.i18n.prop('appLoginway09')+"“" + remoteCall.appname + "”App" + titleHtml);
          }else {
            _this.html(btnHtml);
            _this.css({"background": "#3C9AFF"});
            _this.removeAttr('disabled');

          }
        }, captcha);
      }
    })

    $(document).on("click", ".reset-back", function() {
      $(".reset-pwd").addClass('fadeOutRight animated infinite');
      $(".default-way").addClass('fadeInRight animated infinite');
      // $(".changePwdMark").addClass('fadeInRight animated infinite');
      // $(".changePwdMark").hide();
      setTimeout(function() {
        remoteCall.removeClass();
      }, 200);
      $(".reset-pwd").hide();
      $(".default-way").show();
      // $(".changePwdMark").show();
      $(".changePwdMark").removeClass("changePwdMark");
      initPage.getAllAuthway();
    })
    $(".noModify span").click(function(e) {
      resetFun($(this));
    })

    $(document).on("click", ".reset-pwd .reset-submit", function(e) {
      e.preventDefault();
      var _this = $(this);
      var postdata = remoteCall.getPostData(_this);
      if(!postdata){
        return;
      }
      resetFun(_this, 'modify');
    })
    function resetFun(_this, isModify) {
      var name = $(".password-login").find("input[name='username']").val() || $("#moreLoginBox").contents().find(".password-login input[name='username']").val();
      // var code = $(".password-login").find("input[name='authcode']").val() || $("#moreLoginBox").contents().find(".password-login input[name='authcode']").val();
      var publicKey = loginPage.param.publicKey;
      var encryptorConfig = loginPage.param.alg;
      var encryptor = new CimsEncryptor(encryptorConfig);
      var pwdObj = {};
      if(isModify) {
        var oldPassword = _this.parents('[data-index]').find("input[name='oldPassword']").val();
        var newPassword = _this.parents('[data-index]').find("input[name='newPassword']").val();
        pwdObj = {
          oldPassword: oldPassword,
          newPassword: newPassword
        }
        if(oldPassword!=null && oldPassword!="" && newPassword!=null && newPassword!=""){
          pwdObj = encryptor.encrypt(publicKey, JSON.stringify(pwdObj) + "|" + loginPage.param.salt);
        }
        _this.attr('disabled', 'disabled');
        _this.css({"background": '#C4E1FF'});
      }else {
        pwdObj = encryptor.encrypt(publicKey, "|" + loginPage.param.salt);
        _this.css({"coloe": '#C4E1FF'});
      }
      remoteCall.action("password_change", name, pwdObj, function(status, nextActions) {
        if(status === 1000) {
          $(".reset-pwd").hide();
          _this.removeAttr('disabled');
          _this.css({"background": "#3C9AFF"});
          loginPage.allAuthway = nextActions;
          remoteCall.onNextAction();
        }else {
          if(isModify) {
            _this.removeAttr('disabled');
            _this.css({"background": "#3C9AFF"});
          }else {
            _this.css({"coloe": '#3C9AFF'});
          }
          if(status === 9065){
            $(".reset-back").click();
          }
        }
      });
    }
    $(document).on("click", ".capcha-img-code", function() {
      remoteCall.refreshCaptcha(function(status, result) {

      })
    })

  })
})()
