$(document).ready(function () {
    $('input, textarea').placeholder();
    /*更多验证方式*/
    $(".moreways span").on("click", function () {
        $(this).parents(".main").addClass("open-right");
        $(".conlogin-rightout")
            .addClass("conlogin-rightout1");
        $(".conlogin-rightout").delay(1).animate({
            left: '-719px'
        }, 600);
        $(this).parents(".moreways").hide();
        $(".otherways").delay(100).animate({
            right: '-349px'
        }, 600);
        $(this).parents(".main").attr("style","width:1180px;")
    });
    /*返回上一页*/
    $(".otherways .go-backpage>span").on("click", function () {
        $(this).parents(".main").attr("style","width:1100px;")
        $(this).parents(".main").removeClass("open-right");
        $(".conlogin-rightout").removeClass("conlogin-rightout1");
        $(".moreways").show();
        setTimeout(function () {
            $("#wrapper").removeClass("wrapper");
        }, 180);
        $(".conlogin-rightout").delay(1).animate({
            left: '0px'
        }, 600);
        $(".otherways").delay(100).animate({
            right: '-1100px'
        }, 600);
    });
})