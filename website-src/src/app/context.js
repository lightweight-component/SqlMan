define(['jquery', 'log'], function ($, log) {

    var defaultContext = {
        appName: '',
        accountScope: '邮箱/手机号/工号'
    };

    var context = $.extend({}, defaultContext);
    var challenge = {};
    var actions = [];
    var nextActions = [];
    var certification = "";
    var transactionId = ""

    function getActions() {
        return actions;
    }
    function getNextActions() {
        return nextActions;
    }

    function getCertification() {
        return certification;
    }

    function setActions(actionArray) {
        if (actionArray instanceof Array){
            actions = actionArray;
        }
    }

    function getType(id) {
        for (var i = 0; i < actions.length; i++) {
            if (actions[i].id === id) {
                if ('type' in actions[i] && actions[i].type == 1) {
                    return true
                }else {
                    return false
                }
            }
        }
        
    }

    function setnextActions(actionArray) {
        if (actionArray instanceof Array){
            nextActions = actionArray;
        }
    }

    function getContext() {
        return context;
    }

    function setContext(key, value) {
        context[key] = value;
    }

    function getChallenge() {
        return challenge;
    }

    function getTransactionId() {
        return transactionId;
    }

    function setChallengeFromResponseBody(resp) {
        if (!resp || !resp.responseBody) {
            return;
        }

        var body = resp.responseBody;
        if (body.contextRaw) {
            challenge.contextRaw = body.contextRaw;
        }

        if (body.challengeId) {
            challenge.challengeId = body.challengeId;
        }

        if ('showProtocol' in body) {
            challenge.showProtocol = body.showProtocol;
            // challenge.showProtocol = true;
        }

        if ('autoTrigger' in body) {
            challenge.autoTrigger = body.autoTrigger;
        }

        if (body.nextActions){
            setnextActions(body.nextActions)
        }

        if (body.certification) {
            certification = body.certification
        }

        if (body.length > 0) {
            transactionId = body[1]
        }

        // 认证成功后 设置当前上下文账号信息
        if (body.result === 'ALLOW' && body.account) {
            challenge.account = body.account;
        }

    }

    function setCrossBrowserId(crossBrowserId) {
        challenge.crossBrowserId = crossBrowserId;
    }


    function setContextFromInitResponseBody(resp) {
        if (!resp || !resp.responseBody) {
            log.error('非法的响应内容，无法解析到认证挑战信息');
            throw '非法的响应内容，无法解析到认证挑战信息';
        }

        var body = resp.responseBody;
        if (body.actions){
            setActions(body.actions)
        }

        context = $.extend(context, {
            crossBrowserUrl: body.crossBrowserUrl,
            appName: body.page.appName,
            appTitle: body.page.systemName
        });
        setChallengeFromResponseBody(resp);
    }

    function resetAll() {
        context = $.extend({}, defaultContext);
        challenge = {};
        resetAuthCache();
    }


    var authCache = {};

    function getAuthCache(authCode) {
        if (!authCache[authCode]) {
            authCache[authCode] = {};
        }
        return authCache[authCode];
    }

    function setAuthCache(authCode, data) {
        authCache[authCode] = data;
    }

    function resetAuthCache() {
        authCache = {};
    }

    return {
        getContext: getContext,
        setContext: setContext,
        getChallenge: getChallenge,
        getAuthCache: getAuthCache,
        setAuthCache: setAuthCache,
        resetAuthCache: resetAuthCache,
        resetAll: resetAll,
        setContextFromInitResponseBody: setContextFromInitResponseBody,
        setChallengeFromResponseBody: setChallengeFromResponseBody,
        setCrossBrowserId: setCrossBrowserId,
        getActions: getActions,
        getNextActions: getNextActions,
        getCertification: getCertification,
        getTransactionId:getTransactionId,
        getType: getType,
    }
});