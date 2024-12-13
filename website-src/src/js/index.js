(function() {
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
  // allAuthway = ["ad", "qrcode", "akey", "dynpwd", "smsauth", "voiceauth", "tempauthcode", "faceauth", "voiceprintauth", "staticpwd", "wechat", "dingtalk"];
  $(document).ready(function () {
    var defaultLoginWay = [];
    var moreLoginWay = [];
    var allAuthway = [];
    var bgColor = '';
    var appname = "";
    // 获取所有认证方式
    function getAllAuthway() {
      clearInterval(remoteCall.timerakey);
      clearInterval(remoteCall.timersmsauths);
      clearInterval(remoteCall.timervoiceauth);
      clearInterval(remoteCall.timerfaceauth);
      clearInterval(remoteCall.timervoiceprintauth);
      // clearInterval(remoteCall.timer);
      responseHandler.captchaFun('hide');
      $(".otherways-login").find("input[name='username']").val("");
      $(".otherways-login").find("input[name='authcode']").val("");
      $(".otherways-login").find("input[name='username']").removeAttr("readonly");
      $(".otherways-login").find("input[name='username']").removeAttr("unselectable");
      $("#defaultway_akey").find(".login-submit").html($.i18n.prop('appLoginway10'));
      $("#defaultway_akey").find(".login-submit").removeAttr("disabled");
      $("#defaultway_akey").find(".login-submit").css({"background": "#3C9AFF"});
      $("#defaultway_simauth").find(".login-submit").html($.i18n.prop('appLoginway111'));
      $("#defaultway_simauth").find(".login-submit").removeAttr("disabled");
      $("#defaultway_simauth").find(".login-submit").css({"background": "#3C9AFF"});
      $("#defaultway_smsauth").find(".countdown").html($.i18n.prop('appLoginway15'));
      $("#defaultway_smsauth").find(".countdown").css({"background": "#3C9AFF", "pointer-events": "auto"});
      $("#defaultway_voiceauth").find(".countdown").html($.i18n.prop('appLoginway18'));
      $("#defaultway_voiceauth").find(".countdown").css({"background": "#3C9AFF", "pointer-events": "auto"});
      $("#moreLoginBox").contents().find(".tip").hide();
      $("#moreLoginBox").contents().find(".otherways-login input[name='username']").val("");
      $("#moreLoginBox").contents().find(".otherways-login input[name='authcode']").val("");
      $("#moreLoginBox").contents().find(".otherways-login input[name='username']").removeAttr("readonly");
      $("#moreLoginBox").contents().find(".otherways-login input[name='username']").removeAttr("unselectable");
      $("#moreLoginBox").contents().find("#otherway_akey .login-submit").html($.i18n.prop('appLoginway10'));
      $("#moreLoginBox").contents().find("#otherway_akey .login-submit").removeAttr("disabled");
      $("#moreLoginBox").contents().find("#otherway_akey .login-submit").css({"background": "#3C9AFF"});
      $("#moreLoginBox").contents().find("#otherway_simauth .login-submit").html($.i18n.prop('appLoginway111'));
      $("#moreLoginBox").contents().find("#otherway_simauth .login-submit").removeAttr("disabled");
      $("#moreLoginBox").contents().find("#otherway_simauth .login-submit").css({"background": "#3C9AFF"});
      $("#moreLoginBox").contents().find("#otherway_smsauth .countdown").html($.i18n.prop('appLoginway15'));
      $("#moreLoginBox").contents().find("#otherway_smsauth .countdown").css({"background": "#3C9AFF", "pointer-events": "auto"});
      $("#moreLoginBox").contents().find("#otherway_voiceauth .countdown").html($.i18n.prop('appLoginway18'));
      $("#moreLoginBox").contents().find("#otherway_voiceauth .countdown").css({"background": "#3C9AFF", "pointer-events": "auto"});
      remoteCall.init([], function (actions, responseData) {
        // bgColor = responseData.page.backcolor ? responseData.page.backcolor : "#3C9AFF";
        // $(".tbody").css({'background': bgColor});
        var loginlogo = responseData.page.loginlogo ? ("api/file/get?id=" + responseData.page.loginlogo) : "./image/logo.png";
        var systemname = responseData.page.systemname ? responseData.page.systemname : $.i18n.prop('authApp01');
        var bannerimg = responseData.page.bannerimg ? ("api/file/get?id=" + responseData.page.bannerimg) : "./image/login-bgimg.png";
        appname = responseData.page.appName;
        var portalActiviti=responseData.portalActiviti;
        if(portalActiviti && "off"===portalActiviti){
          $(".header-body .register").hide();
        }
        if(loginlogo) {
          $(".logo img").attr("src", loginlogo);
        }
        if(systemname) {
          $(".logo .logo-name").text(systemname);
        }
        if(bannerimg) {
          $(".login-body").css({"background-image": "url("+bannerimg+")", "filter": "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + bannerimg + "', sizingMethod='scale')", "-ms-filter": "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + bannerimg + "',sizingMethod='scale')"});
          // $(".login-body").css({"background-image": "url(api/file/get?id=" + bannerimg + ")", "filter": "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + bannerimg + "', sizingMethod='scale')", "-ms-filter": "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + bannerimg + "',sizingMethod='scale')"});
        }
        if(appname) {
          $(".app-name").html(appname);
          $("#moreLoginBox").contents().find(".app-name").html(appname);
          $("#defaultway_dynpwd").find("input[name='authcode']").attr("placeholder", "请输入“" + appname + "”app上的六位数动态令牌");
        }
        allAuthway = actions.filter(function (val) { return ["qrcode", "akey", "dynpwd", "smsauth", "voiceauth", "tempauthcode", "faceauth", "voiceprintauth", "staticpwd", "wechat", "dingtalk", "ad"].indexOf(val) > -1 });
        loginPage.homeActionCount = responseData.homeActionCount < allAuthway.length ? responseData.homeActionCount : allAuthway.length;
        if(allAuthway.length <= loginPage.homeActionCount) {
          $(".login-foot-body").hide();
        }
        renderDefaultAuth();
        // crossbrowser
        if(allAuthway.indexOf("qrcode") !== -1) {
          responseHandler.freshQRCode(true);
        }
      });
    }
    getAllAuthway();
    function renderDefaultAuth() {
      defaultLoginWay = allAuthway.slice(0, loginPage.homeActionCount) || [];
      for(var i=0; i<defaultLoginWay.length; i++) {
        // 默认登录项tab
        var currId = defaultLoginWay[i];
        $(".default-title-body").children("#login-tab_"+currId).show();
        $(".default-title-body").children("#login-tab_"+defaultLoginWay[0]).addClass("active");
        if(loginPage.homeActionCount == 1 && i == 0) {
          $(".default-title-body .tab").css({'width': '100%'});
          $(".default-title-body .tab span").css({'fontSize': '29px', 'color': '#333', 'border-bottom': '3px #fff solid'});
          $(".default-title-body").children("#login-tab_"+defaultLoginWay[i]).removeClass("active");
        }
        if(loginPage.homeActionCount == 2 && i == 1) {
          $(".default-title-body .tab").css({'width': '50%'});
          $(".default-title-body").children("#login-tab_"+defaultLoginWay[i]).css({'float': 'right'});
        }
        if(loginPage.homeActionCount == 3 && i== 2) {
          $(".default-title-body .tab").css({'width': '33.3%'});
          $(".default-title-body .tab span").css({'fontSize': '18px'});
          $(".default-title-body").children("#login-tab_"+defaultLoginWay[i]).css({'float': 'right'});
        }
      }
      // 当前选中默认登录项登录方式
      $(".default-title-body").children("#login-tab_"+defaultLoginWay[0]).addClass("active").siblings().removeClass("active");
      $(".default-way-body").children("#defaultway_"+defaultLoginWay[0]).addClass("active").siblings().removeClass("active");
      moreLoginWay = allAuthway.slice(loginPage.homeActionCount, allAuthway.length);
      $(".iframe-box").css({'width': moreLoginWay.length >4 ? moreLoginWay.length*86 + 40 +'px' : '480px'});
    }

    var smallSrceen = false;
    if(document.body.clientWidth <= 1600) {
      smallSrceen = true;
      remoteCall.dynamicLoading.css("./css/style_small.css");
    }

    window.initPage = {
      getAllAuthway: getAllAuthway
    }
    // 更多登录方式
    $(document).on("click", ".login-foot-body .more-defaultway", function() {
      $("#moreLoginBox").contents().find(".app-name").html(appname);
      $(".default-way").addClass('fadeOutRight animated infinite');
      $(".default-way").hide();
      $(".iframe-box").show();
      $(".iframe-box").addClass('fadeInRight animated infinite');
      setTimeout(function() {
        remoteCall.removeClass();
      }, 1000);
      $("#moreLoginBox").contents().find(".otherways-login .title").show();
      $("#moreLoginBox").contents().find(".back").show();
      $("#moreLoginBox").contents().find(".otherways-btns").children(".otherway-button").hide();
      $("#moreLoginBox").contents().find("#otherway_dynpwd input[name='authcode']").attr("placeholder", "请输入“" + appname + "”app上的六位数动态令牌");
      if(document.body.clientWidth <= 1600) {
        remoteCall.attachFile("./css/style_small.css", "css");
      }
      var hasSelectAuth = [];
      for(var i=0; i<moreLoginWay.length; i++) {
        var currId = moreLoginWay[i];
        hasSelectAuth.push(currId);
        $("#moreLoginBox").contents().find(".otherways-btns").children("#otherwaybtn_"+currId).show();
        if(currId == 'qrcode') {
          $(".qr-code-fail, .qr-code-fresh").hide();
          $("#moreLoginBox").contents().find(".qr-code-fail, .qr-code-fresh").hide();
          $("#moreLoginBox").contents().find(".qr-code").attr("src", loginPage.qrParam.qrcodeUri);
        }
      }
      var firstIndexArr = ["qrcode", "akey", "dynpwd", "smsauth", "voiceauth", "tempauthcode", "faceauth", "voiceprintauth", "staticpwd", "wechat", "dingtalk", "ad"].filter(function (val) { return hasSelectAuth.indexOf(val) > -1 });
      $("#moreLoginBox").contents().find(".loginway-body").children("#otherway_"+firstIndexArr[0]).addClass("active").siblings().removeClass("active");
      $("#moreLoginBox").contents().find(".otherways-btns").children("#otherwaybtn_"+firstIndexArr[0]).addClass("active").siblings().removeClass("active");
      $("#moreLoginBox").contents().find(".otherways-btns").children(".otherway-button").children(".icon").css({"background-color": "#fff"});
      $("#moreLoginBox").contents().find(".otherways-btns").children("#otherwaybtn_"+firstIndexArr[0]).children(".icon").css({"background-color": "#3c9aff"});
      remoteCall.currLoginway(currId);
      $("#moreLoginBox").contents().find(".back").bind("click", function() {
        var backIndex = $(this).attr("data-back");
        if(backIndex == "1") {
          $(".default-way").addClass('fadeInRight animated infinite');
          setTimeout(function() {
            remoteCall.removeClass();
            $(".default-way").show();
            $(".iframe-box").hide();
          }, 200);
        } else if(backIndex == "2") {
          remoteCall.removeClass();
          $(".default-way").show();
          $(".iframe-box").hide();
          $("#moreLoginBox").contents().find(".back").attr("data-back", "1");
          $("#moreLoginBox").contents().find(".tip").hide();
          $("#moreLoginBox").contents().find("input[name=username]").removeAttr("readonly");
          $("#moreLoginBox").contents().find("input[name=username]").removeAttr("unselectable");
          getAllAuthway();
        }
      });
    });

    $(document).on("keypress", "input", function(event) {
      if(event.keyCode == 32) {
        event.returnValue = false;
        return false
      }
    })

    $(".btn_bind_qrcode").click(function (event) {
      var e = arguments.callee.caller.arguments[0] || event; // 下面的e改为event，IE运行可以，但是其他浏览器就不兼容
      if (e && e.stopPropagation) {
        e.stopPropagation();
      } else if (window.event) {
        window.event.cancelBubble = true;
      }
      $(".btn_bind_qrcode .bind_windows").toggle();
    });

    $(document).click(function (e) {
      $(".btn_bind_qrcode .bind_windows").hide();
    });

    window.onresize = function(){
      smallSrceen = false;
      if(document.body.clientWidth <= 1600) {
        smallSrceen = true;
      }
      if(smallSrceen) {
        remoteCall.attachFile("./css/style_small.css", "css");
        if(responseHandler.iscaptchaShow) {
          $(".login-div, .iframe-box").css({"height": "459px"});
        }else {
          $(".login-div, .iframe-box").css({"height": "427px"});
        }
      }else {
        remoteCall.removejscssfile("./css/style_small.css", "css");
        if(responseHandler.iscaptchaShow) {
          $(".login-div, .iframe-box").css({"height": "645px"});
        }else {
          $(".login-div, .iframe-box").css({"height": "600px"});
        }
      }
    };
  });

  $(".login-div").addClass('fadeInLeft animated infinite');
  setTimeout(function() {
    remoteCall.removeClass();
  }, 1000);

})();
