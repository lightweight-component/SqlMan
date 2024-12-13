function get_projectname() {
    var pathName = window.document.location.pathname;
    var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
    return projectName;
}

var config = {
    base_url: get_projectname(),
    api_base: get_projectname() + "/rest"
}


var request = {
    init: function () {
        location.hash.replace("#", "");
    },
    getUri: function () {
        if (location.hash.indexOf("?") != -1) {
            return location.hash.replace("#", "").split("?")[0];
        } else {
            return location.hash.replace("#", "");
        }
    },
    getParams: function () {
        if (location.hash.indexOf("?") != -1) {
            var parms = location.hash.split("?")[1];

            var array = new Array();
            $.each(parms.split("&"), function (i, n) {
                var name = n.split("=")[0];
                var val = n.split("=")[1];
                var param = {};
                param.name = name;
                param.value = val;
                array.push(param);
            });

            return array;
        }
        return null;
    },
    getParamValue: function (parmname) {
        if (location.hash.indexOf("?") != -1) {
            var parms = location.hash.split("?")[1];
            var value = null;
            $.each(parms.split("&"), function (i, n) {
                var name = n.split("=")[0];
                if (parmname == name) {
                    value = n.split("=")[1];
                }
            });

            return value;
        }
        return null;
    },
    getParam: function () {
        var params = this.getParams(),
            result = {};
        $.each(params, function (i, val) {
            result[val.name] = val.value;
        });
        return result;
    }
};


var cookie = {
    set: function (key, val, time) {
        var date = new Date();
        if (time != -1) {
            var expiresDays = time;
            date.setTime(date.getTime() + expiresDays * 24 * 3600 * 1000);
            document.cookie = key + "=" + val + ";expires=" + date.toGMTString();
        } else {
            document.cookie = key + "=" + val + ";expires=-1";
        }
    },
    get: function (key) {

        var getCookie = document.cookie.replace(/[ ]/g, "");
        var arrCookie = getCookie.split(";")
        var tips = null;
        for (var i = 0; i < arrCookie.length; i++) {
            var arr = arrCookie[i].split("=");
            if (key == arr[0]) {
                tips = arr[1];
                break;
            }
        }
        return tips;
    },
    del: function (key) {
        var date = new Date();
        date.setTime(date.getTime() - 10000);
        document.cookie = key + "=v; expires =" + date.toGMTString();
    }
};


var route = {
    get: function () {
        return cookie.get("rote");
    },
    set: function (path) {
        cookie.set("rote", path, -1);
    }
};


var param = {
    get: function () {
        return decodeURIComponent(cookie.get("param"));
    },
    set: function (parm) {
        cookie.set("param", encodeURIComponent(parm), -1);
    },
    del: function () {
        cookie.del("param");
    }
};

var permission = {
    have: function (operation) {
        if (permissions != null || typeof(permissions) != 'undefined') {
            if (permissions.indexOf(";:" + operation + ";") != -1) {
                return true;
            }
            return false;
        }
        return false;
    }
};


var nav = {
    show: function (key) {
        return navdata[key];
    },
    show_title: function (key) {

        var nav_info = navdata[key];

        if (nav_info != null && nav_info.indexOf("&gt;") != -1) {
            return nav_info.split("&gt;")[1];
        } else {
            return nav_info;
        }
    },
    show_nav: function (key) {
        var nav_info = navdata[key];

        if (nav_info != null && nav_info.indexOf("&gt;") != -1) {

            var nav_infos = nav_info.split("&gt;");
            var html = "";
            for (var i = 0; i < nav_infos.length; i++) {
                if (i == nav_infos.length - 1) {
                    html += "<li><span>" + nav_info.split("&gt;")[i] + "</span></li>";
                } else {
                    html += "<li><span>" + nav_info.split("&gt;")[i] + "</span><i class='icon-angle-right' style='font-size: 14px;'>&gt;</i></li>";
                }
            }
            return html;
        } else {
            return nav_info;
        }
    }
};


var DateFormat = {
    init: function () {
        Date.prototype.Format = function (fmt) { //author: meizz
            var o = {
                "M+": this.getMonth() + 1, //月份
                "d+": this.getDate(), //日
                "h+": this.getHours(), //小时
                "m+": this.getMinutes(), //分
                "s+": this.getSeconds(), //秒
                "q+": Math.floor((this.getMonth() + 3) / 3), //季度
                "S": this.getMilliseconds() //毫秒
            };
            if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
            for (var k in o)
                if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            return fmt;
        }
    },
    dateToString: function (time) {
        DateFormat.init();

        var time1 = "";
        if (time != null) {
            var date = new Date();
            date.setTime(time);

            time1 = date.Format("yyyy-MM-dd hh:mm:ss");
        }

        return time1;
    },
    dateToMinutes: function (time) {
        DateFormat.init();
        var time1 = "";
        if (time != null) {
            var module = "00";
            var s = Math.floor(time / 1000) % 60 + "";
            var m = Math.floor(time / 1000 / 60) % 60;
            var h = Math.floor(time / 1000 / 60 / 60);
            if (h) {
                time1 += h + "时";
            }
            if (h||m) {
                m = m + "";
                time1 += module.substr(m.length) + m + "分";
            }
            time1 += module.substr(s.length) + s + "秒";
        }

        return time1;
    }
}

