define(function () {


    if (window && window.console) {
        return window.console;
    }

    function donothing() {}

    return {
        info: donothing,
        warn: donothing,
        err: donothing
    }
});