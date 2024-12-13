var urlParams = (function parseQueryString(url) {
    var params = {};
    var arr = url.split("?");
    if (arr.length === 1) {
        return params;
    }
    var arr1 = arr[1].split("&");
    for (var i = 0; i < arr1.length; i++) {
        var arr2 = arr1[i].split('=');
        if (!arr2[1]) {
            params[arr2[0]] = 'true';
        } else {
            if (params[arr2[0]]) {
                var arr3 = [params[arr2[0]]];
                arr3.push(arr2[1]);
                params[arr2[0]] = arr3;
            } else {
                params[arr2[0]] = decodeURI(arr2[1]);
            }
        }
    }
    return params;
}) (window.location.href);