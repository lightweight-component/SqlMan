var scrollBar = $("#scroll-bar");

function setScrollBarWidth() {
    // 获取总共的app条数
    var navNum = $("#auth-footer-nav-container").find(".auth-foot-nav").length;
    // 一页占总共的百分比
    var widthPercentage = (6 / navNum * 100) + "%";
    scrollBar.css("width", widthPercentage);
}
function setScrollPosition() {
    // 获取nav父容器的滚动距离
    var container = $(".auth-footer-nav-container");
    var containerWidth = $(".auth-footer-nav-container").width();
    var navNum = $("#auth-footer-nav-container").find(".auth-foot-nav").length;
    var allWidth = containerWidth * navNum / 6;

    container.scroll(function name() {
        var left = container.scrollLeft();
        var leftPercentage = left / allWidth;
        scrollBar.css("left", leftPercentage * 100 + "%");
    })
}
function setScrollBar(params) {
    setScrollBarWidth();
    setScrollPosition();
}

window.setScrollBar = setScrollBar;