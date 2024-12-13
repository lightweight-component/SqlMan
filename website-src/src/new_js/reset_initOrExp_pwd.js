
import '../css/normalize.css';
import '../css/mobile.css';
import '../css/font_login/iconfont.css';
import '../css/sso_form.css';
import '../lib/layer/layer.css';

import 'jquery';
import 'layer';
import { urlParams } from './my-common';
import '../js/i18n/jquery.i18n.properties.js';
import '../js/i18n/language.js';
import '../js/jquery-sortable.js';
import '../js/jquery.validate.js';
import '../js/jquery-validation-1.17.0/localization/messages_zh.min.js';
import '../js/jquery.validate.extend.js';
import '../js/base64Util.js';
import '../js/jsbn.js';
import '../js/sm-crypto.js';
import '../js/Cryptojs-3.1.2.js';
import '../js/cims_encryptor.js';
import '../js/jsencrypt.js';

$(document).ready(function () {
    var pwdObj = {}
    var queryString = window.location.search;
    var queryObj = {}
    if (queryString) {
        var params = queryString.split('?')[1];
        var pairs = params.split('&');
        for (var i = 0; i < pairs.length; i++) {
            var pair = pairs[i];
            var _key = pair.split('=')[0];
            var _value = pair.split('=')[1];
            queryObj[_key] = _value;
        }
    }
    if (queryObj.warningCount && queryObj.warningCount.length > 0) {
        $("#titleMsg").text($.i18n.prop('resetPwd05') + queryObj.warningCount + $.i18n.prop('resetPwd06'));
    } else {
        $("#titleMsg").text($.i18n.prop('resetPwd07'));
    }
    // 获取密码策略
    $.getJSON("api/verify/fetchUserPwdRule?userId=" + queryObj.userId, function (result) {
        if (result.ok) {
            $("#pwdRule").html(result.data.description);
            $("#newPwd").prop({
                alt: result.data.description,
                title: result.data.description
            });
            $("#confirmNewPwd").prop({
                alt: result.data.description,
                title: result.data.description
            });
            pwdObj = result.data;
        }
    });

    /*if (queryObj.phone){
        $('#init_mobile').val(queryObj.phone);
        $('#init_mobile').prop('readonly','true');
    }
    if (queryObj.email){
        $('#init_email').val(queryObj.email);
        $('#init_email').prop('readonly','true');
    }
    if (queryObj.wechat){
        $('#init_wechat').val(queryObj.wechat);
        $('#init_wechat').prop('readonly','true');
    }*/

    $("#initform").validate({
        rules: {
            oldPwd: {
                required: true
            },
            newPwd: {
                required: true
            },
            confirmNewPwd: {
                required: true,
                equalTo: "#newPwd"
            },
        },
        onkeyup: false,
        messages: {
            oldPwd: {
                required: $.i18n.prop('authApp09')
            },
            newPwd: {
                required: $.i18n.prop('authApp10')
            },
            confirmNewPwd: {
                required: $.i18n.prop('authApp11'),
                equalTo: $.i18n.prop('resetPwd08')
            }
        },
        invalidHandler: function (form, validator) {
            $.each(validator.invalid, function (key, value) {
                layer.msg(value, { time: 2500, shadeClose: true });
                return false;
            })
        },
        errorClass: 'from-check-invalid',
        // errorLabelContainer: $('#form-valid-error'),
        errorPlacement: function (error, element) {
            $("#form-valid-error").html(error.text());
        },
        submitHandler: function (form) {
            $.ajax({
                type: "post",
                url: "api/verify/getPublicKey",
                dataType: 'json',
                success: function (result) {
                    if (result.stat == "1000") {
                        //使用公钥加密
                        var publicKey = result.response.publicKey;
                        var encryptor = new CimsEncryptor(result.response.encryptor);
                        var sendData = {
                            oldPwd: encryptor.encrypt(publicKey, $("#oldPwd").val()),
                            newPwd: encryptor.encrypt(publicKey, $("#newPwd").val()),
                            token: result.response.token,
                            proof: queryObj.token,
                            userId: queryObj.userId
                        };
                        $.ajax({
                            type: "post",
                            url: "api/verify/modifyUserPwd",
                            data: sendData,
                            dataType: 'json',
                            success: function (result) {
                                if (result.code == 1000) {
                                    parent.layer.closeAll();
                                    layer.msg($.i18n.prop('resetPwd09'), { time: 2500, shadeClose: true });
                                } else if (result.code == 9999) {
                                    parent.layer.closeAll();
                                    layer.msg(result.msg, { time: 2500, shadeClose: true });
                                } else {
                                    layer.msg(result.msg, { time: 2500, shadeClose: true });
                                }
                            },
                            complete: function (result) {
                                $("#submitBtn").removeAttr("disabled");
                                $("#submitBtnTitle").text($.i18n.prop('authApp13'));
                            }
                        });
                    }
                }
            });
        }
    });

    /* $("#closeBtn").click(function () {


    });*/

    $("#submitBtn").click(function (e) {
        e.preventDefault();
        if ($("#initform").valid()) {
            $("#initform").submit();
            $("#submitBtnTitle").text($.i18n.prop('resetPwd10'));
            $("#submitBtn").attr("disabled", true);
        }
    });
});