var Tip = {
    show: function (content, z_index, speed) {

        var mydiv = document.getElementById("tip_" + z_index);
        if (mydiv != null) {
            mydiv.className = "top-alert";
        } else {
            mydiv = document.createElement("div");
            mydiv.setAttribute("id", "tip_" + z_index);
            mydiv.className = "top-alert";
            document.body.appendChild(mydiv);
        }

        mydiv.style.zIndex = z_index;
        mydiv.innerHTML = content;
        mydiv.style.display = "block";

        if (speed != -1) {
            setTimeout(function () {
                $("#tip_" + z_index).hide();
            }, speed);
        }
    },
    hide: function (z_index) {
        $("#tip_" + z_index).hide();
    },
    showloading: function () {
        Tip.show("<span id='flashMsg' class='top-alert-icon-doing fade-out'>加载中...</span>", 99999, -1);
    },
    hideloading: function () {
        $("#tip_99999").hide();
    },
    showprocessing: function () {
        Tip.show("<span id='flashMsg' class='top-alert-icon-doing fade-out'>处理中...</span>", 99999, -1);
    },
    hideprocessing: function () {
        $("#tip_99999").hide();
    },
    showinfo: function (content, timeout) {

        var z_index = 100000;

        if (timeout == null) {
            timeout = 3000;//设置超时时间为一秒
        }
        Tip.show("<span class='top-alert-icon-done fade-out'>" + content + "</span>", z_index, timeout);
        return z_index;
    },
    hideinfo: function (z_index) {
        if (z_index == null) {
            z_index = 10000;
        }
        $("#tip_" + z_index).hide();
    },
    show_error: function (content, timeout) {

        var z_index = 100000;

        if (timeout == null) {
            timeout = 3000;//设置超时时间为一秒
        }
        Tip.show("<span class='top-alert-icon-waring fade-out'>" + content + "</span>", z_index, timeout);
        return z_index;
    },
    hide_error: function (z_index) {
        if (z_index == null) {
            z_index = 100000;
        }
        $("#tip_" + z_index).hide();
    },
    show_progress_bar: function () {
        var z_index = 100000;
        Tip.show("<div class='ajax-uploadingOutline'><div class='ajax-uploadinginline'></div></div><span class='uploading-msg'>0%</span>", z_index, -1);
    },
    change_progress_bar: function (val) {
        var z_index = 100000;
        Tip.show("<div class='ajax-uploadingOutline' ><div class='ajax-uploadinginline' style='width:" + val + "'></div></div><span class='uploading-msg'>" + val + "</span>", z_index, -1);
    }

}


function show_login_dialog() {
    var url = config.base_url + "/templates/login.ejs";
    var html = new EJS({url: url}).render({});

    Dialog.open({"title": "登录", "content": html, "width": 500, "height": 204, "confirm_btn_name": "登录"});

    $('#loginForm').validate({
        errorPlacement: function (error, element) {
            error.appendTo(element.parent().next());
        },
        rules: {
            userName: {
                required: true
            },
            password: {
                required: true
            }
        },
        messages: {
            userName: {
                required: "请输入用户名"
            },
            password: {
                required: "请输入密码"
            }
        },
        submitHandler: function (form) {

            var userName = $("#userName").val();
            var password = $("#password").val();

            ajax.base({
                url: config.api_base + "/auth/getToken",
                data: {
                    "userName": userName
                },
                type: "POST",
                success: function (json) {
                    if (json != null) {
                        if (1 == json.status) {
                            if (json != null) {
                                if (json.status == 1) {
                                    //完成登录
                                    var code = encrypt(json.data.publicKey,json.data.token + "," + userName + "," + password);
                                    $.ajax({
                                        async: false,
                                        type: 'POST',
                                        dataType: 'json',
                                        url: config.api_base + "/auth/login",
                                        data: {
                                            "userName": userName,
                                            "tokens" : json.data.token,
                                            "code": code
                                        },
                                        success: function (json) {
                                            if (json != null) {
                                                if (json.status == 1) {
                                                    window.location.reload();
                                                } else if (json.status == 0) {
                                                    alert(json.msg);
                                                    $(".idaaslogin").removeAttr("disabled").val("登录");
                                                } else if (json.status == 2) {
                                                    alert(json.msg);
                                                    location.reload();
                                                }else if (json.status == -1) {
                                                    alert("系统异常，请联系系统管理员");
                                                    $(".idaaslogin").removeAttr("disabled").val("登录");
                                                }
                                            }
                                        },
                                        error: function (json) {
                                            console.log(json.msg);
                                            $(".idaaslogin").removeAttr("disabled").val("登录");

                                        },
                                        beforeSend: function (json) {
                                            $(".idaaslogin").attr("disabled", "disabled").val("登录中...");
                                        }
                                    });
                                } else if (json.status == 0) {
                                    alert("系统异常，请联系系统管理员");
                                    $(".idaaslogin").removeAttr("disabled").val("登录");
                                }
                            }
                        } else if (0 == json.status) {
                            if (json.msg != null) {
                                Tip.show_error(json.msg);
                            } else {
                                Tip.show_error("登录失败");
                            }
                        }
                    }
                }
            });
        }
    });
}

$(document).ajaxComplete(function(event,xhr,options){
    // 处理正常响应但无登录的情况
    if (xhr.status == 200){
        // 若返回结果不是json的可以获取responseText
        var data = xhr.responseJSON;
        // 判断是否是cas未登录，页面重定向
        if (data.status == 401 && data.redirectUrl){
            window.location = data.redirectUrl;
        }
    }
});

