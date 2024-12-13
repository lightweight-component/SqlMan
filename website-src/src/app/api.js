define(["jquery", "context", "issue"], function ($, context, issue) {
  jQuery.support.cors = true;
  jQuery.ajaxSetup({
    xhr: function () {
      if (window.ActiveXObject) {
        return new window.ActiveXObject("Microsoft.XMLHTTP");
      } else {
        return new window.XMLHttpRequest();
      }
    },
  });

  /**
   * 初始化接口
   * @param requestBody requestBody
   * @returns {Promise}
   */
  function init(requestBody) {
    var result = $.Deferred();
    $.post({
      url: "api/idp/init",
      contentType: "application/json",
      data: JSON.stringify(requestBody),
      success: function (resp) {
        if (resp.status === 1000) {
          context.setContextFromInitResponseBody(resp);
          result.resolve(resp.responseBody);
        } else {
          result.reject(resp.status, resp.responseBody);
        }
        onError(resp.status, resp);
      },
      error: function (xhr) {
        result.reject(9998, xhr);
        onError(9998, xhr);
      },
    });
    return result.promise();
  }

  /**
     * 预认证请求
     * @param {object} param
     // * @param {string} param.actionId actionId code，action和 actionId 必须填一个，且action code 有多个时，必须使用actionId；
     * @param {string} param.actionId actionId id，action和 actionId 必须填一个，且action code 有多个时，必须使用actionId；
     * @param {string} [param.account] 用户输入的账号；
     * @param {string} [param.captcha] 人机校验验证码；
     * @returns {*|jQuery.Deferred}
     */
  function preAuth(param) {
    var result = $.Deferred();
    var challenge = context.getChallenge();
    var requestBody = $.extend({}, param, {
      challengeId: challenge.challengeId,
      contextRaw: challenge.contextRaw,
      actionId: param.actionId,
    });
    var actionId = requestBody.actionId;

    $.post({
      url: "api/idp/preAuth",
      contentType: "application/json",
      data: JSON.stringify(requestBody),
      success: function (resp) {
        context.setChallengeFromResponseBody(resp);
        if (resp.responseBody && resp.responseBody.nextActions) {
          renderNextAction(
            resp.responseBody.nextActions,
            resp.responseBody.account
          );
          return;
        }
        if (resp.status === 1000) {
          result.resolve(resp.responseBody);
        } else if (resp.status === 92248) {
          // 提示授权过期
          alertTheLicenseExpires(resp.msg);
          result.reject(resp.status, resp.responseBody);
        } else {
          result.reject(resp.status, resp.responseBody, resp);
        }
        onError(resp.status, resp, actionId);
      },
      error: function (xhr) {
        result.reject(9998, xhr);
        onError(9998, xhr, actionId);
      },
    });
    return result.promise();
  }
  // 提示授权过期
  function alertTheLicenseExpires(message) {
    $("#licenseExpiresContent").text(message);
    $("#licenseExpires").show();
  }
  function action(param) {
    var result = $.Deferred();
    var challenge = context.getChallenge();
    var requestBody = $.extend({}, param, {
      challengeId: challenge.challengeId,
      contextRaw: challenge.contextRaw,
    });
    var actionId = requestBody.actionId;

    $.post({
      url: "api/idp/action",
      contentType: "application/json",
      data: JSON.stringify(requestBody),
      success: function (resp) {
        context.setChallengeFromResponseBody(resp);
        if (resp.status === 1000) {
          var successResponseBody = resp.responseBody;
          result.resolve(successResponseBody,resp);
          // 处理认证成功
          if (successResponseBody.issue) {
            issuesLogin(successResponseBody.issue);
          }
        } else {
          result.reject(resp.status, resp.responseBody, resp);
        }
        onError(resp.status, resp, actionId);
        if (resp.responseBody && resp.responseBody.nextActions) {
          renderNextAction(
            resp.responseBody.nextActions,
            resp.responseBody.account
          );
        }
      },
      error: function (xhr) {
        result.reject(9998, xhr);
        onError(9998, xhr, actionId);
      },
    });
    return result.promise();
  }

  var currentListeningRaw;

  function listening(deferred) {
    var challenge = context.getChallenge();
    var requestBody = {
      challengeId: challenge.challengeId,
      contextRaw: challenge.contextRaw,
    };

    if (challenge.crossBrowserId) {
      requestBody.crossBrowserId = challenge.crossBrowserId;
    }

    currentListeningRaw = $.post({
      url: "api/idp/listening",
      contentType: "application/json",
      data: JSON.stringify(requestBody),
      success: function (resp) {
        if (context.getChallenge().challengeId !== requestBody.challengeId) {
          deferred.reject(9997);
          return;
        }

        context.setChallengeFromResponseBody(resp);
        var actionId =
          resp.responseBody &&
          resp.responseBody.action &&
          resp.responseBody.action.id;
        var actionCode =
          resp.responseBody &&
          resp.responseBody.action &&
          resp.responseBody.action.code;
        if (resp.status != 1000 && resp.status != 2007 && resp.status != 9206) {
          if (actionCode === "qrcode") {
            window.collectEvent("auth_scan_result", {
              is_success: false,
              fail_reason: resp.msg,
            });
          }
          if (actionCode === "cmicqrcode") {
            window.collectEvent("auth_one_click_scan_result", {
              is_success: false,
              fail_reason: resp.msg,
            });
          }
        }
        if (resp.status === 2007) {
          listening(deferred);
        } else if (resp.status === 7001) {
          onError(resp.status, resp, actionId);
          deferred.reject(resp.status, resp);
        } else if (resp.status === 1000) {
          if (actionCode === "qrcode") {
            window.collectEvent("beconEvent", "auth_scan_result", {
              is_success: true,
            });
          }
          if (actionCode === "cmicqrcode") {
            window.collectEvent("beconEvent", "auth_one_click_scan_result", {
              is_success: true,
            });
          }
          var successResponseBody = resp.responseBody;
          deferred.resolve(successResponseBody);

          // 处理认证成功
          if (successResponseBody.issue) {
            issuesLogin(successResponseBody.issue);
          }
        } else if (
          resp.status === 9046 ||
          resp.status === 9045 ||
          resp.status === 9999
        ) {
          deferred.reject(resp.status, resp);
          onError(resp.status, resp, actionId);
        } else if (resp.status === 9205) {
          deferred.reject(resp.status, resp);
        } else {
          onError(resp.status, resp, actionId);
          listening(deferred);
        }
        // 处理下一步认证
        if (resp.responseBody && resp.responseBody.nextActions) {
          renderNextAction(
            resp.responseBody.nextActions,
            resp.responseBody.account
          );
        }
      },
      error: function (xhr) {
        if (xhr.statusText === "abort") {
          return;
        }
        deferred.reject(9998, xhr);
        onError(9998, xhr);
      },
    });
  }

  /* 中移 sim盾认证失败轮询接口 */
  function shieldListening(deferred) {
    var transactionId = context.getTransactionId();
    $.post({
      url: "api/idp/shieldListening/" + transactionId,
      contentType: "application/json",
      success: function (resp) {
        if (resp.status === 2007) {
          shieldListening(deferred);
        } else if (resp.status === 1000) {
          var successResponseBody = resp.responseBody.status;
          deferred.resolve(successResponseBody);

          // 处理认证成功
          if (successResponseBody == "CONFIRMED") {
            // issuesLogin(successResponseBody);
            onError(10001);
          } else if (successResponseBody == "CANCELLED") {
            onError(9100, resp.responseBody);
          }
        }
      },
      error: function (xhr) {
        if (xhr.statusText === "abort") {
          return;
        }
        deferred.reject(9998, xhr);
        onError(9998, xhr);
      },
    });
  }

  var currentDeferred = {};

  function registerShieldListening() {
    // var challengeId = context.getChallenge().challengeId;
    // if (currentDeferred[challengeId]) {
    //     return currentDeferred[challengeId];
    // }
    var deferred = $.Deferred();
    shieldListening(deferred);
    // currentDeferred[challengeId] = deferred.promise();
    // deferred.always(function () {
    //     currentDeferred[challengeId] = null;ƒ
    // });
    return deferred;
  }

  function registerListening() {
    var challengeId = context.getChallenge().challengeId;
    if (currentDeferred[challengeId]) {
      return currentDeferred[challengeId];
    }
    var deferred = $.Deferred();
    listening(deferred);
    currentDeferred[challengeId] = deferred.promise();
    deferred.always(function () {
      currentDeferred[challengeId] = null;
    });
    return currentDeferred[challengeId];
  }

  function crossBrowserEnv() {
    var deferred = $.Deferred();
    $.ajax({
      type: "GET",
      url: context.getContext().crossBrowserUrl + "/sso/get_trusfort_user",
      cache: false,
      success: function (result) {
        if (result.status !== 0) {
          deferred.reject();
          return;
        }
        var respData = result.resp,
          devfp = respData.devfp,
          isPluginLogin = respData.login;

        context.setCrossBrowserId(devfp);

        deferred.resolve(devfp, isPluginLogin);
      },
      error: function (e) {
        deferred.reject();
      },
    });

    return deferred.promise();
  }

  /**
   * 调用浏览器插件接口，使之认证token
   */
  function crossBrowserCallForAuth(token, randomKey) {
    console.log("sso/token");

    var requestData = { token: token };
    if (randomKey) {
      requestData.randomKey = randomKey;
    }
    return $.ajax({
      type: "POST",
      url: context.getContext().crossBrowserUrl + "/sso/token",
      // 应蔡国辉要求 此处改成不正规的传参方式
      data: JSON.stringify(requestData),
      cache: false,
      dataType: "json",
    });
  }

  function refreshCaptcha() {
    var challenge = context.getChallenge();
    var result = $.Deferred();
    $.post({
      url: "api/idp/refreshCaptcha",
      contentType: "application/json",
      data: JSON.stringify({
        challengeId: challenge.challengeId,
        contextRaw: challenge.contextRaw,
      }),
      success: function (resp) {
        if (resp.status === 1000) {
          result.resolve(resp.responseBody);
        } else {
          result.reject(resp.status, resp.responseBody);
        }
      },
      error: function (xhr) {
        result.reject(9998, xhr);
        onError(9998, xhr);
      },
    });
    return result.promise();
  }

  /*---------------------------------
      - 事件处理
      ---------------------------------*/

  /**
   * 处理下一步认证
   * @param nextActions 下一步的认证方式
   * @param account 处理的账号
   */
  function renderNextAction(nextActions, account) {
    if (currentListeningRaw) {
      currentListeningRaw.abort();
      currentDeferred = {};
    }

    context.getChallenge().account = account;
    $.each(nextActionHandler, function (i, fun) {
      fun(nextActions, account);
    });

    var $agreement = $("#agreement");
    $agreement.hide();
  }

  /**
   * 处理登录
   * @param issueDataList 登录的处理列表
   */
  function issuesLogin(issueDataList) {
    $.each(issueDataList, function (i, issueData) {
      var issueFunction = issue.getByType(issueData.type);
      issueFunction(issueData);
    });
  }

  /**
   * @param {number} status 状态码
   * @param {object} respBody 响应
   * @param {string} [action] API调用的action
   */
  function onError(status, respBody, action) {
    $.each(errorCodeHandler, function (i, fun) {
      fun(status, respBody, action);
    });
  }

  var nextActionHandler = [];
  /**
   * 注册nextAction 事件
   * @param {function} callbackFunction
   * @param {array} callbackFunction.nextActions
   * @param {string} callbackFunction.account
   */
  function registerNextActionHandler(callbackFunction) {
    nextActionHandler.push(callbackFunction);
  }

  var errorCodeHandler = [];
  /**
   * 注册异常请求 全局处理事件
   * @param {function} callbackFunction
   * @param {number} callbackFunction.errorCode
   * @param {object} callbackFunction.respBody
   * @param {string} callbackFunction.action
   */
  function registerErrorCodeHandler(callbackFunction) {
    errorCodeHandler.push(callbackFunction);
  }

  return {
    init: init,
    preAuth: preAuth,
    action: action,
    crossBrowserEnv: crossBrowserEnv,
    crossBrowserCallForAuth: crossBrowserCallForAuth,
    registerListening: registerListening,
    registerNextActionHandler: registerNextActionHandler,
    registerErrorCodeHandler: registerErrorCodeHandler,
    refreshCaptcha: refreshCaptcha,
    registerShieldListening: registerShieldListening,
  };
});
