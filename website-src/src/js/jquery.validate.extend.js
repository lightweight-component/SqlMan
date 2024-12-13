/*************************插件validator*****************************************/
$.validator.setDefaults({
    onkeyup: null,
    success: function (label) {
        label.text('').addClass('valid');
    },
    onfocusin: function (element) {
        this.lastActive = element;
        this.addWrapper(this.errorsFor(element)).hide();
        var tip = $(element).attr('tip');
        if (tip && $(element).parent().children(".tip").length === 0) {
            $(element).parent().append("<label class='tip'>" + tip + "</label>");
        }

        $(element).addClass('highlight');
        if (this.settings.focusCleanup) {
            if (this.settings.unhighlight) {
                this.settings.unhighlight.call(this, element, this.settings.errorClass, this.settings.validClass);
            }
            this.hideThese(this.errorsFor(element));
        }
    },
    onfocusout: function (element) {
        $(element).parent().children(".tip").remove();
        $(element).removeClass('highlight');
        if (!this.checkable(element) && (element.name in this.submitted || !this.optional(element))) {
            this.element(element);
        }
    }
});
//自定义密码强度校验
/*$.validator.addMethod("password", function(value, element, param){
    return new RegExp(/^(?![a-zA-z]+$)(?!\d+$)(?![!@#$%^&*()_+|<>,.?/:;'\\[\]{}"]+$)[a-zA-Z\d!@#$%^&*()_+|<>,.?/:;'\\[\]{}"]{8,16}$/).test(value);

}, "密码需由8-16位数字、字母、特殊字符的两种或三种组成");*/


$.validator.addMethod("phoneNumber", function (value, element, param) {
    return new RegExp(/^1[0-9]{10}$/).test(value);

}, "请输入正确的手机号码");

$.validator.addMethod("noSpace", function (value, element, param) {
    return new RegExp(/^[^ ]+$/).test(value);

}, "密码不能包含空格");
