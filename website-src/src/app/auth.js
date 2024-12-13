define([
  "artTemplate",
  "jquery",
  "context",
  "api",
  "encrypt",
  "util",
  "i18n",
  "log",
  "CimsBase64",
], function (template, $, context, api, encrypt, util, i18n, log, CimsBase64) {
  //加载国际化翻译
  i18n.execI18n();

  var _authHandler = {};
  var _authImplIdMapper = {};
  /**
   * Base64 编码加密
   */
  var Base64 = new CimsBase64();

  /**
   *
   * @param {object} action
   * @private
   */
  function AbstractAuthPage(action) {
    if (!action) {
      throw "action can not be null";
    }

    this.actionId = action.id;
    this.actionName = action.name;
    this.hasPageRender = true;
    this.code = action.code;
  }

  AbstractAuthPage.prototype.beforeRender = function () { };
  AbstractAuthPage.prototype.render = function (account) {
    return null;
  };
  AbstractAuthPage.prototype.getName = function () {
    if (this.name) {
      return this.name;
    } else {
      return this.actionName;
    }
  };
  AbstractAuthPage.prototype.afterRender = function (renderContext) { };
  AbstractAuthPage.prototype.renderCaptcha = function () { };
  AbstractAuthPage.prototype.onAuthFormShow = function () { };
  AbstractAuthPage.prototype.onApiError = function (
    errorCode,
    respBody,
    renderContext
  ) { };
  AbstractAuthPage.prototype.getFormDom = function () {
    var actionId = this.actionId;
    return _getAuthFormDomByActionId(actionId);
  };

  function AbstractNoPage(action) {
    AbstractAuthPage.call(this, action);
    this.hasPageRender = false;
  }

  $.extend(AbstractNoPage.prototype, AbstractAuthPage.prototype);
  AbstractNoPage.prototype.constructor = AbstractNoPage;
  AbstractNoPage.prototype.doNoAuthPageAuth = function () { };

  /**
   *
   * @param {string} code
   * @param {Object} config
   * @private
   */
  function _registerAuthPageHandler(code, config) {
    function AuthImpl(action) {
      AbstractAuthPage.call(this, action);
    }

    $.extend(AuthImpl.prototype, AbstractAuthPage.prototype, config);
    _authHandler[code] = AuthImpl;
  }

  function _registerNoAuthPageHandler(code, config) {
    function AuthImpl(action) {
      AbstractNoPage.call(this, action);
    }

    $.extend(AuthImpl.prototype, AbstractNoPage.prototype, config);
    _authHandler[code] = AuthImpl;
  }

  function getAuthHandlerByCode(code) {
    // log.warn(">=3.5.0版本，不再适用使用code组装认证")
    return _authHandler[code];
  }

  /**
   * @param {object|string} action
   */
  function getAuthHandler(action) {
    if (typeof action === "object") {
      if (_authImplIdMapper[action.id]) {
        return _authImplIdMapper[action.id];
      }

      var AuthImpl = getAuthHandlerByCode(action.code);
      if (!AuthImpl) {
        throw "没有【" + action.code + "】的实现方式";
      }

      _authImplIdMapper[action.id] = new AuthImpl(action);
      return _authImplIdMapper[action.id];
    } else if (typeof action === "string") {
      throw "action can not be string";
    }
  }

  function getAuthHandlerByActionId(actionId) {
    if (!_authImplIdMapper[actionId]) {
      throw "找不到已经初始化了的actionId, 请使用getAuthHandler 方法";
    }

    return _authImplIdMapper[actionId];
  }

  /**
   * 获取表单数据
   * @param {jQuery} formJqueryEntity 表单的jQuery实例；可为空，如果参数为空则通过 authCode参数获取实例
   * @returns {object} 表单数据
   * @private
   */
  function _getAuthFormDataByDom(formJqueryEntity) {
    if (!formJqueryEntity) {
      throw "formEntity can not be null";
    }

    var arrData = formJqueryEntity.serializeArray();
    var data = {};
    if (arrData.length === 0) {
      return data;
    }

    $.each(arrData, function (i, oneData) {
      data[oneData.name] =
        oneData.name === "account" ? $.trim(oneData.value) : oneData.value;
    });
    return data;
  }

  /**
   * 根据认证code获取认证form jQuery实例
   * @param authCode
   * @returns {*|jQuery|HTMLElement}
   * @deprecated 3.5.0 认证方式可以创建多个，本方法弃用
   * @private
   */
  function _getAuthFormDom(authCode) {
    return $(".form-auth[data-auth-code=" + authCode + "]");
  }

  /**
   * 根据actionId  获取认证form jQuery实例
   * @param actionId actionId, 也被称为 authId, authTypeId;
   * @returns {*|jQuery|HTMLElement}
   * @private
   */
  function _getAuthFormDomByActionId(actionId) {
    return $(".form-auth[data-auth-id=" + actionId + "]");
  }

  /**
   * 通用表单校验
   * @param {object} formData 表单的数据
   * @param {jQuery} $formDom 表单的jQuery对象
   * @param {array|string} [excludeField] 排除检测的字段；比如发动短信时，是不需要检测口令字段是否填写；
   * @private
   */
  function _validateFormData(formData, $formDom, excludeField) {
    var $inputDomList = $formDom.find("input:visible");
    var $passwordDomList = $formDom.find("input[type=password]");
    for (var i = 0; i < $passwordDomList.length; i++) {
      $inputDomList.push($passwordDomList[i]);
    }

    if (!excludeField) {
      excludeField = [];
    }

    if (typeof excludeField === "string") {
      excludeField = [excludeField];
    }

    for (var i = 0; i < $inputDomList.length; i++) {
      var $dom = $($inputDomList[i]);
      var name = $dom.attr("name");
      if ($.inArray(name, excludeField) >= 0) {
        continue;
      }

      if (name === "account") {
        if ($.trim(formData.account) === "") {
          if ($dom.attr("data-i18n-msg")) {
            util.warn($dom.attr("data-i18n-msg") || i18n.prop("resetPwd12"));
          } else {
            util.warn($dom.attr("placeholder") || i18n.prop("resetPwd12"));
          }

          return false;
        }
        if (!_checkFieldLength(formData.account, i18n.prop("login04"))) {
          return false;
        }
      } else if (name === "password") {
        if (!formData.password) {
          if ($dom.attr("data-i18n-msg")) {
            util.warn(
              $dom.attr("data-i18n-msg") || i18n.prop("appLoginway292")
            );
          } else {
            util.warn($dom.attr("placeholder") || i18n.prop("appLoginway292"));
          }
          return false;
        }
        if (!_checkFieldLength(formData.password, i18n.prop("index03"))) {
          return false;
        }
      } else if (name === "code") {
        if ($.trim(formData.code) === "") {
          util.warn($dom.attr("placeholder") || i18n.prop("login08"));
          return false;
        }
        if (!_checkFieldLength(formData.code, i18n.prop("index05"))) {
          return false;
        }
      } else if (name === "captcha") {
        if ($.trim(formData.captcha) === "") {
          if ($dom.attr("data-i18n-msg")) {
            util.warn($dom.attr("data-i18n-msg"));
          } else {
            util.warn($dom.attr("placeholder"));
          }
          return false;
        }

        if (!_checkFieldLength(formData.captcha, i18n.prop("index04"))) {
          return false;
        }
      } else if (name === "oldPassword") {
        if (!formData.oldPassword || formData.oldPassword === "") {
          util.warn($dom.attr("placeholder") || i18n.prop("authApp09"));
          return false;
        }
        if (!_checkFieldLength(formData.oldPassword, i18n.prop("resetPwd02"))) {
          return false;
        }
      } else if (name === "newPassword") {
        if (!formData.newPassword || formData.newPassword === "") {
          util.warn($dom.attr("placeholder") || i18n.prop("authApp10"));
          return false;
        }
        if (!_checkFieldLength(formData.newPassword, i18n.prop("resetPwd15"))) {
          return false;
        }
      } else if (name === "repeatPassword") {
        if (!formData.repeatPassword || formData.repeatPassword === "") {
          util.warn($dom.attr("placeholder") || i18n.prop("authApp11"));
          return false;
        }
      }
    }

    if (
      $.trim(formData.newPassword) !== "" &&
      formData.newPassword !== formData.repeatPassword
    ) {
      util.warn(i18n.prop("auth01"));
      return false;
    }

    return true;
  }

  /**
   * 检查字段长度并提示错误
   * @param value 值
   * @param fieldName 字段名
   * @return {boolean} 如果没有通过验证，则返回false
   * @private
   */
  function _checkFieldLength(value, fieldName) {
    if (value && value.length > 200) {
      util.warn(fieldName + i18n.prop("auth02"));
      return false;
    }
    return true;
  }
  function validateAgreement(isSIM) {
    const agreementChecked = $("#agreement i").hasClass("agreement-active");
    if (!agreementChecked) {
      if (isSIM) {
        util.warn("请勾选号卡认证服务协议、号卡认证隐私政策！");
      } else {
        util.warn("请勾选用户协议！");
      }
    }
    return agreementChecked;
  }

  /**
   * 类似 密码/临时授权码/动态密码 有用户名+口令的认证方式，通用表单提交处理
   * @param {string} actionId 认证方式
   * @returns {function(): boolean}
   * @private
   */
  function _getPwdFormSubmitFunctionByActionId(actionId) {
    return function () {
      var $formDom = _getAuthFormDomByActionId(actionId);
      var formData = _getAuthFormDataByDom($formDom);
      // 校验表单数据
      if (!_validateFormData(formData, $formDom)) {
        return false;
      }
      var a = getAuthHandlerByActionId(actionId);
      var code = getAuthHandlerByActionId(actionId).code;
      switch (code) {
        case "dynpwd": //动态口令埋点
          window.collectEvent("auth_dynamic_password_click_button_login", {
            button_name: "登录",
            user_unique_id: Base64.encode(formData.account),
          });
          break;
        case "staticpwd": //密码埋点
          window.collectEvent("auth_password_click_button_login", {
            button_name: "登录",
            user_unique_id: Base64.encode(formData.account),
          });
          break;
        case "tempauthcode": //临时授权码埋点
          window.collectEvent("auth_temporal_code_click_button_login", {
            button_name: "登录",
            user_unique_id: Base64.encode(formData.account),
          });
          break;
        default:
          break;
      }
      // if (!validateAgreement($formDom))
      //     return false;
      // }
      // 表单提交
      var preReq = {
        account: formData.account,
        actionId: actionId,
      };
      if (formData.captcha) {
        preReq.captcha = formData.captcha;
      }
      var submitBtn = $formDom.find("button[type=submit]");
      var btnCountdown = util.btnCountDown(submitBtn);
      api
        .preAuth(preReq)
        .done(function (resp) {
          var encryptData = resp.param;
          var encodePwd = encrypt
            .getEncoder(encryptData.alg)
            .encryptWithSalt(
              encryptData.publicKey,
              formData.password,
              encryptData.salt
            );

          var actionReq = $.extend({}, preReq, { param: encodePwd });
          api.action(actionReq).always(function (status, res, msg) {
            btnCountdown.reset();
            if (res.status == 1000) {
              switch (code) {
                case "dynpwd": //动态口令埋点
                  window.collectEvent("auth_dynamic_password_login_finish", {
                    is_success: true,
                    user_unique_id: Base64.encode(formData.account),
                  });
                  break;
                case "staticpwd": //密码埋点
                  window.collectEvent("auth_password_login_finish", {
                    is_success: true,
                    user_unique_id: Base64.encode(formData.account),
                  });
                  break;
                case "tempauthcode": //临时授权码埋点
                  window.collectEvent("auth_temporal_code_login_finish", {
                    is_success: true,
                    user_unique_id: Base64.encode(formData.account),
                  });
                  break;
                default:
                  break;
              }
            } else {
              switch (code) {
                case "dynpwd": //动态口令埋点
                  window.collectEvent("auth_dynamic_password_login_finish", {
                    is_success: false,
                    fail_reason: msg.msg,
                    user_unique_id: Base64.encode(formData.account),
                  });
                  break;
                case "staticpwd": //密码埋点
                  window.collectEvent("auth_password_login_finish", {
                    is_success: false,
                    fail_reason: msg.msg,
                    user_unique_id: Base64.encode(formData.account),
                  });
                  break;
                case "tempauthcode": //临时授权码埋点
                  window.collectEvent("auth_temporal_code_login_finish", {
                    is_success: false,
                    fail_reason: msg.msg,
                    user_unique_id: Base64.encode(formData.account),
                  });
                  break;
                default:
                  break;
              }
            }

          });
        })
        .fail(function (err, res, msg) {
          switch (code) {
            case "dynpwd": //动态口令埋点
              window.collectEvent("auth_dynamic_password_login_finish", {
                is_success: false,
                fail_reason: msg.msg,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            case "staticpwd": //密码埋点
              window.collectEvent("auth_password_login_finish", {
                is_success: false,
                fail_reason: msg.msg,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            case "tempauthcode": //临时授权码埋点
              window.collectEvent("auth_temporal_code_login_finish", {
                is_success: false,
                fail_reason: msg.msg,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            default:
              break;
          }
          btnCountdown.reset();
        });
      return false;
    };
  }

  /**
   * 类似 一键认证/人脸验证/声纹验证 用户名+发送认证的认证方式，通用表单提交处理
   * @param {string} actionId 认证方式ID
   * @param renderContext render
   * @param {number} second
   * @returns {function(): boolean}
   * @private
   */
  function _getPushFormSubmitFunctionByActionId(
    actionId,
    renderContext,
    second,
    isAgree
  ) {
    return function () {
      var $formDom = _getAuthFormDomByActionId(actionId);
      var formData = _getAuthFormDataByDom($formDom);

      // 校验表单数据
      if (!_validateFormData(formData, $formDom)) {
        return false;
      }

      var code = getAuthHandlerByActionId(actionId).code;
      switch (code) {
        case "simshield": //SIM盾认证按钮
          window.collectEvent("auth_sim_certificate_click_button_login", {
            button_name: "登录",
            user_unique_id: Base64.encode(formData.account),
          });
          break;
        case "sim": //SIM认证按钮埋点
          window.collectEvent("auth_sim_click_button_login", {
            button_name: "登录",
            user_unique_id: Base64.encode(formData.account),
          });
          break;
        default:
          break;
      }
      // 是否是二次认证
      if (context.getNextActions().length > 0) {
        // 显示勾选信息
        if (context.getChallenge().showProtocol) {
          // 是否勾选
          if (isAgree && !validateAgreement(isAgree)) {
            return false;
          }
        }
      } else {
        //不是二次认证
        if (context.getType(actionId)) {
          // 显示勾选信息
          if (context.getChallenge().showProtocol) {
            // 是否勾选
            if (isAgree && !validateAgreement(isAgree)) {
              return false;
            }
          }
        } else {
          // 是否勾选
          if (isAgree && !validateAgreement(isAgree)) {
            return false;
          }
        }
      }

      var $submitBtn = $formDom.find("button");
      var btnCountDown = util.btnCountDown($submitBtn, second);

      // 表单提交
      var preReq = {
        account: formData.account,
        actionId: actionId,
      };
      if (formData.captcha) {
        preReq.captcha = formData.captcha;
      }
      btnCountDown.start();
      api.preAuth(preReq).done(function () {
        api.registerListening().done(function () {
          switch (code) {
            case "simshield": //SIM盾认证按钮
              window.collectEvent("auth_sim_certificate_login_finish", {
                is_success: true,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            case "sim": //SIM认证按钮埋点
              window.collectEvent("auth_sim_click_button_login_finish", {
                is_success: true,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            default:
              break;
          }
        }).fail(function (res, body, msg) {
          btnCountDown.reset();
          switch (code) {
            case "simshield": //SIM盾认证按钮
              window.collectEvent("auth_sim_certificate_login_finish", {
                is_success: false,
                fail_reason: body.msg,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            case "sim": //SIM认证按钮埋点
              window.collectEvent("auth_sim_click_button_login_finish", {
                is_success: false,
                fail_reason: body.msg,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            default:
              break;
          }
        });
      }).fail(function (status, responseBody, msg) {
        switch (code) {
          case "simshield": //SIM盾认证按钮
            window.collectEvent("auth_sim_certificate_login_finish", {
              is_success: false,
              fail_reason: msg.msg,
              user_unique_id: Base64.encode(formData.account),
            });
            break;
          case "sim": //SIM认证按钮埋点
            window.collectEvent("auth_sim_click_button_login_finish", {
              is_success: false,
              fail_reason: msg.msg,
              user_unique_id: Base64.encode(formData.account),
            });
            break;
          default:
            break;
        }
        // 不支持sim快捷或者sim盾能力  则切换到短信验证
        if (status === 200040 || status === 200041) {
          btnCountDown.reset();
          var backAuthn = context.getCertification();
          if (
            backAuthn == "" ||
            backAuthn == null ||
            backAuthn == undefined
          ) {
            return false;
          }
          // 判断是否存在返回的认证方式
          var smsAction = context.getActions().find(function (action) {
            if (action.code === backAuthn) {
              return action;
            }
          });
          if (smsAction == undefined && context.getNextActions().length > 0) {
            smsAction = context.getNextActions().find(function (action) {
              if (action.code === backAuthn) {
                return action;
              }
            });
          }
          requirejs(["app/render"], function (render) {
            var actions = render.renderAuthFilter(context.getActions());
            var simPosition,
              simshieldPosition,
              smsauthPosition,
              homeActionCount;
            if (context.getNextActions().length > 0) {
              homeActionCount = 0;
            } else {
              homeActionCount = $(".main-auth-header").children(
                ".main-auth-header-tab"
              ).length;
            }
            //判断sim卡和sim盾位置
            for (var i = 0; i < actions.length; i++) {
              const ele = actions[i];
              if (ele.code === "sim") {
                simPosition = i;
              } else if (ele.code === "simshield") {
                simshieldPosition = i;
              } else if (ele.code === backAuthn) {
                smsauthPosition = i;
              }
            }
            // console.log(simPosition,'111',simshieldPosition,smsauthPosition);
            if (status === 200040) {
              // sim卡认证存在
              if (simPosition != -1) {
                if (simPosition < homeActionCount) {
                  //sim卡认证为主要认证方式
                  if (smsauthPosition != -1) {
                    // 短信认证在更多
                    if (smsauthPosition >= homeActionCount) {
                      // 短信认证在更多
                      formart(actions, "sim");
                    }
                  } else {
                    return false;
                  }
                } else {
                  //sim卡认证在更多
                  if (smsauthPosition != -1) {
                    //短信认证为主要认证方式
                    if (smsauthPosition < homeActionCount) {
                      // 短信认证在首页
                      $("#btn-nav-all-close").click();
                    }
                  } else {
                    return false;
                  }
                }
              }
            } else if (status === 200041) {
              // sim盾认证存在
              if (simshieldPosition != -1) {
                if (simshieldPosition < homeActionCount) {
                  //sim盾认证为主要认证方式
                  if (smsauthPosition != -1) {
                    // 回落认证存在
                    if (smsauthPosition >= homeActionCount) {
                      // 回落认证在更多
                      // console.log('ppp');
                      formart(actions, "simshield");
                    }
                  } else {
                    return false;
                  }
                } else {
                  //sim盾认证在更多
                  if (smsauthPosition != -1) {
                    if (smsauthPosition < homeActionCount) {
                      // 认证在首页
                      $("#btn-nav-all-close").click();
                    }
                  } else {
                    return false;
                  }
                }
              }
            }
            checkAuthn();
            function formart(actions, way) {
              // TODO 排序认证方式 将短信认证置顶；
              var arr = actions;
              for (var i = 0; i < arr.length; i++) {
                var ele = arr[i];
                if (ele.code == way) {
                  actions.splice(i, 1);
                  // actions.push(ele)
                }
                if (ele.code == backAuthn) {
                  actions.splice(i, 1);
                  actions.unshift(ele);
                }
              }
              // console.log(actions,'actions');
              // TODO 渲染头部认证切换样式
              var homeActionCount = $(".main-auth-header").children(
                ".main-auth-header-tab"
              ).length;
              var headAuthAction = actions.slice(0, homeActionCount);
              render.modelNormalHeader(headAuthAction);
              // TODO 修改loadMore里面nav导航的认证方式；
              if ($(".duo_auth_title").css("display") == "none") {
                if (actions.length > homeActionCount) {
                  var navActions = actions.slice(homeActionCount);
                  render.modelNavAllFoot(navActions);
                }
              }
              // $('#main-auth-header').find('div[data-auth-id='+ actionId+']').find('div').html(smsAction.name);
            }
            function checkAuthn() {
              // 切换认证方式；
              renderContext.changeAuthFormById(smsAction.id);
              var smsForm = _getAuthFormDomByActionId(smsAction.id);
              // 设置短信的账号 同时发送短信
              if (backAuthn == "dynpwd") {
                // 动态口令
                smsForm.find("#input-dynpwd-account").val(formData.account);
              } else if (backAuthn == "smsauth") {
                // 短信验证
                smsForm.find("#input-smsauth-account").val(formData.account);
              } else if (backAuthn == "staticpwd") {
                // 密码登录
                smsForm
                  .find("#input-staticpwd-account")
                  .val(formData.account);
              }
              // var btn = smsForm.find('button[type=button]');
              // util.btnCountDown(btn).reset();
              // btn.click();
            }
          });
        } else if (status === 200042) {
          if (responseBody && responseBody.length > 0) {
            if (responseBody[0]) {
              api.registerShieldListening().done(function (respBody) {
                btnCountDown.reset();
              });
            }
          }
        } else {
          btnCountDown.reset();
        }
      });
      return false;
    };
  }

  /**
   * 类似 短信/语音验证码 发送认证+ 用户名/口令 的认证方式，通用发送按钮处理
   * @param {string} actionId 认证方式
   * @returns {function(): boolean}
   * @private
   */
  /**
   * auth_sms_code_send_num 是累积发送验证码次数
   */
  var auth_sms_code_send_num = 0;
  var auth_message_code_send_num = 0;
  function _getPushCodeBtnClickFunctionByActionId(actionId) {
    return function () {
      var $formDom = _getAuthFormDomByActionId(actionId);
      var formData = _getAuthFormDataByDom($formDom);
      // 校验表单数据
      if (!_validateFormData(formData, $formDom, "code")) {
        return false;
      }

      var code = getAuthHandlerByActionId(actionId).code;

      switch (code) {
        case "smsauth": //发送验证码
          ++auth_sms_code_send_num;
          if (auth_sms_code_send_num <= 1) {
            window.collectEvent("auth_sms_code_send", {
              button_name: "发送短信",
              user_unique_id: Base64.encode(formData.account),
            });
          } else {
            window.collectEvent("auth_sms_code_send", {
              button_name: "重新发送",
              user_unique_id: Base64.encode(formData.account),
            });
          }
          break;
        case "notice":
          ++auth_message_code_send_num;
          if (auth_message_code_send_num <= 1) {
            window.collectEvent("auth_message_code_send", {
              button_name: "发送消息",
              user_unique_id: Base64.encode(formData.account),
            });
          } else {
            window.collectEvent("auth_message_code_send", {
              button_name: "重新发送",
              user_unique_id: Base64.encode(formData.account),
            });
          }
          break;
        default:
          break;
      }
      var btnCountDown = util.btnCountDown(this);

      var preAuthReq = { actionId: actionId, account: formData.account };
      if (formData.captcha) {
        preAuthReq.captcha = formData.captcha;
      }

      api
        .preAuth(preAuthReq)
        .done(function (respBody) {
          var code = respBody.action.code;
          switch (code) {
            case "smsauth": //短信验证码发送成功埋点
              window.collectEvent("auth_sms_code_verify_finish", {
                is_success: true,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            default:
              break;
          }
          btnCountDown.start();
          context.setAuthCache(actionId, respBody.param);
          if ($(".captcha_warp:visible").length > 0) {
            api.refreshCaptcha().done(function (respBody) {
              $(".captcha-img-code").attr("src", respBody.img);
              $(".captcha_warp input[name=captcha]").val("");
            });
          }
        })
        .fail(function (err, res, msg) {
          switch (code) {
            case "smsauth": //短信验证码发送成功埋点
              window.collectEvent("auth_sms_code_verify_finish", {
                is_success: false,
                user_unique_id: Base64.encode(formData.account),
                fail_reason: msg.msg,
              });
              break;
            default:
              break;
          }
          btnCountDown.reset();
        });
    };
  }

  /**
   * @param actionId
   * @returns {(function(): boolean)|*}
   * @private
   */
  function _getPushCodeFormSubmitFunctionByActionId(actionId) {
    return function () {
      var cache = context.getAuthCache(actionId);
      if ($.isEmptyObject(cache)) {
        util.warn(i18n.prop("resetPwd21"));
        return false;
      }

      var $formDom = _getAuthFormDomByActionId(actionId);
      var formData = _getAuthFormDataByDom($formDom);
      // 校验表单数据
      if (!_validateFormData(formData, $formDom)) {
        return false;
      }
      // if (!validateAgreement()) {
      //    return false;
      // }
      var encodePwd = encrypt
        .getEncoder(cache.alg)
        .encryptWithSalt(cache.publicKey, formData.code, cache.salt);

      var actionReq = {
        actionId: actionId,
        param: encodePwd,
        account: formData.account,
      };
      if (formData.captcha) {
        actionReq.captcha = formData.captcha;
      }

      var submitBtn = $formDom.find("button[type=submit]");
      var btnCountdown = util.btnCountDown(submitBtn);
      var code = getAuthHandlerByActionId(actionId).code;
      api
        .action(actionReq)
        .done(function (res) {
          switch (code) {
            case "smsauth":
              //短信登录方式成功埋点
              window.collectEvent("auth_sms_click_button_login", {
                is_success: true,
                user_unique_id: Base64.encode(formData.account),
              });
              break;
            default:
              break;
          }
        })
        .fail(function (status, body, msg) {
          switch (code) {
            case "smsauth":
              //短信登录方式失败埋点
              window.collectEvent("auth_sms_click_button_login", {
                is_success: false,
                user_unique_id: Base64.encode(formData.account),
                fail_reason: msg.msg,
              });
              break;
            default:
              break;
          }


        })
        .always(function (err, action, msg) {
          btnCountdown.reset();
        });
      return false;
    };
  }

  /*---------------------------------
      - 密码认证
      ---------------------------------*/
  _registerAuthPageHandler("staticpwd", {
    name: i18n.prop("appLoginway28"),
    iconCode: "icon-lock",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-staticpwd", {
        accountScope: context.getContext().accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-staticpwd");

      // iframe认证嵌入不显示忘记密码链接，详见BUG[38913]
      if (!$("body").hasClass("iframe")) {
        $("#href-forget-password")
          .show()
          .unbind("click")
          .bind("click", function () {
            window.collectEvent("auth_password_click_button_forget_password", {
              button_name: "忘记密码",
              // user_unique_id:
            });
            var params = window.location.href.split("?");
            params = params.length > 1 ? params[1] : "";
            $(location).attr("href", "./reset_pwd.html?" + params);
          });
      }

      // 注册表单提交事件
      var actionId = this.actionId;
      this.getFormDom()
        .unbind("submit")
        .bind("submit", _getPwdFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
      if (isMobile()) {
        $(".auth-content").css("height", "285px");
      }
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002 || errorCode === 9002) {
        util.warn(i18n.prop("render05"));
        return false;
      } else if (errorCode === 9402) {
        renderContext.renderPwdExpire();
        return false;
      }
    },
  });

  /*---------------------------------
      - 扫码认证，二维码
      ---------------------------------*/
  _registerAuthPageHandler("qrcode", {
    name: i18n.prop("appLoginway022"),
    iconCode: "icon-qrcode",
    render() {
      var actionId = this.actionId;
      return template("temp-qrcode", {
        appName: context.getContext().appName, actionId: actionId,
        unhappy: require('@/image/unhappy.png'), qrcodeFail: require('@/image/qrcode-fail.png'), fresh: require('@/image/fresh.png')
      });
    },
    onAuthFormShow() {
      //i18n渲染
      i18n.i18nById("form-qrcode");

      // 加载二维码
      var me = this,
        actionId = me.actionId;
      me._refreshQrcode(actionId);

      // 注册二维码刷新按钮点击事件
      $("#qrcode-mask")
        .unbind("click")
        .bind("click", function () {
          me._refreshQrcode(actionId);
        });
    },
    _refreshQrcode: function (actionId) {
      window.collectEvent("auth_scan_qr_load", {
        current_page: window.location.href,
      });
      api
        .preAuth({ actionId: actionId })
        .done(function (respBody) {
          $("#img-qrcode").attr("src", respBody.param.qrcodeUri);
          $("#qrcode-mask").hide();
          window.collectEvent("auth_scan_qr_load_result", {
            is_success: true
          });
          api
            .registerListening()
            // 过期之后，置灰二维码
            .fail(function (status, resp) {
              // 所有异步认证策略都完成，置灰二维码
              if (status === 9046 || status === 9045) {
                $("#qrcode-mask").show();
                window.collectEvent("auth_scan_qr_invalid", {
                  invalid_reason: resp.msg,
                });
              }
            });
        })
        .fail(function (errCode) {
          window.collectEvent("auth_scan_qr_load_result", {
            is_success: false,
            fail_reason: "网络或连接错误",
          });
          if (errCode === 9998) {
            $("#qrcode-mask").show();
          } else if (errCode === 92248) {
            $("#img-qrcode").hide();
            $("#qrcode-bg").show();
          }
        });
    },
  });

  /*---------------------------------
      - 中移互联网号扫码认证，二维码
      ---------------------------------*/
  _registerAuthPageHandler("cmicqrcode", {
    name: i18n.prop("appLoginway023"),
    iconCode: "icon-yijiansaomarenzheng",
    render: function () {
      var actionId = this.actionId;
      return template("temp-cmicqrcode", {
        appName: context.getContext().appName, actionId: actionId,
        unhappy: require('@/image/unhappy.png'), qrcodeFail: require('@/image/qrcode-fail.png'), loadFaid: require('@/image/load-faid.png'),
        fresh: require('@/image/fresh.png'),
      });
    },
    onAuthFormShow: function () {
      //i18n渲染
      i18n.i18nById("form-cmicqrcode");
      // 加载移动二维码
      var me = this,
        actionId = me.actionId;
      me._refreshCmicQrcode(actionId);

      // 注册二维码刷新按钮点击事件
      $("#cmicqrcode-mask")
        .unbind("click")
        .bind("click", function () {
          $("#cmicqrcode-mask").hide();
          $("#img-cmicqrcode").hide();
          $("#cmicqrcode-scan").hide();
          $(".loading").show();
          me._refreshCmicQrcode(actionId);
        });
    },
    _refreshCmicQrcode: function (actionId) {
      window.collectEvent("auth_one_click_qr_load", {
        current_page: window.location.href,
      });

      api
        .preAuth({ actionId: actionId })
        .done(function (respBody) {
          $("#img-cmicqrcode").attr(
            "src",
            "data:image/png;base64," + respBody.param.qrCodeBase64
          );
          $("#img-cmicqrcode").show();
          $("#cmicqrcode-mask").hide();
          $(".loading").hide();
          api
            .registerListening()
            // 过期之后，置灰二维码
            .fail(function (status, respBody) {
              // 所有异步认证策略都完成，置灰二维码
              if (
                status === 9046 ||
                status === 9045 ||
                status === 9205 ||
                status === 9999
              ) {
                $("#cmicqrcode-scan").hide();
                $("#cmicqrcode-mask").show();
                window.collectEvent("auth_one_click_qr_invalid", {
                  invalid_reason: respBody.msg,
                });
              }
            });
          window.collectEvent("auth_one_click_qr_load_result", {
            is_success: true,
          });
        })
        .fail(function (errCode) {
          $(".loading").hide();
          if (errCode === 92248) {
            $("#img-qrcode").hide();
            $("#cmicqrcode-bg").show();
          } else {
            $("#cmicqrcode-mask").show();
          }
          window.collectEvent("auth_one_click_qr_load_result", {
            is_success: false,
            fail_reason: "网络或连接错误"
          });
        });
    },
    onApiError: function (errorCode) {
      if (errorCode === 9206) {
        $("#cmicqrcode-scan").show();
        return false;
      }
    },
  });

  /*---------------------------------
      - 一键登录
      ---------------------------------*/
  _registerAuthPageHandler("akey", {
    name: i18n.prop("appLoginway042"),
    iconCode: "icon-one-click",
    render: function (account) {
      var contextData = context.getContext();
      var actionId = this.actionId;
      return template("temp-akey", {
        actionId: actionId,
        appName: contextData.appName,
        accountScope: contextData.accountScope,
        account: account,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-akey");

      // 注册表单提交事件
      var actionId = this.actionId;
      this.getFormDom()
        .unbind("submit")
        .bind("submit", _getPushFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
    },
  });

  /*---------------------------------
      - 邮件认证
      ---------------------------------*/
  _registerAuthPageHandler("email", {
    name: i18n.prop("email"),
    iconCode: "icon-email",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-email", {
        accountScope: context.getContext().accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-email");

      var $form = this.getFormDom();
      var actionId = this.actionId;
      // 注册发送按钮点击事件
      $form
        .find("button[type=button]")
        .unbind("click")
        .bind("click", _getPushCodeBtnClickFunctionByActionId(actionId));

      // 注册表单提交事件
      $form
        .unbind("submit")
        .bind("submit", _getPushCodeFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("email_auth_failed"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("v320"));
        return false;
      } else if (errorCode === 9043) {
        util.warn(i18n.prop("email_send_failed"));
        return false;
      }
    },
  });

  /*---------------------------------
      - 短信验证
      ---------------------------------*/
  _registerAuthPageHandler("smsauth", {
    name: i18n.prop("appLoginway14"),
    iconCode: "icon-sms",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-smsauth", {
        accountScope: context.getContext().accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-smsauth");

      var $form = this.getFormDom();
      var actionId = this.actionId;
      // 注册发送按钮点击事件
      $form
        .find("button[type=button]")
        .unbind("click")
        .bind("click", _getPushCodeBtnClickFunctionByActionId(actionId));

      // 注册表单提交事件
      $form
        .unbind("submit")
        .bind("submit", _getPushCodeFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("auth03"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("v320"));
        return false;
      } else if (errorCode === 9043) {
        util.warn(i18n.prop("auth04"));
        return false;
      }
    },
  });

  /*---------------------------------
      - 消息验证
      ---------------------------------*/
  _registerAuthPageHandler("notice", {
    name: i18n.prop("notice"),
    iconCode: "icon-notice",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-notice", {
        accountScope: context.getContext().accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-notice");

      var $form = this.getFormDom();
      var actionId = this.actionId;
      // 注册发送按钮点击事件
      $form
        .find("button[type=button]")
        .unbind("click")
        .bind("click", _getPushCodeBtnClickFunctionByActionId(actionId));

      // 注册表单提交事件
      $form
        .unbind("submit")
        .bind("submit", _getPushCodeFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("notice_auth_failed"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("v320"));
        return false;
      } else if (errorCode === 9043) {
        util.warn(i18n.prop("notice_send_failed"));
        return false;
      }
    },
  });

  /*---------------------------------
      - 动态令牌登录
      ---------------------------------*/
  _registerAuthPageHandler("dynpwd", {
    name: i18n.prop("appLoginway112"),
    iconCode: "icon-dynamic-code",
    render: function (account) {
      var contextData = context.getContext();
      var actionId = this.actionId;
      return template("temp-dynpwd", {
        appName:
          contextData.appName === "" || contextData.appName == null
            ? " "
            : contextData.appName,
        accountScope: contextData.accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-dynpwd");

      // 注册表单提交事件
      var actionId = this.actionId;
      this.getFormDom()
        .unbind("submit")
        .bind("submit", _getPwdFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("auth05"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("auth05"));
        return false;
      }
    },
  });

  /*---------------------------------
      - 语音验证
      ---------------------------------*/
  _registerAuthPageHandler("voiceauth", {
    name: i18n.prop("appLoginway172"),
    iconCode: "icon-phone",
    render: function (account) {
      var contextData = context.getContext();
      var actionId = this.actionId;
      return template("temp-voiceauth", {
        appName: contextData.appName,
        accountScope: contextData.accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-voiceauth");

      var $form = this.getFormDom();
      var actionId = this.actionId;
      // 注册发送按钮点击事件
      $form
        .find("button[type=button]")
        .unbind("click")
        .bind("click", _getPushCodeBtnClickFunctionByActionId(actionId));

      // 注册表单提交事件
      $form
        .unbind("submit")
        .bind("submit", _getPushCodeFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("auth06"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("v320"));
        return false;
      } else if (errorCode === 9043) {
        util.warn(i18n.prop("auth07"));
        return false;
      }
    },
  });

  /*---------------------------------
      - 临时授权码
      ---------------------------------*/
  _registerAuthPageHandler("tempauthcode", {
    name: i18n.prop("appLoginway202"),
    iconCode: "icon-auth-code",
    render: function (account) {
      var contextData = context.getContext();
      var actionId = this.actionId;
      return template("temp-tempauthcode", {
        accountScope: contextData.accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-tempauthcode");

      // 注册表单提交事件
      var actionId = this.actionId;
      this.getFormDom()
        .unbind("submit")
        .bind("submit", _getPwdFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("auth08"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("auth08"));
        return false;
      }
    },
  });

  /*---------------------------------
      - 人脸识别验证
      ---------------------------------*/
  _registerAuthPageHandler("faceauth", {
    name: i18n.prop("appLoginway222"),
    iconCode: "icon-face",
    render: function (account) {
      var contextData = context.getContext();
      var actionId = this.actionId;
      return template("temp-faceauth", {
        appName: contextData.appName,
        accountScope: contextData.accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-faceauth");

      // 注册表单提交事件
      var actionId = this.actionId;
      this.getFormDom()
        .unbind("submit")
        .bind("submit", _getPushFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
    },
  });

  /*---------------------------------
      - 声纹验证
      ---------------------------------*/
  _registerAuthPageHandler("voiceprintauth", {
    name: i18n.prop("auth092"),
    iconCode: "icon-voice",
    render: function (account) {
      var contextData = context.getContext();
      var actionId = this.actionId;
      return template("temp-voiceprintauth", {
        appName: contextData.appName,
        accountScope: contextData.accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-voiceprintauth");

      // 注册表单提交事件
      var actionId = this.actionId;
      this.getFormDom()
        .unbind("submit")
        .bind("submit", _getPushFormSubmitFunctionByActionId(actionId));
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
  });

  /*---------------------------------
      - 微信验证
      ---------------------------------*/
  _registerAuthPageHandler("wechat", {
    name: i18n.prop("appLoginway322"),
    iconCode: "icon-wechat",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-wechat", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-wechat");

      var qrCodeWindow = null;

      var actionId = this.actionId;
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            if (qrCodeWindow && !qrCodeWindow.closed) {
              return;
            }
            var url = respBody.param.href;
            var width = 600;
            var height = 548;
            var top = (window.screen.height - 30 - height) / 2;
            var left = (window.screen.width - 10 - width) / 2;
            qrCodeWindow = window.open(
              url,
              "_blank",
              "height=" +
              height +
              ",innerHeight=" +
              height +
              ",width=" +
              width +
              ",innerWidth=" +
              width +
              ",top=" +
              top +
              ",left=" +
              left +
              ",toolbar=no,menubar=no,scrollbars=auto,resizeable=no,location=no,status=no"
            );
            api.registerListening();
          });
        });
    },
  });

  /*---------------------------------
      - C端微信注册
      ---------------------------------*/
  _registerAuthPageHandler("wechat_register", {
    name: " ",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-wechat-register", {
        accountScope: context.getContext().accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      i18n.i18nById("form-wechat-register");

      $(".title-first").html(i18n.prop("wechat_register"));
      $(".title-second").html(i18n.prop("set_phone"));

      var $form = this.getFormDom();
      var actionId = this.actionId;

      // 显示图形验证码；
      api.refreshCaptcha().done(function (respBody) {
        $(".captcha-img-code").attr("src", respBody.img);
        $(".captcha_warp input[name=captcha]").val("");
      });

      $(".captcha-img-box")
        .unbind("click")
        .bind("click", function () {
          api.refreshCaptcha().done(function (respBody) {
            $(".captcha-img-code").attr("src", respBody.img);
            $(".captcha_warp input[name=captcha]").val("");
          });
        });

      // 注册发送按钮点击事件
      $form
        .find("button[type=button]")
        .unbind("click")
        .bind("click", function () {
          var $formDom = _getAuthFormDomByActionId(actionId);
          var formData = _getAuthFormDataByDom($formDom);
          // 校验表单数据
          if (!_validateFormData(formData, $formDom, "code")) {
            return false;
          }

          if (!new RegExp("^1\\d{10}$").test(formData["account"])) {
            util.warn("请输入正确手机号");
            return false;
          }
          var btnCountDown = util.btnCountDown(this);
          var preAuthReq = {
            actionId: actionId,
            param: formData.account,
            captcha: formData.captcha,
          };
          api
            .preAuth(preAuthReq)
            .done(function (respBody) {
              btnCountDown.start();
              context.setAuthCache(actionId, "code");
              if ($(".captcha_warp:visible").length > 0) {
                api.refreshCaptcha().done(function (respBody) {
                  $(".captcha-img-code").attr("src", respBody.img);
                  $(".captcha_warp input[name=captcha]").val("");
                });
              }
            })
            .fail(btnCountDown.reset);
        });

      // 注册表单提交事件
      $form.unbind("submit").bind("submit", function () {
        var cache = context.getAuthCache(actionId);
        if ($.isEmptyObject(cache)) {
          util.warn(i18n.prop("resetPwd21"));
          return false;
        }

        var $formDom = _getAuthFormDomByActionId(actionId);
        var formData = _getAuthFormDataByDom($formDom);
        // 校验表单数据
        if (!_validateFormData(formData, $formDom)) {
          return false;
        }
        var actionReq = {
          actionId: actionId,
          param: {
            code: formData.code,
            phone: formData.account,
            captcha: formData.captcha,
          },
        };

        var submitBtn = $formDom.find("button[type=submit]");
        var btnCountdown = util.btnCountDown(submitBtn);
        api.action(actionReq).always(btnCountdown.reset);
        return false;
      });
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("auth03"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("v320"));
        return false;
      } else if (errorCode === 9043) {
        util.warn(i18n.prop("auth04"));
        return false;
      }
    },
  });

  /*---------------------------------
     - C端完善注册信息
     ---------------------------------*/
  _registerAuthPageHandler("complete_information", {
    name: i18n.prop("complete_information"),
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-complete-information", {
        accountScope: context.getContext().accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      i18n.i18nById("form-complete-information");

      var $form = this.getFormDom();
      var actionId = this.actionId;
      $("#form-complete-information .form-auth label").css("display", "inline");
      var formSettings = [];
      requirejs(["layui", "xmSelect"], function (layui, xmSelect) {
        layui.use(["form"], function () {
          var form = layui.form;
          var multiData = [];
          var multiDom = {};
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            var attrs = respBody.param;

            var html = "";
            for (var i = 0; i < attrs.length; i++) {
              var ele = attrs[i];
              if (ele.key !== "phone") {
                formSettings.push(ele);
                switch (ele.inputModel) {
                  case 0:
                    if (ele.required) {
                      html +=
                        '<div class="input-warp margin-top-normal">' +
                        '<span class="label" title="' +
                        ele.name +
                        '" for="' +
                        ele.key +
                        '">' +
                        ele.name +
                        "</span>" +
                        '<input id="' +
                        ele.key +
                        '" type="text" name="' +
                        ele.key +
                        '" placeholder="请输入' +
                        ele.name +
                        '">' +
                        "</div>";
                    } else {
                      html +=
                        '<div class="input-warp margin-top-normal">' +
                        '<span class="label" title="' +
                        ele.name +
                        '" for="' +
                        ele.key +
                        '">' +
                        ele.name +
                        '<span class="additional">(选填)</span></span>' +
                        '<input id="' +
                        ele.key +
                        '" type="text" name="' +
                        ele.key +
                        '" placeholder="请输入' +
                        ele.name +
                        '">' +
                        "</div>";
                    }
                    break;
                  case 1:
                    if (ele.required) {
                      html +=
                        '<div class="input-warp margin-top-normal">' +
                        '<span class="label" title="' +
                        ele.name +
                        '" for="' +
                        ele.key +
                        '">' +
                        ele.name +
                        "</span>" +
                        '<input id="' +
                        ele.key +
                        '" type="number" name="' +
                        ele.key +
                        '" placeholder="请输入' +
                        ele.name +
                        '">' +
                        "</div>";
                    } else {
                      html +=
                        '<div class="input-warp margin-top-normal">' +
                        '<span class="label" title="' +
                        ele.name +
                        '" for="' +
                        ele.key +
                        '">' +
                        ele.name +
                        '<span class="additional">(选填)</span></span>' +
                        '<input id="' +
                        ele.key +
                        '" type="number" name="' +
                        ele.key +
                        '" placeholder="请输入' +
                        ele.name +
                        '">' +
                        "</div>";
                    }
                    break;
                  case 2:
                    var opt_html = "";
                    var multi_html = "";
                    var required_html = "";
                    for (var j = 0; j < ele.objectDataList.length; j++) {
                      var opt = ele.objectDataList[j];
                      opt_html +=
                        "<option value='" +
                        opt.keyProp +
                        "'class='more_text'" +
                        ">" +
                        opt.keyName +
                        "</option>";
                    }
                    if (ele.required) {
                      required_html = "";
                    } else {
                      required_html = '<span class="additional">(选填)</span>';
                    }
                    if (ele.multi) {
                      multi_html = '<div id="' + ele.key + '"></div>';
                      multiData.push(ele);
                    } else {
                      multi_html =
                        '<select id="' +
                        ele.key +
                        '" name="' +
                        ele.key +
                        '" class="register_input" lay-verify="required">' +
                        '<option class="more_text" data-i18n-text="register14" data-toggle="tooltip" data-placement="bottom" title=""></option>' +
                        opt_html +
                        "</select>";
                    }
                    html +=
                      '<div class="input-warp margin-top-normal">' +
                      '<div class="layui-form" action="">' +
                      '<div class="layui-form-item item">' +
                      '<span class="label" style="vertical-align: middle;">' +
                      ele.name +
                      required_html +
                      "</span>" +
                      '<div class="layui-input-inline">' +
                      multi_html +
                      "</div>" +
                      "</div>" +
                      "</div>" +
                      "</div>";
                    break;
                  default:
                    break;
                }
              }
            }
            $("#form-complete-login").before(html);
            form.render();

            $("dl > dd").each(function (i, n) {
              var text = $(this).text();
              $(n).attr("title", text);
              $(n).css({
                "text-align": "left",
                "font-size": "10px",
                "line-height": "32px",
              });
            });

            /* 渲染下拉多选 */
            for (var m = 0; m < multiData.length; m++) {
              var it = multiData[m];
              var el = "#" + it.key;
              var data = [];
              for (var o = 0; o < it.objectDataList.length; o++) {
                var ob = it.objectDataList[o];
                data.push({
                  name: ob.keyName,
                  value: ob.keyProp,
                });
              }
              multiDom[it.key] = xmSelect.render({
                el: el,
                name: it.key, //input框的name属性不修改默认为select
                theme: {
                  color: "#3c9aff",
                },
                height: "150px",
                data: data,
              });
            }

            form.on("select", function (data) {
              var text = data.elem[data.elem.selectedIndex].text;
              $(".layui-input-inline").attr("title", text);
            });
          });
        });
      });

      function _validateCompleteInformation(formData) {
        for (var index = 0; index < formSettings.length; index++) {
          var setting = formSettings[index];
          if (setting.required && !formData[setting.key]) {
            util.warn(setting.name + "必填！");
            return false;
          }
        }
        return true;
      }
      // 表单提交事件
      $form.unbind("submit").bind("submit", function () {
        var $formDom = _getAuthFormDomByActionId(actionId);
        var formData = _getAuthFormDataByDom($formDom);

        // 校验表单数据
        if (!_validateCompleteInformation(formData, $formDom)) {
          return false;
        }

        var actionReq = {
          actionId: actionId,
          param: formData,
        };

        var submitBtn = $formDom.find("button[type=submit]");
        var btnCountdown = util.btnCountDown(submitBtn);
        api.action(actionReq).always(btnCountdown.reset);
        return false;
      });
    },
    renderCaptcha: function () {
      var $form = this.getFormDom();
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
      // 调整间距
      $form
        .find(".margin-top-normal")
        .removeClass("margin-top-normal")
        .addClass("margin-top-small");
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 7002) {
        util.warn(i18n.prop("auth03"));
        return false;
      } else if (errorCode === 9002) {
        util.warn(i18n.prop("v320"));
        return false;
      }
    },
  });

  /*---------------------------------
      - 钉钉验证
      ---------------------------------*/
  _registerAuthPageHandler("dingtalk", {
    iconCode: "icon-dingding",
    render: function () {
      var actionId = this.actionId;
      return template("temp-dingtalk", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-dingtalk");

      var actionId = this.actionId;
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            window.open(respBody.param.href);
            api.registerListening();
          });
        });
    },
  });

  /*---------------------------------
      - AD域认证
      ---------------------------------*/
  _registerAuthPageHandler("ad", {
    name: i18n.prop("appLoginway38"),
    iconCode: "icon-AD",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-ad", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-ad");

      var actionId = this.actionId;
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            window.open(respBody.param.href);
            api.registerListening();
          });
        });
    },
  });

  /*---------------------------------
     - CAS认证
     ---------------------------------*/
  _registerAuthPageHandler("CAS", {
    iconCode: "icon-CAS",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-CAS", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-CAS");

      var actionId = this.actionId;
      // 点击进行认证
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          // if (validateAgreement()) {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            window.location.href = respBody.param.href;
          });
          // }
        });
    },
  });

  /*---------------------------------
     - OIDC认证
     ---------------------------------*/
  _registerAuthPageHandler("OIDC", {
    iconCode: "icon-OIDC",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-OIDC", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-OIDC");

      var actionId = this.actionId;
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          //   if (validateAgreement()) {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            window.location.href = respBody.param.href;
          });
          // }
        });
    },
  });

  /*---------------------------------
     - OAUTH认证
     ---------------------------------*/
  _registerAuthPageHandler("OAUTH", {
    iconCode: "icon-OAuth",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-OAUTH", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-OAUTH");

      var actionId = this.actionId;
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          //    if (validateAgreement()) {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            window.location.href = respBody.param.href;
          });
          // }
        });
    },
  });

  /*---------------------------------
     - JWT认证
     ---------------------------------*/
  _registerAuthPageHandler("JWT", {
    iconCode: "icon-JWT",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-JWT", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-JWT");

      var actionId = this.actionId;
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          //    if (validateAgreement()) {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            window.location.href = respBody.param.href;
          });
          // }
        });
    },
  });

  /*---------------------------------
     - SAML认证
     ---------------------------------*/
  _registerAuthPageHandler("SAML", {
    iconCode: "icon-SAML",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-SAML", { actionId: actionId });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("form-SAML");

      var actionId = this.actionId;
      this.getFormDom()
        .find("button")
        .unbind("click")
        .bind("click", function () {
          // if (validateAgreement()) {
          api.preAuth({ actionId: actionId }).done(function (respBody) {
            var formElement = document.createElement("form");
            formElement.action = respBody.param.formAction;
            formElement.method = respBody.param.method;

            var keyMap = {
              relayState: "RelayState",
              samlRequest: "SAMLRequest",
            };
            var body = respBody.param.body;
            for (var key in body) {
              if (!keyMap[key]) {
                continue;
              }
              var ele = document.createElement("input");
              ele.type = "hidden";
              ele.name = keyMap[key];
              ele.value = body[key];
              formElement.appendChild(ele);
            }

            document.body.appendChild(formElement);
            formElement.submit();
          });
          // }
        });
    },
  });

  /*---------------------------------
      - 企业微信（企微）扫码
      ---------------------------------*/
  _registerAuthPageHandler("workWechatQrcode", {
    name: i18n.prop("auth10"),
    iconCode: "icon-qiyeweixin",
    render: function (account) {
      return template("temp-workWechatQrcode", {});
    },
    onAuthFormShow: function () {
      //i18n渲染
      i18n.i18nById("form-workWechatQrcode");

      // 该认证页面加载后 就不用再次加载
      var actionId = this.actionId;
      var cache = context.getAuthCache("workWechatQrcode");
      if (cache && cache.isRendered) {
        return;
      }
      context.setAuthCache("workWechatQrcode", { isRendered: true });
      api.preAuth({ actionId: actionId }).done(function (respBody) {
        var wechatQrConf = respBody.param;
        context.setAuthCache(
          "workWechatQrcode",
          $.extend(wechatQrConf, { isRendered: true })
        );
        var url =
          wechatQrConf.baseUrl +
          "/wwopen/sso/qrConnect?" +
          "appid=" +
          wechatQrConf.appId +
          "&agentid=" +
          wechatQrConf.agentId +
          "&redirect_uri=" +
          wechatQrConf.redirectUri +
          "&login_type=jssdk" +
          "&href=" +
          wechatQrConf.redirectUri +
          "/authn/app/wx-qr-auth.css";
        var frame = document.getElementById("frame-workWechatQrcode");
        frame.src = url;
        frame.frameBorder = "0";
        frame.allowTransparency = "true";
        frame.scrolling = "no";
        frame.className = "wechatQrFrame";
        frame.width = "300px";
        frame.height = "400px";

        frame.onload = function () {
          if (frame.contentWindow.postMessage && window.addEventListener) {
            window.addEventListener("message", function (event) {
              if (
                event.origin.indexOf("feishu") > -1 ||
                event.data.indexOf("feishu") > -1
              ) {
                window.removeEventListener("message", workWechatQrcode);
              } else {
                api.action({ actionId: actionId, param: event.data });
              }
            });
            frame.contentWindow.postMessage("ask_usePostMessage", "*");
          }
        };
      });
    },
  });

  /*---------------------------------
      - 跨浏览器插件认证
      ---------------------------------*/
  _registerNoAuthPageHandler("crossbrowser", {
    beforeRender: function (initResp) {
      context.setContext("crossBrowserUrl", initResp.crossBrowserUrl);
      var actionId = this.actionId;
      api.crossBrowserEnv().done(function (devfp, isPluginLogged) {
        api
          .preAuth({
            actionId: actionId,
            param: {
              devId: devfp,
              needAction: isPluginLogged,
            },
          })
          .done(function (preAuthRespBody) {
            if (isPluginLogged) {
              api.registerListening();
              var localQuery = util.getQueryByLocalSearch();
              api.crossBrowserCallForAuth(
                preAuthRespBody.param.token,
                localQuery.randomKey
              );
            }
          });
      });
    },
  });

  /*---------------------------------
      - 企业微信（企微）H5 免密认证
      ---------------------------------*/
  _registerNoAuthPageHandler("wechatEnterpriseIdp", {
    beforeRender: function (initResp) {
      var count = 0;
      $.each(initResp.actions, function (i, action) {
        if (action.code === "wechatEnterpriseIdp") {
          count++;
        }
      });

      if (count === 1) {
        this.doNoAuthPageAuth();
      }
    },
    doNoAuthPageAuth: function () {
      var params = new URLSearchParams(window.location.search);
      var actionId = this.actionId;
      api.action({
        actionId: actionId,
        param: {
          code: params.get("code"),
        },
      });
    },
  });

  /*---------------------------------
      - 钉钉H5免密认证
      ---------------------------------*/
  _registerNoAuthPageHandler("dingtalkIdp", {
    beforeRender: function (initResp) {
      var count = 0;
      $.each(initResp.actions, function (i, action) {
        if (action.code === "dingtalkIdp") {
          count++;
        }
      });

      if (count === 1) {
        this.doNoAuthPageAuth();
      }
    },
    doNoAuthPageAuth: function () {
      var actionId = this.actionId;
      api.preAuth({ actionId: actionId }).done(function (respBody) {
        requirejs(["dingtalk"], function (dingtalkJsApi) {
          dingtalkJsApi.ready(function () {
            dingtalkJsApi.runtime.permission.requestAuthCode({
              corpId: respBody.param.corpId, // 企业id
              onSuccess: function (info) {
                // 登录IAM
                api.action({
                  actionId: actionId,
                  param: info.code,
                });
              },
              onFail: function (err) {
                alert(i18n.prop("auth11"));
              },
            });
          });
        });
      });
    },
  });

  /*---------------------------------
      - 移动办公H5免密认证
      ---------------------------------*/
  _registerNoAuthPageHandler("cmOaIdp", {
    beforeRender: function (initResp) {
      var count = 0;
      $.each(initResp.actions, function (i, action) {
        if (action.code === "cmOaIdp") {
          count++;
        }
      });

      if (count === 1) {
        this.doNoAuthPageAuth();
      }
    },
    doNoAuthPageAuth: function () {
      var params = new URLSearchParams(window.location.search);
      var actionId = this.actionId;
      api.action({
        actionId: actionId,
        param: {
          code: params.get("code"),
        },
      });
    },
  });

  /*---------------------------------
      - 飞书扫码
      ---------------------------------*/
  _registerAuthPageHandler("feiShuQrcode", {
    iconCode: "icon-feishu",
    render: function (account) {
      return template("temp-feiShuQrcode", {});
    },
    onAuthFormShow: function () {
      //i18n渲染
      i18n.i18nById("form-feiShuQrcode");
      // 该认证页面加载后 就不用再次加载
      var cache = context.getAuthCache("feishu");
      var actionId = this.actionId;
      if (cache && cache.isRendered) {
        return;
      }
      context.setAuthCache("feishu", { isRendered: true });
      api.preAuth({ actionId: actionId }).done(function (respBody) {
        var feiShuQrConf = respBody.param;
        context.setAuthCache(
          "feishu",
          $.extend(feiShuQrConf, { isRendered: true })
        );
        var fsUrl = respBody.param;
        var fscode = {
          id: "frame-feiShuQrcode",
          url: fsUrl,
          width: "250",
          height: "255",
          style: "border: 0;",
        };
        requirejs(["feishuqrcode"], function (feishucode) {
          var QRLoginObj = QRLogin(fscode);
          document.getElementById("frame-feiShuQrcode");
          var handleMessage = function (event) {
            var origin = event.origin;
            // 使用 matchOrigin 方法来判断 message 来自页面的url是否合法
            if (QRLoginObj.matchOrigin(origin)) {
              var loginTmpCode = event.data;
              var url = fsUrl + "&tmp_code=" + loginTmpCode;
              var feiShuFrame = document.getElementById("iframe-feiShuQrcode");
              feiShuFrame.src = url;
              feiShuFrame.frameBorder = "0";
              feiShuFrame.allowTransparency = "true";
              feiShuFrame.scrolling = "no";
              feiShuFrame.className = "feiShuQrFrame";
              feiShuFrame.width = "10px";
              feiShuFrame.height = "10px";
              feiShuFrame.onload = function () { };
              window.addEventListener("message", function (event) {
                api.action({ actionId: actionId, param: event.data });
              });
            }
          };
          if (typeof window.addEventListener != "undefined") {
            window.addEventListener("message", handleMessage, false);
          } else if (typeof window.attachEvent != "undefined") {
            window.attachEvent("onmessage", handleMessage);
          }
        });
      });
    },
  });

  /*---------------------------------
     - 飞书H5免密认证
     ---------------------------------*/
  _registerNoAuthPageHandler("feishuIdp", {
    beforeRender: function (initResp) {
      var count = 0;
      $.each(initResp.actions, function (i, action) {
        if (action.code === "feishuIdp") {
          count++;
        }
      });

      if (count === 1) {
        this.doNoAuthPageAuth();
      }
    },
    doNoAuthPageAuth: function () {
      var actionId = this.actionId;
      api.preAuth({ actionId: actionId }).done(function (respBody) {
        requirejs(["feishu"], function (feishu) {
          h5sdk.ready(function () {
            tt.requestAuthCode({
              appId: respBody.param,
              success: function (info) {
                api.action({ actionId: actionId, param: info.code });
              },
              fail: function (error) {
                util.warn(JSON.stringify(error));
              },
            });
          });
        });
      });
    },
  });

  /*---------------------------------
      - 强制修改密码
      ---------------------------------*/
  _registerAuthPageHandler("password_change", {
    name: " ",
    render: function (account) {
      var actionId = this.actionId;
      return template("temp-password-change", {
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function () {
      //i18n渲染
      i18n.i18nById("password_change");

      // 渲染页面
      var actionId = this.actionId;
      api.preAuth({ actionId: actionId }).done(function (respBody) {
        // password-change-title
        var respParam = respBody.param;
        context.setAuthCache("password_change", respParam);
        if (respParam.initPwd) {
          // 判断是初始密码
          $(".title-first").html(i18n.prop("auth12"));
          $(".title-second").html(i18n.prop("auth13"));
        } else if (respParam.weakPwd) {
          $(".title-first").html(i18n.prop("pwdIsWeak"));
          $(".title-second").html(i18n.prop("authApp08"));
        } else if (respParam.simplePwd) {
          $(".title-first").html(i18n.prop("pwdIsSimple"));
          $(".title-second").html(i18n.prop("authApp08"));
        } else if (respParam.modifyPwdRemainingDays > 0) {
          $(".title-first").html(
            i18n.prop("auth14") +
            " " +
            respParam.modifyPwdRemainingDays +
            " " +
            i18n.prop("auth15")
          );
          $(".title-second").html(i18n.prop("authApp08"));
        } else {
          $(".title-first").html(i18n.prop("auth16"));
          $(".title-second").html(i18n.prop("authApp08"));
        }

        // 判断是否显示暂不修改按钮；
        var isShowBtn = !(
          respBody.param.initPwd ||
          respBody.param.expirePwd ||
          respBody.param.weakPwd ||
          respBody.param.simplePwd
        );
        if (isShowBtn) {
          $("#href-skip")
            .show()
            .unbind("click")
            .bind("click", function () {
              var cache = context.getAuthCache("password_change");
              var account = context.getChallenge().account;
              var encodeSkipParam = encrypt
                .getEncoder(cache.alg)
                .encryptWithSalt(cache.publicKey, "", cache.salt);
              api.action({
                actionId: "password_change",
                account: account,
                param: encodeSkipParam,
              });
            });
        }
      });

      // 注册表单提交事件
      this.getFormDom()
        .unbind("submit")
        .bind("submit", function () {
          var $formDom = _getAuthFormDomByActionId(actionId);
          var formData = _getAuthFormDataByDom($formDom);
          // 校验表单数据
          if (!_validateFormData(formData, $formDom)) {
            return false;
          }

          var cache = context.getAuthCache("password_change");
          var reqRaw = {
            oldPassword: formData.oldPassword,
            newPassword: formData.newPassword,
          };
          var encodePwd = encrypt
            .getEncoder(cache.alg)
            .encryptWithSalt(
              cache.publicKey,
              JSON.stringify(reqRaw),
              cache.salt
            );
          var actionReq = $.extend(
            {},
            {
              actionId: actionId,
              account: formData.account,
              param: encodePwd,
            }
          );

          api.action(actionReq);
          return false;
        });
    },
    onApiError: function (errorCode, respBody, renderContext) {
      if (errorCode === 9065) {
        util.warn(respBody.msg, { icon: 1 });
        renderContext.allReset();
        return false;
      }
    },
  });

  /*---------------------------------
      - SIM卡认证
      ---------------------------------*/
  _registerAuthPageHandler("sim", {
    name: i18n.prop("appLoginway066"),
    iconCode: "icon-SIMrenzheng",
    render: function (account) {
      var actionId = this.actionId;
      var contextData = context.getContext();

      return template("temp-simauth", { appName: contextData.appName, accountScope: contextData.accountScope, account: account, actionId: actionId, loadFaid: require('@/image/load-faid.png') });
    },
    afterRender: function (renderContext) {
      //i18n渲染
      i18n.i18nById("form-simauth");
      var actionId = this.actionId;
      // 注册表单提交事件
      this.getFormDom()
        .unbind("submit")
        .bind(
          "submit",
          _getPushFormSubmitFunctionByActionId(
            actionId,
            renderContext,
            60,
            true
          )
        );
    },
    renderCaptcha: function () {
      var actionId = this.actionId;
      var $form = _getAuthFormDomByActionId(actionId);
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
    },
  });

  /*---------------------------------
      - SIM盾认证
      ---------------------------------*/
  _registerAuthPageHandler("simshield", {
    name: i18n.prop("appLoginway067"),
    iconCode: "icon-SIMdunrenzheng",
    render: function (account) {
      var actionId = this.actionId;
      var contextData = context.getContext();
      return template("temp-simshield", {
        appName: contextData.appName,
        accountScope: contextData.accountScope,
        account: account,
        actionId: actionId,
      });
    },
    afterRender: function (renderContext) {
      //i18n渲染
      i18n.i18nById("form-simshield");
      var actionId = this.actionId;
      // 注册表单提交事件
      this.getFormDom()
        .unbind("submit")
        .bind(
          "submit",
          _getPushFormSubmitFunctionByActionId(
            actionId,
            renderContext,
            120,
            true
          )
        );
    },
    renderCaptcha: function () {
      var actionId = this.actionId;
      var $form = _getAuthFormDomByActionId(actionId);
      // 显示图形验证码输入框
      $form.find(".captcha_warp").show();
    },
  });

  return {
    getAuthHandler: getAuthHandler,
    getAuthHandlerByActionId: getAuthHandlerByActionId,
  };
});
