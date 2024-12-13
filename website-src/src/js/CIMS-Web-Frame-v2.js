window.CIMS = (function (document, window) {
    var CIMS_MESSAGE_FORMAT = /^(?:AUTH|ENROLL)+\|[A-Za-z0-9\+\/=]+\|[A-Za-z0-9\+\/=]+$/;
    var CIMS_GO_FORMAT = /^GO\|[A-Z]{4}\|[a-zA-Z0-9\/\+]+\=*\|[a-zA-Z0-99\/\+]+=*$/;
    var CIMS_ERROR_FORMAT = /^ERR\|[\w\s\.\(\)]+$/;
    options = {};         // global variable
    function init(opt) {
        options = opt || {};
        if (checkParameter()) {
            onReady(onDOMReady);
        }
    }

    function messages(msg, opt) {
        var opt = opt || {time: 5000, shadeClose: true};
        layer.msg(msg, opt);
    }

    function onDOMReady() {
        iframe = $$(options.containerid);
        var contentUrl = [options.host, 'login.html?view=frame&type=0&sign=', encodeURIComponent(cimsSig), '&parent=',
            encodeURIComponent(document.location.href), '&version=', options.version, '&email=', options.email].join('');
        iframe.src = contentUrl;
        // reloadPage();
    }

    function go(response) {
        var sigParts = response.split('|');
        if (sigParts.length !== 4) {
            throwError('获取签名参数个数错误!');
        }
        response = response.substr(response.indexOf('|') + 1);
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = options.postArgument;
        input.value = response + ':' + appSig;

        var form;
        if (options.formid) {
            form = document.getElementById(options.formid);
        } else {
            form = document.createElement('form');
            iframe.parentElement.insertBefore(form, iframe.nextSibling);
        }

        // make sure we are actually posting to the right place
        form.method = 'POST';
        form.action = options.postaction;
        form.target = "_top";
        // add the response token input to the form
        form.appendChild(input);
        // away we go!
        if (typeof submitCallback === "function") {
            submitCallback.call(null, form);
        } else {
            form.submit();
        }
    }

    function checkParameter() {

        if (!options.host) {
            throwError('No API hostname is given for CIMS to use.  Be sure to pass a `host` parameter to CIMS.init');
        } else {
            if (!options.host.startWith('http')) {
                options.host = 'http://' + options.host;
            }
            if (!options.host.endWith('/')) {
                options.host = options.host + '/';
            }
            //处理url不规范的情况 会导致sessionid变更 session为空
            options.host = options.host.replace(new RegExp(/([^\:])\/\/+/g), '$1/');
        }
        if (!options.email) {
            options.email = '';
        }
        if (!options.postArgument) {
            options.postArgument = 'sig_response';
        }
        if (!options.containerid) {
            throw new Error(
                'This page does not contain an Div for CIMS to use.'
                + 'Add an element like <iframe id="cims_iframe"></iframe> '
                + 'to this page.  ');
        } else {
            $$(options.containerid).style.width = '100%';
            $$(options.containerid).style.height = '100%';
        }

        if (options.width) {
            $$(options.containerid).style.width = options.width;
        } else {
            $$(options.containerid).style.width = '100%';
        }

        if (options.height) {
            $$(options.containerid).style.height = options.height;
        } else {
            $$(options.containerid).style.height = '100%';
        }

        if (!options.version) {
            options.version = 'v2';
        }

        if (!options.qrpageurl) {
            throw new Error(
                "qrpageurl can't be null");
        } else {
            options.qrpageurl = options.qrpageurl.replace(new RegExp(/([^\:])\/\/+/g), '$1/');
        }
        if (!cimsSig || !appSig) {
            parseSigRequest(options.sig_request);

            if (!cimsSig || !appSig) {
                throwError('No valid signed request is given.  Be sure to give the '
                    + '`sig_request` parameter to CIMS.init');
            }
        }
        if (typeof options.submit_callback === 'function') {
            submitCallback = options.submit_callback;
        }

        onMessage(onReceivedMessage);

        // listen for the 'message' event
        return true;
    }

    function onReceivedMessage(event) {
        if (isCIMSGOMessage(event)) {
            go(event.data);
            offMessage(onReceivedMessage);
        } else if (event.data == 'RELOAD_PAGE') {
            top.location.href = options.qrpageurl;
        }
    }

    function parseSigRequest(sig) {
        if (!sig) {
            // nothing to do
            return;
        }
        // see if the token contains an error, throwing it if it does
        if (sig.indexOf('ERR|') === 0) {
            throwError(sig.split('|')[1]);
        }
        // validate the token
        if (sig.indexOf(':') === -1 || sig.split(':').length !== 2) {
            throwError(
                'CIMS was given a bad token.  This might indicate a configuration '
                + 'problem with one of CIMS\'s client libraries.');
        }

        var sigParts = sig.split(':');
        // hang on to the token, and the parsed cims and app sigs
        sigRequest = sig;
        cimsSig = sigParts[0];
        appSig = sigParts[1];
        return {
            sigRequest: sig,
            cimsSig: sigParts[0],
            appSig: sigParts[1]
        };
    }

    // cross-browser event binding/unbinding
    function on(context, event, fallbackEvent, callback) {
        if ('addEventListener' in window) {
            context.addEventListener(event, callback, false);
        } else {
            context.attachEvent(fallbackEvent, callback);
        }
    }

    function off(context, event, fallbackEvent, callback) {
        if ('removeEventListener' in window) {
            context.removeEventListener(event, callback, false);
        } else {
            context.detachEvent(fallbackEvent, callback);
        }
    }

    function onReady(callback) {
        on(document, 'DOMContentLoaded', 'onreadystatechange', callback);
    }

    function offReady(callback) {
        off(document, 'DOMContentLoaded', 'onreadystatechange', callback);
    }

    function offMessage(callback) {
        off(window, 'message', 'onmessage', callback);
    }

    function onMessage(callback) {
        on(window, 'message', 'onmessage', callback);
    }

    function $$(id) {
        return document.getElementById(id);
    }

    function getRootPath() {
        var pathName = window.location.pathname.substring(1);
        var webName = pathName == '' ? '' : pathName.substring(0, pathName.indexOf('/'));
        if (webName == "") {
            return window.location.protocol + '//' + window.location.host;
        }
        else {
            return window.location.protocol + '//' + window.location.host + '/' + webName;
        }
    }

    function throwError(message, url) {
        throw new Error('CIMS Web SDK error: ' + message);
    }

    function isCIMSGOMessage(event) {
        return Boolean(
            options.host.startWith(event.origin) &&
            typeof event.data === 'string' &&
            (
                event.data.match(CIMS_GO_FORMAT)
            )
        );
    }

    function isCIMSMessage(event) {
        return Boolean(
            options.host.startWith(event.origin) &&
            typeof event.data === 'string' &&
            (
                event.data.match(CIMS_MESSAGE_FORMAT) ||
                event.data.match(CIMS_GO_FORMAT) ||
                event.data.match(CIMS_ERROR_FORMAT)
            )
        );
    }

    function reloadPage() {
        top.location.href = options.qrpageurl;
//		var c = expireTime / 1000;
//		
//		setInterval(function(){
//			c--;
//			console.log(c);
//			if(c <= 3){
//				console.log('还剩余'+ c +'秒过期');
//			}
//		}, 1000);
//		setTimeout(function(){
//				top.location.href = options.qrpageurl;
//		 },expireTime);
    }

    String.prototype.startWith = function (str) {
        var reg = new RegExp("^" + str);
        return reg.test(this);
    };
    String.prototype.endWith = function (str) {
        var reg = new RegExp(str + "$");
        return reg.test(this);
    };
    String.prototype.trim = function () {
        return this.replace(/(^\s*)|(\s*$)/g, "");
    };
    String.prototype.ltrim = function () {
        return this.replace(/(^\s*)/g, "");
    };
    String.prototype.rtrim = function () {
        return this.replace(/(\s*$)/g, "");
    };

    var sigRequest, cimsSig, appSig;

    return {
        init: init,
        go: go
    };
}(document, window));