var ajax = {
    base: function (json) {
        $.ajax({
            type: json.type,
            url: json.url,
            contentType: json.contentType || "application/x-www-form-urlencoded",
            data: json.data,
            dataType: 'json',
            async: json.async || true,
            beforeSend: function () {
                //判断存不存在beforeSend函数，存在的话调用该函数，否则跳过
                if (typeof(json["beforeSend"]) !== 'undefined') {
                    json.beforeSend();
                }
            },
            success: function (data) {
                if (data != null && data.status != null) {
                    if (data.status == "401") {
                        Tip.hideloading();
                        show_login_dialog();
                        return false;
                    } else if (data.status == "403") {
                        Tip.hideloading();
                        Tip.show_error("权限受限");
                        return false;
                    } else if (data.status == "404") {
                        Tip.hideloading();
                        Tip.show_error("资源不存在");
                        return false;
                    } else if (data.status == "-1") {
                        Tip.hideloading();
                        Tip.show_error("系统异常,请联系管理员");
                        return false;
                    } else if (data.status == "-2") {
                        Tip.hideloading();
                        Tip.show_error("请求超时");
                        return false;
                    }
                }
                json.success(data);
                if ($.isFunction(json.onSuccess)) {
                    json.onSuccess(data);
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                //判断存不存在error函数，存在的话调用该函数，否则跳过
                if (typeof(json["error"]) !== 'undefined') {
                    json.error(jqXHR, textStatus, errorThrown);
                }
            },
            complete: function (data) {

                if (data.status == "401") {
                    Tip.hideloading();
                    show_login_dialog();
                    return false;
                } else if (data.status == "403") {
                    Tip.hideloading();
                    Tip.show_error("权限受限");
                    return false;
                } else if (data.status == "-1") {
                    Tip.hideloading();
                    Tip.show_error("系统异常,请联系管理员");
                    return false;
                } else if (data.status == "-2") {
                    Tip.hideloading();
                    Tip.show_error("请求超时");
                    return false;
                }
                //判断存不存在complete函数，存在的话调用该函数，否则跳过
                if (typeof(json["complete"]) !== 'undefined') {
                    json.complete(data);
                }
            }
        });
    },
    add: function (json) {
        ajax.base($.extend({
            type: "POST",
            url: json.url,
            data: json.data,
            contentType: json.contentType || "application/x-www-form-urlencoded",
            success: function (data) {
                if (data.status == null) {
                    Tip.hideprocessing();
                    Tip.show_error("接口返回格式错误，缺少状态码");
                    $("#btn_confim").removeAttr("disabled");
                }
                if (data.status == "1") {
                    Tip.hideprocessing();
                    $("#btn_confim").removeAttr("disabled");

                    var msg = "添加成功";
                    if (data.msg != null) {
                        msg = data.msg;
                    }
                    //弹出添加成功的提示层
                    var idx = Tip.showinfo(msg);
                    //等待1秒后关闭提示层，关闭添加页面，刷新数据列表
                    setTimeout(function () {
                        Dialog.close();
                        if (json.enableRefrushUrl !== false) {
                            change_rote({
                                roteUrl: json.roteUrl,
                                enableDelPageInfo: true
                            });
                        }
                        Tip.hideinfo(idx);
                    }, 1000);

                } else if (data.status == "0") {
                    Tip.hideprocessing();
                    $("#btn_confim").removeAttr("disabled");

                    var _msg="";
                    if (data.msg != null) {
                        var msg = data.msg;
                        if (msg.split(";").length > 1) {
                            for (var i = 0; i < msg.split(";").length; i++) {
                                _msg += msg.split(";")[i] + "<br/>";
                            }
                        } else {
                            _msg = msg;
                        }
                    }
                    _msg = _msg!="" ? _msg : '操作失败';
                    _msg = iamEscapeStr(_msg);
                    if (json.alertType && json.alertType == 'tip'){
                        Tip.show_error(_msg);
                    } else {
                        Dialog.open({"title": "操作失败", "content": _msg, "type": "alert"});
                    }

                    return false;
                }
            }
        }, json));
    },
    edit: function (json) {
        ajax.base($.extend({
            type: "POST",
            url: json.url,
            data: json.data,
            contentType: json.contentType || "application/x-www-form-urlencoded",
            beforeSend: function () {
                Tip.showprocessing();
            },
            success: function (data) {
                Tip.hideprocessing();
                if (data.status == null) {
                    Tip.show_error("接口返回格式错误，缺少状态码");
                    $("#btn_confim").removeAttr("disabled");

                } else if (data.status == "1") {
                    $("#btn_confim").removeAttr("disabled");

                    var msg = "修改成功";
                    if (data.msg != null) {
                        msg = data.msg;
                    }
                    //弹出添加成功的提示层
                    //共三个参数，只传第一个，二三默认就为null
                    var idx = Tip.showinfo(msg);
                    //等待1秒后关闭提示层，关闭添加页面，刷新数据列表
                    setTimeout(function () {
                        Dialog.close();
                        if (json.enableRefrushUrl !== false) {
                            change_rote({
                                roteUrl: false,
                                enableDelPageInfo: false
                            });
                        }

                        Tip.hideinfo(idx);
                    }, 1000);
                } else if (data.status == "0" || data.status == "2") {
                    $("#btn_confim").removeAttr("disabled");
                    var _msg;
                    if (data.msg != null) {
                        var msg = data.msg;
                        if (msg.split(";").length > 1) {
                            for (var i = 0; i < msg.split(";").length; i++) {
                                _msg += msg.split(";")[i] + "<br/>";
                            }
                        } else {
                            _msg = msg;
                        }
                    }
                    _msg = _msg ? _msg : '操作失败';
                    _msg = iamEscapeStr(_msg);
                     if (json.alertType && json.alertType == 'tip'){
                        Tip.show_error(_msg);
                    } else {
                        Dialog.open({"title": "操作失败", "content": _msg, "type": "alert"});
                    }

                    return false;
                } else if (data.status == "405") {
                    // 无操作权限
                    $("#btn_confim").removeAttr("disabled");

                    Dialog.open({"title": "操作失败", "content": "只能操作当前登录人角色的下属角色", "type": "alert"});
                }
            }
        }, json));
    },
    del: function (json) {
        var confirm_tip = "确定要删除吗？";
        if (json.confirm_tip != null) {
            confirm_tip = json.confirm_tip;
        }
        confirm("确定操作", confirm_tip, function () {
            ajax.base($.extend({
                type: "POST",
                url: json.url,
                data: json.data,
                beforeSend: function () {
                    Tip.showprocessing();
                },
                success: function (data) {
                    if (data.status == null) {
                        Tip.hideprocessing();
                        Tip.show_error("接口返回格式错误，缺少状态码");
                        $("#btn_confim").removeAttr("disabled");
                    }
                    if (data.status == "1") {
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");

                        var msg = "删除成功";
                        if (data.msg != null) {
                            msg = data.msg;
                        }
                        //弹出添加成功的提示层
                        var idx = Tip.showinfo(msg);
                        //等待1秒后关闭提示层，关闭添加页面，刷新数据列表
                        setTimeout(function () {

                            Dialog.close("alert");

                            if (json.enableRefrushUrl !== false) {
                                change_rote({
                                    roteUrl: json.roteUrl,
                                    enableDelPageInfo: true
                                });
                            }

                            Tip.hideinfo(idx);
                        }, 1000);

                    } else if (data.status == "0") {
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");
                        if (data.msg != null) {
                            var _msg = "";
                            var msg = data.msg;
                            if (msg.split(";").length > 1) {
                                for (var i = 0; i < msg.split(";").length; i++) {
                                    _msg += msg.split(";")[i] + "<br/>";
                                }
                            } else {
                                _msg = msg;
                            }
                            Dialog.open({"title": "操作失败", "content": _msg, "type": "alert"});
                        } else {
                            Dialog.open({"title": "操作失败", "content": "操作失败", "type": "alert"});
                        }
                        return false;
                    } else if (data.status == "405") {
                        // 无操作权限
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");

                        Dialog.open({"title": "操作失败", "content": "只能操作当前登录人角色的下属角色", "type": "alert"});
                    } else if (data.status == "406") {
                        // 删除警告，无法删除包含子角色的角色
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");
                        var url = config.base_url + "/templates/role/edit_warning.ejs";
                        var html = new EJS({url: url}).render();
                        var warning_win = {
                            "title": "提示信息",
                            "content": html,
                            "width": 460,
                            "height": 335,
                            "show_btn": false
                        }
                        Dialog.open(warning_win);
                    }
                }
            }, json));
        }, "确定");
    },

    delDev: function (json) {
        var confirm_tip = "确定要删除吗？";
        if (json.confirm_tip != null) {
            confirm_tip = json.confirm_tip;
        }
        confirmDel("确定操作", confirm_tip, function () {
            ajax.base($.extend({
                type: "POST",
                url: json.url,
                data: json.data,
                beforeSend: function () {
                    Tip.showprocessing();
                },
                success: function (data) {
                    if (data.status == null) {
                        Tip.hideprocessing();
                        Tip.show_error("接口返回格式错误，缺少状态码");
                        $("#btn_confim").removeAttr("disabled");
                    }
                    if (data.status == "1") {
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");

                        var msg = "删除成功";
                        if (data.msg != null) {
                            msg = data.msg;
                        }
                        Dialog.close("default");
                        //弹出添加成功的提示层
                        var idx = Tip.showinfo(msg);
                        //等待1秒后关闭提示层，关闭添加页面，刷新数据列表
                        setTimeout(function () {

                            Dialog.close("alert");

                            if (json.enableRefrushUrl !== false) {
                                change_rote({
                                    roteUrl: json.roteUrl,
                                    enableDelPageInfo: true
                                });
                            }

                            Tip.hideinfo(idx);
                        }, 1000);

                    } else if (data.status == "0") {
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");
                        if (data.msg != null) {
                            var _msg = "";
                            var msg = data.msg;
                            if (msg.split(";").length > 1) {
                                for (var i = 0; i < msg.split(";").length; i++) {
                                    _msg += msg.split(";")[i] + "<br/>";
                                }
                            } else {
                                _msg = msg;
                            }
                            Dialog.open({"title": "操作失败", "content": _msg, "type": "alert"});
                        } else {
                            Dialog.open({"title": "操作失败", "content": "操作失败", "type": "alert"});
                        }
                        return false;
                    } else if (data.status == "405") {
                        // 无操作权限
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");

                        Dialog.open({"title": "操作失败", "content": "只能操作当前登录人角色的下属角色", "type": "alert"});
                    } else if (data.status == "406") {
                        // 删除警告，无法删除包含子角色的角色
                        Tip.hideprocessing();
                        $("#btn_confim").removeAttr("disabled");
                        var url = config.base_url + "/templates/role/edit_warning.ejs";
                        var html = new EJS({url: url}).render();
                        var warning_win = {
                            "title": "提示信息",
                            "content": html,
                            "width": 460,
                            "height": 335,
                            "show_btn": false
                        }
                        Dialog.open(warning_win);
                    }
                }
            }, json));
        }, "确定");
    },

    upload: function (json) {
        $.ajax({
            type: 'POST',
            url: json.url,
            data: json.data,
            //async :false,
            contentType: false,
            processData: false,
            beforeSend: function () {
                //Tip.showprocessing();
                Tip.show_progress_bar();
            },
            success: function (data) {

                if ($.isFunction(json.onSuccess)) {
                    json.onSuccess(data);
                }

                if (data.status == null) {
                    Tip.hideprocessing();
                    Tip.show_error("接口返回格式错误，缺少状态码");
                    $("#btn_confim").removeAttr("disabled");
                }

                if (data.status == "401") {
                    Tip.hideloading();
                    show_login_dialog();
                    return false;
                } else if (data.status == "403") {
                    Tip.hideloading();
                    Tip.show_error("权限受限");
                    return false;
                } else if (data.status == "404") {
                    Tip.hideloading();
                    Tip.show_error("资源不存在");
                    return false;
                } else if (data.status == "-1") {
                    Tip.hideloading();
                    Tip.show_error("系统异常,请联系管理员");
                    return false;
                } else if (data.status == "-2") {
                    Tip.hideloading();
                    Tip.show_error("请求超时");
                    return false;
                }

                if (data.status == "1") {
                    Tip.hideprocessing();
                    $("#btn_confim").removeAttr("disabled");

                    var msg = "上传成功";
                    if (data.msg != null) {
                        msg = data.msg;
                    }
                    if(json.callback==null || json.callback==""){
                        //弹出添加成功的提示层
                        var idx = Tip.showinfo(msg);
                        //等待1秒后关闭提示层，关闭添加页面，刷新数据列表
                        setTimeout(function () {
                            if (json.enableRefrushUrl !== false) {
                                change_rote({
                                    roteUrl: json.roteUrl,
                                    enableDelPageInfo: true
                                });
                            }
                            Tip.hideinfo(idx);
                        }, 1000);
                    }

                    if (json.showid != null && data.resp_data != null) {
                        $("#" + json.showid).attr("value", data.resp_data.id);
                        $("#_" + json.showid).attr("src", config.api_base + "" + data.resp_data.url);
                        Dialog.close("layer");
                        if(json.callback!=null && json.callback!=""){
                            eval(json.callback+'('+JSON.stringify(json)+')');
                        }
                    }
                    if (data.resp_data != null && data.resp_data.baseId != null) {
                        $("#baseId").val(data.resp_data.baseId);
                        $("#btnbase").html("重新上传");
                        $("#baseName").html(data.resp_data.fileName)
                        $("#baseSize").html(data.resp_data.size + "M")
                    }

                } else if (data.status == "0") {
                    Tip.hideprocessing();
                    $("#btn_confim").removeAttr("disabled");

                    if (data.msg != null) {
                        Tip.show_error(data.msg);
                    } else {
                        Tip.show_error("上传失败");
                    }

                    return false;
                }
            }
            ,
            xhr: function () {        //这是关键  获取原生的xhr对象  做以前做的所有事情
                var xhr = $.ajaxSettings.xhr();

                xhr.upload.onprogress = function (ev) {
                    if (ev.lengthComputable) {
                        var percent = parseInt(100 * ev.loaded / ev.total);
                        Tip.change_progress_bar(percent + "%");
                    }

                };

                return xhr;
            }
        });
    },
    cancel: function (json) {
        ajax.base({
            type: json.type || "GET",
            url: json.url,
            beforeSend: function () {
                Tip.showloading("加载中...");
            },
            data: json.data,
            success: function (data) {
                Tip.hideloading();
                if (data.status == "0") {
                    if (data.msg != null) {
                        Tip.show_error(data.msg);
                    } else {
                        Tip.show_error("数据返回有误");
                    }
                    return false;
                }
                json.success(data.resp_data);
            }, error: function (jqXHR, textStatus, errorThrown) {
                //console.log(errorThrown);
                if (jqXHR.status == 404) {
                    Tip.show_error("接口不存在");
                    Tip.hideloading();
                }
            }
        });
    },


    show: function (json) {
        ajax.base({
            type: json.type || "POST",
            url: json.url,
            beforeSend: function () {
                Tip.showloading("加载中...");
            },
            data: json.data,
            success: function (data) {
                Tip.hideloading();
                if (data.status == "0") {
                    if (data.msg != null) {
                        Tip.show_error(data.msg);
                    } else {
                        Tip.show_error("数据返回有误");
                    }
                    return false;
                }
                json.success(data.resp_data);
            }, error: function (jqXHR, textStatus, errorThrown) {
                //console.log(errorThrown);
                if (jqXHR.status == 404) {
                    Tip.show_error("接口不存在");
                    Tip.hideloading();
                }
            }
        });
    }
};


//上传进度回调函数：
function progressHandlingFunction(e) {
    alert("------------");
    if (e.lengthComputable) {
        $('progress').attr({value: e.loaded, max: e.total}); //更新数据到进度条
        var percent = e.loaded / e.total * 100;
        $('#progress').html(e.loaded + "/" + e.total + " bytes. " + percent.toFixed(2) + "%");
    }
}


var paging = {
    htmlWithAction: function (total, cur, size, funName) {
        var html = this.showhtml(total, cur, size);
        return html.replace(/onclick="go\(/ig, "onclick=\"" + funName + "(");
    },
    showhtml: function (total, cur, size, nu) {
        var orde = 1;
        if (size > 0)
            var page_size = size;
        else
            var page_size = 10;
        if (nu > 1) {
            orde = nu;
        }
        var page_total = total % page_size != 0 ? parseInt(total / page_size) + 1 : parseInt(total / page_size);

        if (page_total > 7) {
            var html = "<div class='zxf_pagediv'>";

            if (cur == 1) {
                html += "<a class='prebfirst disabled'>首页</a>";
                html += "<a class='prebtn disabled'>上一页</a>";
            } else {
                html += "<a class='prebfirst' onclick=\"go('1','" + orde + "')\">首页</a>";
                html += "<a class='prebtn' onclick=\"go('" + (cur - 1) + "','" + orde + "')\">上一页</a>";
            }
            if (cur > 3 && cur < page_total - 2) {
                html += "<a class='zxfPagenum' onclick=\"go('1','" + orde + "')\">1</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (cur - 1) + "','" + orde + "')\">" + (cur - 1) + "</a>";
                html += "<a class='zxfPagenum current' onclick=\"go('" + (cur) + "','" + orde + "')\">" + (cur) + "</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (cur + 1) + "','" + orde + "')\">" + (cur + 1) + "</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total) + "','" + orde + "')\">" + (page_total) + "</a>";
            } else if (cur == 3) {
                html += "<a class='zxfPagenum' onclick=\"go('1','" + orde + "')\">1</a>";
                html += "<a class='zxfPagenum' onclick=\"go('2','" + orde + "')\">2</a>";
                html += "<a class='zxfPagenum current' onclick=\"go('3','" + orde + "')\">3</a>";
                html += "<a class='zxfPagenum' onclick=\"go('4','" + orde + "')\">4</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total) + "','" + orde + "')\">" + (page_total) + "</a>";
            } else if (cur == 1) {
                html += "<a class='zxfPagenum current' onclick=\"go('1','" + orde + "')\">1</a>";
                html += "<a class='zxfPagenum' onclick=\"go('2','" + orde + "')\">2</a>";
                html += "<a class='zxfPagenum' onclick=\"go('3','" + orde + "')\">3</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total) + "','" + orde + "')\">" + (page_total) + "</a>";
            } else if (cur == 2) {
                html += "<a class='zxfPagenum' onclick=\"go('1','" + orde + "')\">1</a>";
                html += "<a class='zxfPagenum current' onclick=\"go('2')\">2</a>";
                html += "<a class='zxfPagenum' onclick=\"go('3','" + orde + "')\">3</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total) + "','" + orde + "')\">" + (page_total) + "</a>";
            } else if (cur == page_total - 2) {
                html += "<a class='zxfPagenum' onclick=\"go('1','" + orde + "')\">1</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total - 3) + "','" + orde + "')\">" + (page_total - 3) + "</a>";
                html += "<a class='zxfPagenum current' onclick=\"go('" + (page_total - 2) + "','" + orde + "')\">" + (page_total - 2) + "</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total - 1) + "','" + orde + "')\">" + (page_total - 1) + "</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total) + "','" + orde + "')\">" + (page_total) + "</a>";
            } else if (cur == page_total - 1) {
                html += "<a class='zxfPagenum' onclick=\"go('1')\">1</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total - 2) + "','" + orde + "')\">" + (page_total - 2) + "</a>";
                html += "<a class='zxfPagenum current' onclick=\"go('" + (page_total - 1) + "','" + orde + "')\">" + (page_total - 1) + "</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total) + "','" + orde + "')\">" + (page_total) + "</a>";
            } else if (cur == page_total) {
                html += "<a class='zxfPagenum' onclick=\"go('1','" + orde + "')\">1</a>";
                html += "<a class='zxfPagenum' >...</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total - 2) + "','" + orde + "')\">" + (page_total - 2) + "</a>";
                html += "<a class='zxfPagenum' onclick=\"go('" + (page_total - 1) + "','" + orde + "')\">" + (page_total - 1) + "</a>";
                html += "<a class='zxfPagenum current' onclick=\"go('" + (page_total) + "','" + orde + "')\">" + (page_total) + "</a>";
            }


            if (cur == page_total) {
                html += "<a class='nextbtn disabled'>下一页</a>";
                html += "<a class='preblast disabled'>尾页</a>";
            } else {
                html += "<a class='nextbtn' onclick=\"go('" + (cur + 1) + "','" + orde + "')\">下一页</a>";
                html += "<a class='preblast' onclick=\"go('" + (page_total) + "','" + orde + "')\">尾页</a>";
            }
            html += "<span class='preball'>共<b>" + total + "</b>条,分<b>" + page_total + "</b>页显示</span>";
            html += "</div>";
            return html;

        } else if (page_total <= 7 && page_total > 1) {
            var html = "<div class='zxf_pagediv'>";
            if (cur == 1) {
                html += "<a class='prebfirst disabled'>首页</a>";
                html += "<a class='prebtn disabled'>上一页</a>";
            } else {
                html += "<a class='prebfirst' onclick=\"go('1','" + orde + "')\">首页</a>";
                html += "<a class='prebtn' onclick=\"go('" + (cur - 1) + "','" + orde + "')\">上一页</a>";
            }
            for (var i = 1; i <= page_total; i++) {
                if (cur == i) {
                    html += "<a class='zxfPagenum current' onclick=\"go('" + i + "','" + orde + "')\">" + i + "</a>";
                } else {
                    html += "<a class='zxfPagenum' onclick=\"go('" + i + "','" + orde + "')\">" + i + "</a>";
                }
            }
            if (cur == page_total) {
                html += "<a class='nextbtn disabled'>下一页</a>";
                html += "<a class='preblast disabled'>尾页</a>";
            } else {
                html += "<a class='nextbtn' onclick=\"go('" + (cur + 1) + "','" + orde + "')\">下一页</a>";
                html += "<a class='preblast' onclick=\"go('" + (page_total) + "','" + orde + "')\">尾页</a>";
            }
            html += "<span class='preball'>共<b>" + total + "</b>条,分<b>" + page_total + "</b>页显示</span>";
            html += "</div>";
            return html;
        } else {
            return "";
        }
    },
    jump_page: function (t,n) {
        var page = $(t).prev().find("input").val();
        go(page,n);
    }
}


