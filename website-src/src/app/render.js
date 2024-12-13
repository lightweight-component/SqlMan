define(["jquery", "auth", "log", "artTemplate", "api", "util", "context", "i18n"], function ($, auth, log, template, api, util, context, i18n) {
  var constant = {
    AUTH_CONTAINER_ID: "auth-container",
    AUTH_CONTENT_ID: "auth-content",
    MAIN_AUTH_HEADER_ID: "main-auth-header",
    AUTH_HEADER_ID: "auth-header",
    TEMP_MAIN_AUTH_HEADER: "temp-main-auth-header",
    TEMP_PASSWORD_EXPIRE: "temp-password-expired",
    AUTH_FOOT_NAV_CONTAINER_ID: "auth-footer-nav-container",
    AUTH_FOOT_NAV_TEMP_ID: "temp-nav-auth",
    AUTH_FOOT_CONTAINER_ID: "auth-footer",
    BTN_LOAD_MORE_WRAP_ID: "auth-foot-load-more-btn-wrap",
    BTN_LOAD_MORE_ID: "btnLoadMore",
    AUTH_HEADER_NAV_ALL_ID: "auth-header-nav-all",
    BTN_NAV_ALL_CLOSE_ID: "btn-nav-all-close",
    AUTH_FORM_MODEL_NORMAL: "normal",
    AUTH_FORM_MODEL_NAV_ALL: "nav_all",
    NAV_ALL_TITLE: "nav-all-title",
  };

  /**
   * 当前的认证方式
   */
  var currentActionId = null;
  /**
   * key：认证方式code, value：认证form表单的jquery对象
   * @type {object}
   */
  var authFormDom = {};
  var modelNormalAuthCodes = [];
  var modelNavAllAuthCodes = [];

  var $authContainer = $("#" + constant.AUTH_CONTAINER_ID);
  var $authContent = $("#" + constant.AUTH_CONTENT_ID);
  var $mainAuthHeader = $("#" + constant.MAIN_AUTH_HEADER_ID);
  var $authFootNavContainer = $("#" + constant.AUTH_FOOT_NAV_CONTAINER_ID);
  var $authFoot = $("#" + constant.AUTH_FOOT_CONTAINER_ID);
  var $btnLoadMore = $("#" + constant.BTN_LOAD_MORE_ID);
  var $btnLoadMoreWrap = $("#" + constant.BTN_LOAD_MORE_WRAP_ID);
  var $authHeaderNavAll = $("#" + constant.AUTH_HEADER_NAV_ALL_ID);
  var $navAllTitle = $("#" + constant.NAV_ALL_TITLE);
  var $btnNavAllClose = $("#" + constant.BTN_NAV_ALL_CLOSE_ID);
  var $passwordChangeTitle = $(".password-change-title");
  var $duoAuthTitle = $(".duo_auth_title");
  var $content = $("#content");
  var $foot = $("#footer");
  var $body = $("body");

  /**
   * 渲染主认证页面<>
   * @param authAction {array}
   */
  function modelNormalHeader(authAction) {
    modelNormalAuthCodes = authAction;

    var authHeaderDomList = $.map(authAction, function (action) {
      var authHandler = auth.getAuthHandler(action);
      if (!authHandler) {
        log.error("没有可以处理的认证方式：", action);
        return "";
      }
      return template(constant.TEMP_MAIN_AUTH_HEADER, {
        actionId: action.id,
        authCode: action.code,
        authName: authHandler.getName(),
      });
    }).filter(function (dom) {
      return dom !== "";
    });

    if (authHeaderDomList.length === 0) {
      log.info("渲染头部导航，没有可供渲染的认证方式");
      return;
    }

    authHeaderDomList.push('<div class="clearfix"></div>');
    $mainAuthHeader.html(authHeaderDomList.join(""));

    // 添加初始化样式
    if (authAction.length === 2) {
      $mainAuthHeader
        .children(".main-auth-header-tab:first")
        .addClass("active")
        .css("float", "left");
      $mainAuthHeader
        .children(".main-auth-header-tab:last")
        .css("float", "right");
    } else if (authAction.length === 1) {
      $mainAuthHeader
        .children(".main-auth-header-tab")
        .css({ margin: "auto" })
        .children("div")
        .css({ "border-bottom": 0 });
    } else if (authAction.length === 3) {
      $authContainer.addClass("customer-three-auth");
      $mainAuthHeader.children(".main-auth-header-tab").css("float", "left");
    }
    // 注册点击事件
    $mainAuthHeader
      .children(".main-auth-header-tab")
      .unbind("click")
      .bind("click", function () {
        var clickAuthId = $(this).data("auth-id");
        changeAuthFormById(clickAuthId);
      });

    return this;
  }

  /**
   * 渲染认证窗口脚部的导航栏
   * @param authActions
   */
  function modelNavAllFoot(authActions) {
    modelNavAllAuthCodes = authActions;

    if (authActions.length === 1) {
      $authFoot.hide();
      return;
    }

    var authNavList = $.map(authActions, function (action) {
      var authHandler = auth.getAuthHandler(action);
      if (!authHandler) {
        log.error("没有可以处理的认证方式：", action);
        return "";
      }
      if (!authHandler.render) {
        return "";
      }

      return template(constant.AUTH_FOOT_NAV_TEMP_ID, {
        actionId: action.id,
        action: action.code,
        iconCssCode: authHandler.iconCode,
        authName: authHandler.getName(),
      });
    }).filter(function (dom) {
      return dom !== "";
    });

    if (authNavList.length === 0) {
      log.info("渲染脚部导航，没有可供渲染的认证方式");
      return;
    }

    // 渲染页面
    authNavList.push('<div class="clearfix"></div>');
    $authFootNavContainer.html(authNavList.join(""));
    // var scrollBar = '<div class="scroll-wrapper">'+
    //                     '<div id="scroll-bar" class="scroll-bar"></div>'+
    //                 '</div>'
    // $authFootNavContainer.append(scrollBar)
    var marginClass =
      authActions.length <= 5
        ? "auth-foot-nav-margin-wide"
        : "auth-foot-nav-margin-slim";

    $authFootNavContainer
      .children(".auth-foot-nav")
      // 设置间距 按钮导航之间的间距
      .removeClass(["auth-foot-nav-margin-slim", "auth-foot-nav-margin-wide"])
      .addClass(marginClass)
      // 注册点击事件
      .unbind("click")
      .bind("click", function () {
        // 底部切换登录方式出发的事件
        var clickAuthId = $(this).data("auth-id");
        changeAuthFormById(clickAuthId);
      });

    $authFootNavContainer.find("p").each(function (i, dom) {
      var $el = $(dom);
      var txt = $el.html();
      if (txt.length === 5) {
        $el.css("margin-left", -8);
      } else if (txt.length > 5) {
        $el.css("margin-left", -14);
      }
    });
  }

  /**
   * 渲染认证方式form
   * @param {array} authType 认证方式列表
   * @param {string} [account] 用户账号，用在已经确定用户身份的场景；为空时自己填写账号
   */
  function authForm(authType, account) {
    if (!$.isArray(authType) || authType.length === 0) {
      log.error("渲染认证页面传入空数组");
      return;
    }

    _cleanAuthFormStatus();

    var authHandlerList = $.map(authType, function (authCode) {
      var authHandler = auth.getAuthHandler(authCode);
      if (!authHandler) {
        log.error("没有可以处理的认证方式：", authCode);
        return null;
      }
      return authHandler;
    }).filter(function (authHandler) {
      return authHandler && authHandler.hasPageRender;
    });

    var authFormDomList = $.map(authHandlerList, function (authHandler) {
      return authHandler.render(account);
    });

    var agreementHtml =
      '<div id="agreement" class="agreement margin-top-normal hide">' +
      '<i style = "vertical-align: middle;" onclick = "setAgreementRead()" class="iconfont icon-fuxuanmoren1 pointer" ></i>' +
      '<span style="vertical-align: middle;">' +
      '我已阅读并同意 <a href="https://wap.cmpassport.com/resources/html/numberCardContract.html">《号卡认证服务协议》</a><a href="https://wap.cmpassport.com/resources/html/numberCardPrivacy.html">《号卡认证隐私政策》</a>' +
      "</span>" +
      "</div>";

    $authContent.html(authFormDomList.join("") + agreementHtml);
    //i18n渲染
    var textEleAuth = $authContent.find("[data-i18n-text]");
    textEleAuth.each(function () {
      $(this).text(i18n.prop($(this).data("i18n-text")));
    });

    var insertEleAuth = $authContent.find(".i18n");
    insertEleAuth.each(function () {
      $(this).html(i18n.prop($(this).attr("name")));
    });

    $authContent.children(".form-auth").each(function (i, element) {
      var $el = $(element);
      var actionId = $el.data("auth-id");
      authFormDom[actionId] = $el;
    });

    $.each(authHandlerList, function (i, authHandler) {
      authHandler.afterRender(render);
    });

    // IE8 placeholder 适配;
    util.placeholderOfIE();
  }

  /**
   * 切换当前的认证方式显示
   * @param string actionId action
   */
  function changeAuthFormById(actionId) {
    if (actionId === currentActionId) {
      return;
    }

    var authHandler = auth.getAuthHandlerByActionId(actionId);
    //火山引擎代码埋点
    switch (authHandler.code) {
      case "qrcode":
        window.collectEvent("auth_scan_page_view", {
          page_title: "扫码认证",
        });
        break;
      case "dynpwd":
        window.collectEvent("auth_dynamic_password_page_view", {
          page_title: "动态口令",
        });
        break;
      case "smsauth":
        window.collectEvent("auth_sms_page_view", {
          page_title: "短信验证",
        });
        break;
      case "tempauthcode":
        window.collectEvent("auth_temporal_code_page_view", {
          page_title: "临时授权码",
        });
        break;
      case "staticpwd":
        window.collectEvent("auth_password_page_view", {
          page_title: "密码登录",
        });
        break;
      case "notice":
        window.collectEvent("auth_message_page_view", {
          page_title: "消息认证",
        });
        break;
      case "sim":
        window.collectEvent("auth_sim_page_view", {
          page_title: "sim卡认证",
        });
        break;
      case "cmicqrcode":
        window.collectEvent("auth_one_click_scan_page_view", {
          page_title: "一键扫码认证",
        });
        break;
      case "simshield":
        window.collectEvent("auth_sim_certificate_page_view", {
          page_title: "SIM盾证书",
        });
        break;
      default:
        break;
    }
    var showAgreeNames = ["SIM卡认证", "SIM盾证书认证"];
    // 是否是二次认证
    if (context.getNextActions().length > 0) {
      if (showAgreeNames.indexOf(authHandler.actionName) !== -1) {
        if (context.getChallenge().showProtocol) {
          $("#agreement").show();
        } else {
          $("#agreement").hide();
        }
      } else {
        $("#agreement").hide();
      }
    } else {
      if (context.getType(actionId)) {
        if (context.getChallenge().showProtocol) {
          $("#agreement").show();
        } else {
          $("#agreement").hide();
        }
      } else {
        if (showAgreeNames.indexOf(authHandler.actionName) !== -1) {
          $("#agreement").show();
        } else {
          $("#agreement").hide();
        }
      }
    }

    // 导航状态
    $(".active").removeClass("active");
    $("*[data-auth-id=" + actionId + "]").addClass("active");
    $("*[data-auth-actionCode=" + authHandler.code + "]").addClass("active");

    // 表单显示状态
    if (currentActionId !== null) {
      authFormDom[currentActionId].hide();
    }
    authFormDom[actionId].show();

    // navAll模式下 认证窗口头部标题的显示
    $navAllTitle.html(authHandler.getName());

    currentActionId = actionId;

    authHandler.onAuthFormShow();
  }

  /**
   * 清除所有选中状态
   * @private
   */
  function _cleanAuthFormStatus() {
    currentActionId = null;
    currentActionId = null;
    authFormDom = {};
  }

  /**
   * 渲染首页登录认证样式
   * @param {array} homeAuthAction 头部的一项或两项的认证方式
   * @param {array} loadMoreAuthCode 更多的认证方式
   */
  function renderHomeAuthFrom(homeAuthAction, loadMoreAuthCode) {
    modelNormalHeader(homeAuthAction);
    if (loadMoreAuthCode && loadMoreAuthCode.length > 0) {
      modelNavAllFoot(loadMoreAuthCode);
      loadMoreBtn();
    }

    if (loadMoreAuthCode.length == 0 && isMobile()) {
      // 手机端并且没有加载更多
      $(".auth-content").css("height", "285px");
    }

    var allRenderAuthActions =
      loadMoreAuthCode && loadMoreAuthCode.length > 0
        ? homeAuthAction.concat(loadMoreAuthCode)
        : homeAuthAction;
    authForm(allRenderAuthActions);
    _showModelNormal(loadMoreAuthCode && loadMoreAuthCode.length > 0);
    allPageWindowsResize();
  }

  function renderReAuthFrom(authCodes, account) {
    authForm(authCodes, account);
    modelNavAllFoot(authCodes);
    _showModelNavAll();
    allPageWindowsResize();
  }

  /**
   * 渲染下一步认证方式
   * @param {array} nextActions 下一步认证code列表
   * @param {string} account 当前认证的账号
   */
  function renderNextActionForm(nextActions, account) {
    nextActions = renderAuthFilter(nextActions);

    // 显示脚部
    $authFoot.show();

    // 隐藏加载更多按钮
    $btnLoadMoreWrap.hide();

    // 显示底部认证方式导航
    $authFootNavContainer.show();

    // 隐藏普通模式下的头部认证方式导航
    $mainAuthHeader.hide();

    // 显示头部认证样式
    $authHeaderNavAll.show();

    _cleanAuthFormStatus();
    context.resetAuthCache();

    modelNavAllFoot(nextActions);

    // 渲染认证方式
    if (nextActions && nextActions[0]) {
      authForm(nextActions, account);
      changeAuthFormById(nextActions[0].id);
    }

    resetAuthFormSize(constant.AUTH_FORM_MODEL_NAV_ALL);

    if (
      nextActions &&
      nextActions[0] &&
      ["password_change", "wechat_register"].includes(nextActions[0].code)
    ) {
      $duoAuthTitle.hide();
      $passwordChangeTitle.show();
      if (isMobile()) {
        $(".auth-content").css("height", "346px");
      }
    } else if (
      nextActions &&
      nextActions[0] &&
      nextActions[0].code === "complete_information"
    ) {
      $duoAuthTitle.hide();
      $passwordChangeTitle.hide();
    } else {
      if (nextActions && nextActions[0]) {
        var showAgreeNames = ["sim", "simshield"];
        if (showAgreeNames.indexOf(nextActions[0].code) !== -1) {
          if (context.getChallenge().showProtocol) {
            var delay = setTimeout(function () {
              $("#agreement").css("display", "block");
              clearTimeout(delay);
            }, 100);
          } else {
            var delayLoad = setTimeout(function () {
              $("#agreement").css("display", "none");
              clearTimeout(delayLoad);
            }, 100);
          }
          // 自动点击登录按钮
          if (context.getChallenge().autoTrigger) {
            var smsForm = $(
              ".form-auth[data-auth-id=" + nextActions[0].id + "]"
            );
            var btn = smsForm.find("button[type=submit]");
            // util.btnCountDown().reset();
            btn.click();
          }
        } else {
          var delayLoad = setTimeout(function () {
            $("#agreement").css("display", "none");
            clearTimeout(delayLoad);
          }, 100);
        }
        // 第一个认证方式是短信认证 自动发送验证码
        if (context.getChallenge().autoTrigger) {
          if (nextActions[0].code == "smsauth") {
            var smsForm = $(
              ".form-auth[data-auth-id=" + nextActions[0].id + "]"
            );
            var btn = smsForm.find("button[type=button]");
            // util.btnCountDown(btn).reset();
            btn.click();
          }
        }

        // // 通过showProtocol的值决定是否展示勾选信息
        // if (context.getChallenge().showProtocol) {
        //     var delay = setTimeout(function() {
        //         $("#agreement").css("display","block")
        //         clearTimeout(delay)
        //     }, 100);
        // } else {
        //     var delayLoad = setTimeout(function() {
        //         $("#agreement").css("display","none")
        //         clearTimeout(delayLoad)
        //     }, 100);
        // }
      }

      $duoAuthTitle.show();
      if (isMobile()) {
        $(".content").css("padding-top", "22%");
      }
      $passwordChangeTitle.hide();
    }

    // 注册 认证头部叉叉按钮点击事件
    $btnNavAllClose.show().unbind("click").bind("click", allReset);
    $("#auth-foot-customer-register-wrap").hide();
  }

  function allReset() {
    if (
      context.getChallenge() &&
      context.getChallenge().account &&
      context.getChallenge().account !== ""
    ) {
      location.reload();
    } else {
      $mainAuthHeader.hide();
      $authHeaderNavAll.hide();
      $authFoot.hide();
      $btnLoadMoreWrap.hide();
      $authFootNavContainer.hide();
      $passwordChangeTitle.hide();
      $duoAuthTitle.hide();
      if (isMobile()) {
        $(".content").css("padding-top", "8%");
        $(".auth-content").css("height", "263px");
      }
      context.resetAll();
      init();
    }
  }

  /**
   * 渲染加载更多按钮
   */
  function loadMoreBtn() {
    $authFoot.show();
    $btnLoadMoreWrap.show();
    $btnLoadMore.unbind("click").bind("click", function () {
      _showModelNavAll();
    });
  }

  /**
   * 渲染拒绝登录
   */
  function renderDeny() {
    $authFoot.hide();
    $btnLoadMoreWrap.hide();
    $authFootNavContainer.hide();
    $authHeaderNavAll.hide();
    $mainAuthHeader.hide();

    var denyDom = template("temp-challenge-deny");
    $authContent.html(denyDom);
    //i18n渲染
    var textEleAuth = $authContent.find("[data-i18n-text]");
    textEleAuth.each(function () {
      $(this).text(i18n.prop($(this).data("i18n-text")));
    });

    var insertEleAuth = $authContent.find(".i18n");
    insertEleAuth.each(function () {
      $(this).html(i18n.prop($(this).attr("name")));
    });
    allPageWindowsResize();
    resetAuthFormSize(constant.AUTH_FORM_MODEL_NORMAL);
  }

  /**
   * 渲染密码过期
   */
  function renderPwdExpire() {
    var dom = template(constant.TEMP_PASSWORD_EXPIRE);
    $authContent.html(dom);
    //i18n渲染
    var textEleAuth = $authContent.find("[data-i18n-text]");
    textEleAuth.each(function () {
      $(this).text(i18n.prop($(this).data("i18n-text")));
    });

    var insertEleAuth = $authContent.find(".i18n");
    insertEleAuth.each(function () {
      $(this).html(i18n.prop($(this).attr("name")));
    });

    // 注册 认证头部叉叉按钮点击事件
    $btnNavAllClose.show().unbind("click").bind("click", allReset);

    // 点击xx重置页面
    $("#btn-reset-password")
      .unbind("click")
      .bind("click", function () {
        var params = window.location.href.split("?");
        params = params.length > 1 ? params[1] : "";
        $(location).attr("href", "./reset_pwd.html?" + params);
      });

    resetAuthFormSize(constant.AUTH_FORM_MODEL_NORMAL);
    $authFoot.hide();
    $authHeaderNavAll.show();
    $btnNavAllClose.show();
    $mainAuthHeader.hide();
    $passwordChangeTitle.hide();
    $duoAuthTitle.hide();
  }

  window.renderPwdExpire = renderPwdExpire;

  /**
   * 显示头部导航 + 底部 "更多认证方式" 的样式；
   * @param {boolean} [showLoadMoreBtn] 传入false 不显示底部的加载更多按钮
   * @private
   */
  function _showModelNormal(showLoadMoreBtn) {
    // 判断是否显示脚部加载更多
    if (showLoadMoreBtn) {
      $(".scroll-wrapper").hide();
      $authFoot.show();
      $btnLoadMoreWrap.show();
    } else {
      $authFoot.hide();
      $btnLoadMoreWrap.hide();
    }

    $authFootNavContainer.hide();
    $authHeaderNavAll.hide();
    $mainAuthHeader.show();

    changeAuthFormById(modelNormalAuthCodes[0].id);
    resetAuthFormSize(constant.AUTH_FORM_MODEL_NORMAL);
  }

  /**
   * 显示底部认证方式导航切换样式
   * @private
   */
  function _showModelNavAll() {
    // 隐藏加载更多按钮
    $btnLoadMoreWrap.hide();
    $(".scroll-wrapper").show();
    setScrollBar();
    if ($authFootNavContainer.children("div").length !== 0) {
      // 显示脚部
      $authFoot.show();
      // 显示底部认证方式导航
      $authFootNavContainer.show();
    } else {
      $authFoot.hide();
    }

    // 隐藏普通模式下的头部认证方式导航
    $mainAuthHeader.hide();

    // 显示头部认证样式
    $authHeaderNavAll.show();

    // 关闭按钮显示/隐藏 和 点击事件
    if (modelNormalAuthCodes.length > 0) {
      $btnNavAllClose.show();
      var actions = renderAuthFilter(context.getActions());
      var homeActionCount = $(".main-auth-header").children(
        ".main-auth-header-tab"
      ).length;
      var headAuthAction = actions.slice(0, homeActionCount);
      modelNormalHeader(headAuthAction);
      modelNavAllFoot(actions.splice(headAuthAction.length));
      $btnNavAllClose.unbind("click").bind("click", _showModelNormal);
    } else {
      $btnNavAllClose.hide();
    }

    changeAuthFormById(modelNavAllAuthCodes[0].id);
    resetAuthFormSize(constant.AUTH_FORM_MODEL_NAV_ALL);
  }

  /*------------------------------------------------------
   *  认证窗口大小屏适配
   *------------------------------------------------------*/

  var normalSizeConstant = {
    // 认证窗口
    AUTH_MIN_WIDTH: 480,
    AUTH_HEIGHT: 630,

    // 认证窗口外层
    PAGE_MIN_HEIGHT: 820,

    NAV_ICON_WIDTH: 60,
    NAV_ICON_MARGIN_SLIM: 16,
    NAV_ICON_MARGIN: 40,
    NAV_ICON_BOTH_END_PADDING: 40,
  };

  var smallSizeConstant = {
    // 认证窗口
    AUTH_MIN_WIDTH: 342,
    AUTH_HEIGHT: 420,

    // 认证窗口外层
    PAGE_MIN_HEIGHT: 530,

    NAV_ICON_WIDTH: 38,
    NAV_ICON_MARGIN_SLIM: 10,
    NAV_ICON_MARGIN: 20,
    NAV_ICON_BOTH_END_PADDING: 20,
  };

  /**
   * 根据页面样式 设置认证窗口大小
   */
  var currentAuthModel;
  function resetAuthFormSize(model) {
    if (window.parent !== window) {
      // 认证被 iframe 嵌入的情况下 不适用
      $authContainer.width("100%");
      return;
    }
    var pxConstant = $body.hasClass("page-small")
      ? smallSizeConstant
      : normalSizeConstant;

    if (!model) {
      model = currentAuthModel;
    }

    if (model === constant.AUTH_FORM_MODEL_NORMAL) {
      $authContainer.animate({ width: pxConstant.AUTH_MIN_WIDTH }, 200);
      currentAuthModel = model;
    } else if (model === constant.AUTH_FORM_MODEL_NAV_ALL) {
      // 根据导航个数，计算nav_all模式下的宽度
      var navCount = modelNavAllAuthCodes.length;

      var length =
        (navCount - 1) *
          (navCount <= 5
            ? pxConstant.NAV_ICON_MARGIN
            : pxConstant.NAV_ICON_MARGIN_SLIM) +
        navCount * pxConstant.NAV_ICON_WIDTH +
        pxConstant.NAV_ICON_BOTH_END_PADDING * 2;
      if (length < pxConstant.AUTH_MIN_WIDTH) {
        length = pxConstant.AUTH_MIN_WIDTH;
      }

      $authContainer.animate({ width: length }, 200);
      currentAuthModel = model;
    }
  }

  function onWindowsResize() {
    var pageHeight = $(document).height();

    if (pageHeight < normalSizeConstant.PAGE_MIN_HEIGHT) {
      if (!$body.hasClass("page-small")) {
        $body.addClass("page-small");
        resetAuthFormSize();
      }
    } else {
      if ($body.hasClass("page-small")) {
        $body.removeClass("page-small");
        resetAuthFormSize();
      }
    }
  }

  function iframeWindowsResize() {
    var pageHeight = $(document).height();

    if (pageHeight <= smallSizeConstant.AUTH_HEIGHT) {
      if (!$body.hasClass("page-small")) {
        $body.addClass("page-small");
      }
    } else {
      if ($body.hasClass("page-small")) {
        $body.removeClass("page-small");
      }
    }
    if (modelNavAllAuthCodes.length > 4) {
      var authFootNavContainerWidth = modelNavAllAuthCodes.length * 100; //动态设置authFootNavContainer的宽度
      $authFootNavContainer.css("width", authFootNavContainerWidth + "px");
      $("#auth-footer-nav-container-next").removeClass("prohibit");
    } else {
      $("#auth-footer-nav-container-next").addClass("prohibit");
    }
    var Offset = -100;
    var i = 0;
    $("#auth-footer-nav-container-pre").on("click", function () {
      i -= 1;
      $authFootNavContainer.css("margin-left", i * Offset + "px");
      if (i > 0) {
        $("#auth-footer-nav-container-pre").removeClass("prohibit");
        if (i <= modelNavAllAuthCodes.length - 4) {
          $("#auth-footer-nav-container-next").addClass("prohibit");
        }
      } else {
        $("#auth-footer-nav-container-pre").addClass("prohibit");
        $("#auth-footer-nav-container-next").removeClass("prohibit");
      }
    });
    $("#auth-footer-nav-container-next").on("click", function () {
      i += 1;
      $authFootNavContainer.css("margin-left", i * Offset + "px");
      if (i > 0) {
        $("#auth-footer-nav-container-pre").removeClass("prohibit");
        if (i <= modelNavAllAuthCodes.length - 4) {
          $("#auth-footer-nav-container-next").addClass("prohibit");
        }
      }
    });
  }

  function allPageWindowsResize() {
    onWindowsResize();
    window.onresize = onWindowsResize;
    setInterval(onWindowsResize, 500);
  }

  /**
   * 通用异常处理
   */
  api.registerErrorCodeHandler(function (errorCode, respBody, actionId) {
    if (errorCode === 1000) {
      return;
    }

    if (
      $(".captcha_warp input[name=captcha]").is(":visible") &&
      (!respBody || !respBody.responseBody || !respBody.responseBody.img)
    ) {
      refreshCaptcha();
    }

    if (actionId && auth.getAuthHandlerByActionId(actionId)) {
      var continueDefaultHandler = auth
        .getAuthHandlerByActionId(actionId)
        .onApiError(errorCode, respBody, render);
      if (continueDefaultHandler === false) {
        return;
      }
    }

    switch (errorCode) {
      case 9998:
        util.warn(i18n.prop("render01"), {});
        break;
      case 9051:
        if (respBody.msg) {
          // "用户没有注册过声纹信息"
          util.warn(respBody.msg);
        } else {
          util.warn(
            i18n.prop("render02") +
              "“" +
              context.getContext().appName +
              "”app" +
              i18n.prop("render03")
          );
        }
        break;
      case 9065:
        util.warn(i18n.prop("render04"));
        allReset();
        break;
      case 7002:
        util.warn(i18n.prop("render05"));
        break;
      case 9020:
        util.warn(respBody.msg);
        break;
      case 9204:
        util.warn(i18n.prop("render06"));
        if (
          respBody &&
          respBody.responseBody &&
          respBody.responseBody.redirectUrl
        ) {
          window.location.href = respBody.responseBody.redirectUrl;
        }
        break;
      case 9066:
        window.location.href = respBody.responseBody.redirectUrl;
        break;
      case 9028:
        util.warn(i18n.prop("render07"));
        refreshCaptcha();
        break;
      case 90091:
        // 图形验证码
        util.warn(i18n.prop("abmLogin042"));
        renderImgCaptcha(respBody.responseBody.img);
        break;
      case 9064:
        // 禁止访问
        renderDeny();
        break;
      case 9045:
        util.warn(i18n.prop("render08"));
        break;
      case 9046:
        // do nothing; 所有的异步认证已完成
        break;
      case 9099:
        util.warn(i18n.prop("render09"));
        break;
      case 9100: // sim盾认证失败
        util.warn(respBody.statusMessage);
        break;
      case 10001: // sim盾认证确认
        util.info("SIM盾证书申请成功，请点击登录发起SIM盾证书认证。");
        break;
      default:
        util.warn(respBody.msg);
        break;
    }
  });

  api.registerNextActionHandler(renderNextActionForm);

  /*--------------------------------------------
      - 人机校验 - 图形验证码
      --------------------------------------------*/
  function renderImgCaptcha(base64Img) {
    $.each(authFormDom, function (authCode) {
      var authHandler = auth.getAuthHandlerByActionId(authCode);
      authHandler.renderCaptcha();
    });

    captchaImgReplace(base64Img);

    $(".captcha-img-box").unbind("click").bind("click", refreshCaptcha);
  }

  function captchaImgReplace(base64Img) {
    $(".captcha-img-code").attr("src", base64Img);
  }

  function refreshCaptcha() {
    api.refreshCaptcha().done(function (respBody) {
      captchaImgReplace(respBody.img);
      $(".captcha_warp input[name=captcha]").val("");
    });
  }

  /*--------------------------------------------
      - 页面初始化
      --------------------------------------------*/

  function _buildInitParam() {
    var localQuery = util.getQueryByLocalSearch();

    // 下游系统集成认证能力
    if (localQuery.sign) {
      var result = { sp: localQuery };
      result.sp.type = "AUTH_ISSUE";
      return result;
    }

    var param = {
      sp: {
        type: "IAM_TICKET",
        callbackUrl: localQuery.service,
      },
      challengeId: localQuery.challengeId,
    };

    if (localQuery.userCategory) {
      param.userCategory = localQuery.userCategory;
    }

    return param;
  }

  function init() {
    var initRequestData = _buildInitParam();
    api.init(initRequestData).done(function (resp) {
      window.collectEvent("config", {
        tenant_id: resp.tenantId,
        target_app_id: resp.targetAppId,
        target_app_name: resp.targetAppName,
      });
      // 预处理
      $.each(resp.actions, function (indexInArray, authCode) {
        var authHandler = auth.getAuthHandler(authCode);
        if (!authHandler) {
          log.error("没有可以处理的认证方式：", authCode);
          return;
        }
        authHandler.beforeRender(resp);
      });

      // 根据环境渲染页面框架
      getHandler(resp).frameRender(resp).renderAuth(resp);

      if (resp.captcha) {
        renderImgCaptcha(resp.captcha.img);
      }
      //加载国际化翻译
      i18n.execI18n();
    });
  }

  /**
   * 从action中 过滤出需要渲染页面的action
   * @param actions 可以
   * @return {array} 需要渲染的认证方式列表
   */
  function renderAuthFilter(actions) {
    return $.map(actions, function (action) {
      var authHandler = auth.getAuthHandler(action);
      if (!authHandler) {
        return null;
      }
      return authHandler.hasPageRender ? action : null;
    }).filter(function (action) {
      return action !== null;
    });
  }

  function renderChooseIdp(actions) {
    // 渲染选择页面
    var wrapHtml = template("temp-choose-idp");
    $authContent.html(wrapHtml);
    var itemHtml = $.map(actions, function (action) {
      return template("temp-choose-idp-item", action);
    });

    $("#choose-idp").html(itemHtml.join(""));
    $("#choose-idp li")
      .unbind("click")
      .bind("click", function () {
        var actionId = $(this).data("auth-id");
        var authHandler = auth.getAuthHandlerByActionId(actionId);
        authHandler.doNoAuthPageAuth();
      });
  }

  /*---------------------------------------------
     - 页面渲染
     ----------------------------------------------*/

  /*
   * 页面框架模板
   */
  var defaultFrameRender = {
    /**
     * 初始化时调用，检查是否适用于当期页面
     * @return {boolean}
     */
    isSupports: function (resp) {
      return false;
    },

    /**
     * 页面框架渲染
     * @param {object} initResp init响应内容
     */
    frameRender: function (initResp) {},

    renderAuth: function (resp) {},
  };

  var homePageFrameRender = $.extend({}, defaultFrameRender, {
    isSupports: function (resp) {
      return resp && resp.render && resp.render === "DEFAULT";
    },
    frameRender: function (initResp) {
      var page = $.extend(
        {},
        {
          systemName: i18n.prop("authApp01"),
          loginLogoImg: "image/logo.png",
          bannerImg: "image/login-bgimg.png",
          appName: i18n.prop("appLoginway27"),
        },
        initResp.page
      );
      var refreshOTP;
      // 设置标题名称
      $("#title").html(page.systemName);

      // 设置LOGO图片
      $("#imgLogo").attr("src", page.loginLogoImg);

      // 设置浏览器标签页title
      if (page) {
        $("title").html(page.systemName);
      }
      // 设置浏览器标签页ico
      var favicon = document.querySelector('link[rel="icon"]');
      if (page && page.loginIcoImg) {
        favicon.href = page.loginIcoImg;
      } else {
        favicon.href = "./favicon.ico";
      }

      // // 渲染背景图
      $("#content").css({
        "background-repeat": "no-repeat",
        "background-size": "cover",
        "background-position": "center",
        "background-image": "url('image/index/portal_bg.jpg')",
        filter:
          "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" +
          page.bannerImg +
          "', sizingMethod='scale')",
        "-ms-filter":
          "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" +
          page.bannerImg +
          "',sizingMethod='scale')",
      });
      $(".slogan").show();
      var urlParam = util.getQueryByLocalSearch();
      var isCustomer = urlParam.userCategory === "CUSTOMER";

      // 注册扫一扫绑定按钮点击事件
      var $windowsBindQr = $("#bind_windows");
      $("#btn-show-bind-windows")
        .unbind("click")
        .bind("click", function (e) {
          e.stopPropagation();
          $(".bind_windows").hide();
          $windowsBindQr.show();
          $(document)
            .unbind("click", _hideBindWindows)
            .bind("click", _hideBindWindows);
          var btnPosition = this.getBoundingClientRect();
          $windowsBindQr.css("top", btnPosition.y + btnPosition.height + 5);
          $windowsBindQr.css("left", btnPosition.x - 35);
        });
      function _hideBindWindows() {
        $(".bind_windows").hide();
        $(document).unbind("click", _hideBindWindows);
        if (refreshOTP) {
          clearInterval(refreshOTP);
        }
      }
      $(".bind_windows").click(function (e) {
        e.stopPropagation();
      });

      // 设置应用名称
      $(".app-name").html(page.appName);
      $(".header").show();

      if (!initResp.showScanBindingQR || isCustomer) {
        $("#btn-show-bind-windows").hide();
      }

      if (initResp.page.portalI18n) {
        var language = i18n.getCookie("userLanguage");
        if ("en" === language) {
          $("#language_E").addClass("un_active");
        } else {
          $("#language").addClass("un_active");
        }

        $("#btn-show-i18n").show();
      } else {
        $("#btn-show-i18n").hide();
      }

      if (!initResp.showAppDownloadBtn || isCustomer) {
        $("#btn-2fa-app-download").hide();
      } else {
        var $windowsAppDownload = $("#app_download_windows");
        $("#btn-2fa-app-download")
          .unbind("click")
          .bind("click", function (e) {
            e.stopPropagation();
            $(".bind_windows").hide();
            $windowsAppDownload.show();
            $(document)
              .unbind("click", _hideBindWindows)
              .bind("click", _hideBindWindows);
            var btnPosition = this.getBoundingClientRect();
            $windowsAppDownload.css(
              "top",
              btnPosition.y + btnPosition.height + 5
            );
            $windowsAppDownload.css("left", btnPosition.x - 35);
          });
      }

      // 显示右上角的按钮栏
      $(".header-btn-group").show();
      return this;
    },
    renderAuth: function (resp) {
      $("#customer_login").show();
      $("#customer_login").click(function () {
        var urlParam = util.getQueryByLocalSearch();
        urlParam.userCategory = "CUSTOMER";
        var params = $.map(urlParam, function (value, key) {
          return key + "=" + value;
        });
        $(location).attr("href", "./index.html?" + params.join("&"));
      });

      if (resp && resp.deny) {
        renderDeny();
        return;
      }

      if (!$.isArray(resp.actions)) {
        return;
      }

      // 检查是否需要渲染 选择IDP 页面；
      var wechatIdps = resp.actions.filter(function (action) {
        return action.code === "wechatEnterpriseIdp";
      });
      if (wechatIdps.length > 1) {
        var urlParam = util.getQueryByLocalSearch();
        if (urlParam["code"] && urlParam["state"]) {
          var state = urlParam["state"];
          for (var i = 0; i < wechatIdps.length; i++) {
            var idp = wechatIdps[i];
            if (idp.id === state) {
              var authHandler = auth.getAuthHandler(idp);
              authHandler.doNoAuthPageAuth();
              return;
            }
          }
        }
        renderChooseIdp(wechatIdps);
      }
      var cmOaIdps = resp.actions.filter(function (action) {
        return action.code === "cmOaIdp";
      });
      if (cmOaIdps.length > 1) {
        var urlParam = util.getQueryByLocalSearch();
        if (urlParam["code"] && urlParam["state"]) {
          var state = urlParam["state"];
          for (var i = 0; i < cmOaIdps.length; i++) {
            var idp = cmOaIdps[i];
            if (idp.id === state) {
              var authHandler = auth.getAuthHandler(idp);
              authHandler.doNoAuthPageAuth();
              return;
            }
          }
        }
        renderChooseIdp(cmOaIdps);
      }

      var feishuIdps = resp.actions.filter(function (action) {
        return action.code === "feishuIdp";
      });
      if (feishuIdps.length > 1) {
        renderChooseIdp(feishuIdps);
        return;
      }

      var dingtalkIdps = resp.actions.filter(function (action) {
        return action.code === "dingtalkIdp";
      });
      if (dingtalkIdps.length > 1) {
        renderChooseIdp(dingtalkIdps);
        return;
      }

      // 需要渲染的认证方式
      var allRenderAuthAction = renderAuthFilter(resp.actions);

      if (allRenderAuthAction.length === 0) {
        log.info("没有任何可以渲染的认证方式");
        return;
      }

      // 渲染认证方式
      var homeActionCount = resp.homeActionCount;
      renderHomeAuthFrom(
        allRenderAuthAction.slice(0, homeActionCount),
        allRenderAuthAction.length > homeActionCount
          ? allRenderAuthAction.slice(homeActionCount)
          : []
      );

      return this;
    },
  });

  var reAuthPageFrameRender = $.extend({}, defaultFrameRender, {
    /**
     * 初始化时调用，检查是否适用于当期页面
     * @param {object} resp init响应内容
     * @return {boolean}
     */
    isSupports: function (resp) {
      return resp && resp.render && resp.render === "RE_AUTH";
    },

    /**
     * 二次认证页面框架渲染
     * @param {object} resp init响应内容
     */
    frameRender: function (resp) {
      var page = resp.page;
      $authContent.addClass("center-block");

      $content.css({
        height: "100%",
      });

      $authContainer.css({
        left: "0",
        right: "0",
      });
      $body.css("background-color", "#C4E1FF");
      // 应用二次认证
      // if (navigator.userAgent.indexOf("yidongbangong") !== -1 ||
      //     navigator.userAgent.indexOf("DingTalk") !== -1
      // ) {
      //     $('.reAuthTitle').css({top:"-47px"})
      // }
      $("#reAuthTitle").show();

      if (isMobile()) {
        $(".content").css("padding-top", "22%");
      }
      if ($(".slogan")) {
        $(".slogan").hide();
      }
      // 设置浏览器标签页title
      if (page) {
        $("title").html(page.systemName);
      }
      // 设置浏览器标签页ico
      var favicon = document.querySelector('link[rel="icon"]');
      if (page && page.loginIcoImg) {
        favicon.href = page.loginIcoImg;
      } else {
        favicon.href = "./favicon.ico";
      }

      if (resp.actions && resp.actions.length > 0) {
        var showAgreeNames = ["sim", "simshield"];
        if (showAgreeNames.indexOf(resp.actions[0].code) !== -1) {
          if (context.getChallenge().showProtocol) {
            var delay = setTimeout(function () {
              $("#agreement").css("display", "block");
              clearTimeout(delay);
            }, 100);
          } else {
            var delayLoad = setTimeout(function () {
              $("#agreement").css("display", "none");
              clearTimeout(delayLoad);
            }, 100);
          }
          // 自动点击登录按钮
          if (context.getChallenge().autoTrigger) {
            var autoPushBtn = setTimeout(function () {
              var smsForm = $authContent.find(
                ".form-auth[data-auth-id=" + resp.actions[0].id + "]"
              );
              var btn = smsForm.find("button[type=submit]");
              // util.btnCountDown(btn).reset();
              btn.click();
              clearTimeout(autoPushBtn);
            }, 300);
          }
        } else {
          var delayLoad = setTimeout(function () {
            $("#agreement").css("display", "none");
            clearTimeout(delayLoad);
          }, 100);
        }
        // 第一个认证方式是短信认证 自动发送验证码
        if (context.getChallenge().autoTrigger) {
          if (resp.actions[0].code == "smsauth") {
            var autoPushBtn = setTimeout(function () {
              var smsForm = $authContent.find(
                ".form-auth[data-auth-id=" + resp.actions[0].id + "]"
              );
              var btn = smsForm.find("button[type=button]");
              // util.btnCountDown(btn).reset();
              btn.click();
              clearTimeout(autoPushBtn);
            }, 300);
          }
        }
      }

      return this;
    },

    /**
     * 渲染认证方式
     * @param {object} resp init响应内容
     */
    renderAuth: function (resp) {
      if (resp.deny) {
        renderDeny();
        return;
      }
      // 需要渲染的认证方式
      var allRenderAuthCode = renderAuthFilter(resp.actions);

      if (allRenderAuthCode.length === 0) {
        log.info("没有任何可以渲染的认证方式");
        return;
      }

      renderReAuthFrom(allRenderAuthCode, resp.account);

      return this;
    },
  });

  var iFrameAuthRender = $.extend({}, defaultFrameRender, {
    isSupports: function (resp) {
      return resp && resp.render && resp.render === "IFRAME";
    },
    /**
     * 页面框架渲染
     * @param {object} initResp init响应内容
     */
    frameRender: function (initResp) {
      $body.addClass("iframe");
      return this;
    },

    renderAuth: function (resp) {
      // 需要渲染的认证方式
      var allRenderAuthAction = renderAuthFilter(resp.actions);

      if (allRenderAuthAction.length === 0) {
        log.info("没有任何可以渲染的认证方式");
        return;
      }

      authForm(allRenderAuthAction, resp.account);
      modelNavAllFoot(allRenderAuthAction);
      _showModelNavAll();
      iframeWindowsResize();
      return this;
    },
  });

  var customerFrameRender = $.extend({}, homePageFrameRender, {
    isSupports: function (resp) {
      return resp && resp.render && resp.render === "CUSTOMER";
    },
    renderAuth: function (resp) {
      // 需要渲染的认证方式
      var allRenderAuthAction = renderAuthFilter(resp.actions);

      if (allRenderAuthAction.length === 0) {
        log.info("没有任何可以渲染的认证方式");
        return;
      }

      modelNormalHeader(allRenderAuthAction);
      authForm(allRenderAuthAction);

      $authFoot.show();
      $("#auth-foot-customer-register-wrap").show();
      $("#auth-foot-customer-register")
        .unbind("click")
        .bind("click", function () {
          var params = window.location.href.split("?");
          params = params.length > 1 ? params[1] : "";
          $(location).attr("href", "./customer_register.html?" + params);
        });

      $mainAuthHeader.show();

      changeAuthFormById(allRenderAuthAction[0].id);

      $("#btn-show-bind-windows").hide();

      $("#employee_login").show();
      $("#employee_login").click(function () {
        var urlParam = util.getQueryByLocalSearch();
        urlParam.userCategory = "EMPLOYEE";
        var params = $.map(urlParam, function (value, key) {
          return key + "=" + value;
        });
        $(location).attr("href", "./index.html?" + params.join("&"));
      });

      normalSizeConstant.AUTH_MIN_WIDTH = 582;

      return this;
    },
  });

  var ALL_HANDLER = [
    reAuthPageFrameRender,
    homePageFrameRender,
    iFrameAuthRender,
    customerFrameRender,
  ];
  function getHandler(resp) {
    for (var i = 0; i < ALL_HANDLER.length; i++) {
      var handler = ALL_HANDLER[i];
      if (handler.isSupports(resp)) {
        return handler;
      }
    }
  }

  // begin
  init();

  var render = {
    allReset: allReset,
    renderPwdExpire: renderPwdExpire,
    changeAuthFormById: changeAuthFormById,
    renderAuthFilter: renderAuthFilter,
    modelNormalHeader: modelNormalHeader,
    modelNavAllFoot: modelNavAllFoot,
  };
  return render;
});
