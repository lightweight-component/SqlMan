define(['context'], function (context) {


    var crossbrowser = function (issueData) {
        var authContext = context.getContext();
        var ticket = issueData.ticket;

        $.ajax({
            type: "POST",
            url: authContext.crossBrowserUrl + "/sso/ticket",
            dataType: 'json',
            async: false,
            cache: false,
            data:JSON.stringify({"dev_ticket" : ticket}),
            success: function (result) {}
        });
    };

    var iamTicket = function (issueData) {
        location.href = issueData.go;
    };

    var authIssue = function (issueData) {
        if (issueData.issue === 'POST_MESSAGE') {
            if (window.parent && window.parent.postMessage) {
                window.parent.postMessage({
                    "ticket": issueData.token,
                    "type": "POST_MESSAGE"
                }, issueData.url);
            }
        } else if (issueData.issue === "REDIRECT") {
            window.location.href = issueData.url;
        }
    };

    /**
     * 获取认证完成处理器
     * @param issueType {string}
     * @returns {function} 处理认证完成的方法
     */
    function getByType(issueType) {
        switch (issueType) {
            case 'crossbrowser':
                return crossbrowser;
            case 'IAM_TICKET':
                return iamTicket;
            case 'AUTH_ISSUE':
                return authIssue;
            default:
                throw '没有找到合适的处理方式：' + issueType;
        }
    }

    return {
        getByType: getByType
    };
});