/*
 *以逗号分隔获取所有选中的复选框的值
 */
function get_checkedval(separation ) {
    if(!separation){
        separation = ",";
    }
    var check_sp = $(".xd-15-table-fixed-body table tbody tr td div span");//.attr("value");
    var val = "";
    var ind = 0;
    for (var i = 0; i < check_sp.length; i++) {
        if ($(check_sp[i]).hasClass("checked")) {
            var value = $(check_sp[i]).attr("value")
            if (value) {
                if (ind == 0) {
                    val += $(check_sp[i]).attr("value");
                } else {
                    val += separation + $(check_sp[i]).attr("value");
                }
                ind += 1;
            }
        }
    }

    return val;
}

/*
 *以分号分隔获取所有选中的复选框的值
 */
function get_checkedval2() {
    var check_sp = $(".xd-15-table-fixed-body table tbody tr td div span");//.attr("value");
    var val = "";
    var ind = 0;
    for (var i = 0; i < check_sp.length; i++) {
        if ($(check_sp[i]).hasClass("checked")) {
            if (ind == 0) {
                val += $(check_sp[i]).attr("value");
            } else {
                val += ";" + $(check_sp[i]).attr("value");
            }
            ind += 1;
        }
    }
    return val;
}

/**
 * 表单验证 提交
 *
 *
 * @param {String} formid
 * @param {Object} validate 验证信息
 * @param {Object} options 配置信息
 * @param {String} options.roteUrl 重新加载的url 默认为当前url
 * @param {Boolean} [options.enableDelPageInfo = false] 是否删除链接中的分页信息 并且刷新整个页面
 * @param {Boolean} [options.enableRefrushUrl = true] 是否重新加载页面
 * @param {function} options.onSuccess 页面加载成功回调函数
 *
 *
 */
function form_validate(formid, validate, options) {

    // 手机号码验证
    jQuery.validator.addMethod("isMobile", function (value, element) {
        var length = value.length;
        var mobile = /^(1[0-9]{10})$/;
        return this.optional(element) || (length == 11 && mobile.test(value));
    }, "请正确填写您的手机号码");

    jQuery.validator.addMethod("ip", function (value, element) {
        return this.optional(element) || /^(([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.)(([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))\.){2}([1-9]|([1-9]\d)|(1\d\d)|(2([0-4]\d|5[0-5])))$/.test(value);
    }, "请填写正确的IP地址。");


    jQuery.validator.addMethod("checkPassword", function (value, element) {
        //包含数字、字母
        //return this.optional(element) || /(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9])/.test(value);
        //包含数字、字母、特殊字符
        return this.optional(element) || /(?=.*[0-9])(?=.*[a-zA-Z])/.test(value);
    }, "必须包含字母、数字");

    jQuery.validator.addMethod("hasChinese", function (value, element) {
        return this.optional(element) || /^[^\u4e00-\u9fa5]+$/.test(value);
    }, "不能含有中文");

    jQuery.validator.addMethod("checkFileSize", function (value, element) {
        var fileSize = element.files[0].size;
        //var maxSize = 10*1024*1024;
        var maxSize = 1000 * 1024 * 1024;
        if (fileSize > maxSize) {
            return false;
        } else {
            return true;
        }
    }, "请上传大小在10M一下的图片");

    jQuery.validator.addMethod("checkpattern", function (value, element) {
        var _pattern = ($(element).data("pattern")+"").trim();
        if (_pattern){
            var reg = new RegExp();
            reg.compile(_pattern);
            if (reg.test(value)) {
                // 检测通过
                return true;
            } else {
                return false;
            }
        }else{
            return true;
        }
    }, "字段格式有误");

    $.validator.addMethod('lessThen', function (value, element, ruleValue) {
        var siblings = $(element).siblings('[name=' + ruleValue + ']');
        var validResult = parseInt(value) <= parseInt(siblings.val());
        if (validResult){
            // 获取配置的errorClass
            var errClass = this.settings.errorClass;
            $(siblings).removeClass(errClass);
        }
        return !siblings.val() || validResult;
    }, '请输入正确的配置策略');

    $.validator.addMethod('greaterThen', function (value, element, ruleValue) {
        var siblings = $(element).siblings('[name=' + ruleValue + ']');
        // siblings.focus().blur();
        var validResult = parseInt(value) >= parseInt(siblings.val());
        if (validResult){
            // 获取配置的errorClass
            var errClass = this.settings.errorClass;
            $(siblings).removeClass(errClass);
        }
        return !siblings.val() || validResult;
    }, '请输入正确的配置策略');

    $('#' + formid).validate({
        errorPlacement: function (error, element) {
            if (element.is(":radio"))
                //error.appendTo(element.parent());
                error.appendTo(element.parent().next());
            else if (element.is(":checkbox"))
                //error.appendTo(element.parent().parent());
                error.appendTo(element.parent().next());
            else if (element.siblings('.error-msg').length >= 1)// 添加一种在控件旁边<span class="error-msg"></span>
                error.appendTo(element.siblings('.error-msg'));
            else
                error.appendTo(element.parent().next());
        },
        rules: validate.rules || {},
        messages: validate.messages || {},
        submitHandler: validate.submitHandler || default_submitHandler
    });

    function default_submitHandler(form) {
        var encryptor, publicKey;
        var formdata = {};
        $.ajax({
            async: false,
            type: 'POST',
            dataType: 'json',
            url: config.api_base + "/auth/getToken",
            data: {},
            success: function (json) {
                if (json != null) {
                    if (json.status == 1) {
                        encryptor = new CimsEncryptor(json.data.encryptor);
                        publicKey = json.data.publicKey;
                    } else if (json.status == 0) {
                        alert("系统异常，请联系系统管理员");
                    }
                }
            },
        });
        var action = $("#" + formid).attr("action");
        var enctype = $("#" + formid).attr("enctype");
        var method = $("#" + formid).attr("method");
        var $form = $(form), data = $form.serialize();     //序列化表单数据
        $("#btn_confim").attr("disabled", "disabled");

        var ajaxParam = $.extend({
            url: config.api_base + "" + action,
            data: data
        }, options);

        if(formid == "addSpUserForm"){
            var dataArray = $form.serializeArray();
            for (var k in dataArray) {
                var dataObj = dataArray[k];
                if(dataObj.name == 'initLoginPwd'){
                    dataObj.value = encryptor.encrypt(publicKey, dataObj.value);
                    formdata[dataObj.name] = dataObj.value;
                }else{
                    formdata[dataObj.name] = dataObj.value;
                }
            }
            ajaxParam = $.extend({
                url: config.api_base + "" + action,
                data: formdata
            }, options);
        }

        if(formid == "editLdapUserPwdForm"){
            var dataArray = $form.serializeArray();
            for (var k in dataArray) {
                var dataObj = dataArray[k];
                if(dataObj.name != 'userid' && dataObj.name != 'username'){
                    dataObj.value = encryptor.encrypt(publicKey, dataObj.value);
                    formdata[dataObj.name] = dataObj.value;
                }else{
                    formdata[dataObj.name] = dataObj.value;
                }
            }
            ajaxParam = $.extend({
                url: config.api_base + "" + action,
                data: formdata
            }, options);
        }

        if(formid == "editPassForm"){
            var dataArray = $form.serializeArray();
            for (var k in dataArray) {
                var dataObj = dataArray[k];
                if(dataObj.name == 'username'){
                    formdata[dataObj.name] = dataObj.value;
                }else{
                    dataObj.value = encryptor.encrypt(publicKey, dataObj.value);
                    formdata[dataObj.name] = dataObj.value;
                }
            }
            ajaxParam = $.extend({
                url: config.api_base + "" + action,
                data: formdata
            }, options);
        }

        if ("multipart/form-data" == enctype) {
            var formData = new FormData($("#" + formid)[0]);
            ajaxParam.data = formData;
            ajax.upload(ajaxParam);
        } else {
            if (action.indexOf("add") != -1) {
                ajax.add(ajaxParam);
            } else {
                ajax.edit(ajaxParam);
            }
        }

    }
}


/**
 * json参数
 *    suffix:限定上传文件的后缀名,未指定用默认的
 *    maxSize:限定上传文件的大小,未指定用默认的
 *    showid:回显上传文件的id
 */
var upload = {
    show: function (json) {

        var suffix = json.suffix || ".jpg;.jpeg;.png";
        var maxSize = json.maxSize || 1000 * 1024 * 1024;
        var showid = json.showid;
        var id = json.id || "";
        var callback=json.callback || "";
        var spid=json.spid || "";
        var title= json.title|| "文件上传";

        jQuery.validator.addMethod("checkFileSuffix", function (value, element) {
            if(element.files==null || element.files.length<1){
                Tip.showinfo("请选择上传文件！");
                return false;
            }
            var fileName = element.files[0].name;
            var suf = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length);
            if (suffix.indexOf(suf) != -1) {
                return true;
            }
            return false;
        }, "允许上传扩展名只能是" + suffix);


        var max = "";
        if ((maxSize / 1024) < 1024) {
            max = maxSize / 1024 + "KB";
        } else {
            max = maxSize / 1024 / 1024 + "MB";
        }


        var message = "文件大小控制在" + max + "以内";
        jQuery.validator.addMethod("checkFileSize", function (value, element) {
            var fileSize = element.files[0].size;
            if (fileSize > maxSize) {
                return false;
            } else {
                return true;
            }
        }, message);


        var validate = {
            rules: {
                file: {
                    checkFileSuffix: true,
                    checkFileSize: true
                }
            }
        };

        var url = config.base_url + "/templates/upload.ejs";
        var html = new EJS({url: url}).render({"id": id});
        Dialog.open({"title": title, "content": html, "width": 500, "height": 300, "type": "layer"})

        var formid = "uploadForm";
        $('#uploadForm').validate({
            errorPlacement: function (error, element) {
                error.appendTo(element.parent().parent().next().children().first().next());
            },
            rules: validate.rules || {},
            messages: validate.messages || {},
            submitHandler: function (form) {
                var action = $("#" + formid).attr("action");
                var enctype = $("#" + formid).attr("enctype");
                var method = $("#" + formid).attr("method");
                var $form = $(form), data = $form.serialize();     //序列化表单数据
                $("#btn_confim").attr("disabled", "disabled");
                var formData = new FormData($("#" + formid)[0]);
                ajax.upload({
                    url: config.api_base + "" + action,
                    data: formData,
                    enableRefrushUrl: false,
                    showid: showid,
                    spid: spid,
                    callback: callback
                });
            }
        });
    }
};


function remove_upload(showid) {

    $("#" + showid).attr("value", "");
    $("#_" + showid).attr("src", "");

}


/*
 * param str 要截取的字符串
 * param L 要截取的字节长度，注意是字节不是字符，一个汉字两个字节
 * return 截取后的字符串
 */
function cutStr(str) {
    var L = 12;
    var result = '';
    if (str) {
        strlen = str.length, // 字符串长度
            chrlen = str.replace(/[^\x00-\xff]/g, '**').length; // 字节长度

        if (chrlen <= L) {
            return str;
        } else {
            for (var i = 0, j = 0; i < strlen; i++) {
                var chr = str.charAt(i);
                if (/[\x00-\xff]/.test(chr)) {
                    j++; // ascii码为0-255，一个字符就是一个字节的长度
                } else {
                    j += 2; // ascii码为0-255以外，一个字符就是两个字节的长度
                }
                if (j <= L) { // 当加上当前字符以后，如果总字节长度小于等于L，则将当前字符真实的+在result后
                    result += chr;
                } else { // 反之则说明result已经是不拆分字符的情况下最接近L的值了，直接返回
                    return result + "...";
                }
            }
        }
    }
    return result;
}


function authParseStr(status, authPolicy, spAuthConf) {
    if (status === 0) {
        return "无策略";
    }

    // TODO 此处逻辑整理
    if (status === 1 && spAuthConf.status !== 0) {
        var result = [],
            spAuthStatus = spAuthConf.status,
            confArray = spAuthStatus === 1 ? spAuthConf.ways : spAuthStatus === 2 ? spAuthConf.mulit : [];

        $.each(confArray, function (i, val) {
            result.push(parseAuth(val))
        });
        return result.join(" | ");
    }


    if (!authPolicy) {
        return "";
    }
    var confArray = JSON.parse(authPolicy);
    if (status === 1) {
        return "安全级别" + confArray[0];
    } else {
        var result = [];
        $.each(confArray, function (i, val) {
            result.push(parseAuth(val))
        });
        return result.join(" | ");
    }
}

function parseAuth(authId) {
    switch (authId.toString()) {
        case "0" :
            return "二维码";
        case "1" :
            return "APP一键登录";
        case "2" :
            return "动态令牌";
        case "3" :
            return "短信验证";
        case "4" :
            return "语音认证";
        case "5" :
            return "临时授权码";
        case "6" :
            return "人脸验证";
        case "7" :
            return "声纹验证";
        case "8" :
            return "密码登录";
        case "9" :
            return "微信登录";
        case "10" :
            return "UKEY登录";
        case "11" :
            return "钉钉登录";
        case "12" :
            return "AD域认证";
        case "13" :
            return "离线令牌";
        case "n" :
            return "无";
        default :
            return "未知";
    }
}

function TimeDifference(timestamp1) {
    if (!timestamp1 && timestamp1 !== 0) {
        return true;
    }
    var timestamp2 = Date.parse(new Date());
    if (timestamp1 > timestamp2) {
        return true;
    } else {
        return false;
    }
}

/**
 * 根据两个百日期度，判断相差天问数
 * @param sDate1 开始日期 如：2016-11-01
 * @param sDate2 结束日期 如：2016-11-02
 * @returns {number} 返回相差天数
 */
function daysBetween(sDate1,sDate2){
    //Date.parse() 解析一个日期时间字符串，并返答回1970/1/1 午夜距离该日期时间的毫秒数
    var time1 = Date.parse(new Date(sDate1));
    var time2 = Date.parse(new Date(sDate2));
    var nDays = Math.abs(parseInt((time2 - time1)/1000/3600/24));
    return nDays;
}

// 去掉一个表单内，只验证第一个name相同的约束

/**
 *  jquery.validate 这个插件在生成rules的时候是按name来生成的，也就是说，
 *  你的表单其实只添加了一条验证rule：就是对name=test_a的字段做非空和最小长度验证。
 当输入框失去焦点时会触发这条规则，因为每个input的name都是test_a，可以命中rules中的规则
 当submit的时候，同样会调用{'test_a': { required:true, minlength: 2}}这条规则，
 只不过这条规则会被通过，因为已经有一个test_a字段达到了规则的要求。
 */
(function ($) {
    if ($.validator) {
        $.validator.prototype.elements = function () {
            var validator = this,
                rulesCache = {};
            return $(this.currentForm)
                .find("input, select, textarea")
                .not(":submit, :reset, :image, [disabled]")
                .not(this.settings.ignore)
                .filter(function () {
                    var name = this.id || this.name || this.value; // 判断name 首先是id, name, value 。
                    // 可以规避同一个form里面相同name不验证的问题。
                    if (!name && validator.settings.debug && window.console) {
                        console.error("%o has no name assigned", this);
                    }
                    // 注释掉这里
                    //select only the first element for each name, and only those with rules specified
                    if (name in rulesCache || !validator.objectLength($(this).rules())) {
                        return false;
                    }
                    rulesCache[name] = true;
                    return true;
                });
        }
    }
})($)


String.prototype.myReplace=function(f,e){//吧f替换成e
    var reg=new RegExp(f,"g"); //创建正则RegExp对象
    return this.replace(reg,e);
}

function toggleSwitch(ele) {
    if ($(ele).hasClass("xd-checkbox-on")) {
        $(ele).removeClass("xd-checkbox-on")
    } else {
        $(ele).addClass("xd-checkbox-on")
    }
}


/**
 * 转义特殊字符，防止xss攻击
 * @param strOrObject 可能带特殊字符的字符串
 * @returns {string|*} 转移后的字符串或对象
 */
function iamEscapeStr(strOrObject){
    var escapeMap = {
        "<": "&#60;",
        ">": "&#62;",
        '"': "&#34;",
        "'": "&#39;",
        "&": "&#38;"
    };

    var escapeFn = function (s) {
        return escapeMap[s];
    };

    var toString = function (value, type) {
        if (typeof value !== 'string') {
            type = typeof value;
            if (type === 'number') {
                value += '';
            } else if (type === 'function') {
                value = toString(value.call(value));
            } else {
                value = '';
            }
        }
        return value;

    };

    var escapeHTML = function (content) {
        return toString(content)
            .replace(/&(?![\w#]+;)|[<>"']/g, escapeFn);
    };

    if ($.isPlainObject(strOrObject) || $.isArray(strOrObject)) {
        $.each(strOrObject, function(k,v) {
            strOrObject[k] = iamEscapeStr(v);
        });
        return strOrObject;
    } else {
        return escapeHTML(strOrObject);
    }
}