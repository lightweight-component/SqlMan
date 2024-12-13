
/*页面执行加载执行*/
define(['jquery', 'i18n'], function ($, i18n) {
    
    (function($){$.i18n={};$.i18n.map={};$.i18n.properties=function(settings){var defaults={name:'messages',language:'',path:'',mode:'vars',cache:false,encoding:'UTF-8',callback:null};settings=$.extend(defaults,settings);if(settings.language===null||settings.language==''){settings.language=$.i18n.browserLang()}if(settings.language===null){settings.language=''}var files=getFiles(settings.name);for(i=0;i<files.length;i++){loadAndParseFile(require('@/'+settings.path+files[i]+'.properties'),settings)}if(settings.callback){settings.callback()}};$.i18n.prop=function(key){var value=$.i18n.map[key];if(value==null)return'['+key+']';var i;if(typeof(value)=='string'){i=0;while((i=value.indexOf('\\',i))!=-1){if(value[i+1]=='t')value=value.substring(0,i)+'\t'+value.substring((i++)+2);else if(value[i+1]=='r')value=value.substring(0,i)+'\r'+value.substring((i++)+2);else if(value[i+1]=='n')value=value.substring(0,i)+'\n'+value.substring((i++)+2);else if(value[i+1]=='f')value=value.substring(0,i)+'\f'+value.substring((i++)+2);else if(value[i+1]=='\\')value=value.substring(0,i)+'\\'+value.substring((i++)+2);value=value.substring(0,i)+value.substring(i+1)}var arr=[],j,index;i=0;while(i<value.length){if(value[i]=='\''){if(i==value.length-1)value=value.substring(0,i);else if(value[i+1]=='\'')value=value.substring(0,i)+value.substring(++i);else{j=i+2;while((j=value.indexOf('\'',j))!=-1){if(j==value.length-1||value[j+1]!='\''){value=value.substring(0,i)+value.substring(i+1,j)+value.substring(j+1);i=j-1;break}else{value=value.substring(0,j)+value.substring(++j)}}if(j==-1){value=value.substring(0,i)+value.substring(i+1)}}}else if(value[i]=='{'){j=value.indexOf('}',i+1);if(j==-1)i++;else{index=parseInt(value.substring(i+1,j));if(!isNaN(index)&&index>=0){var s=value.substring(0,i);if(s!="")arr.push(s);arr.push(index);i=0;value=value.substring(j+1)}else i=j+1}}else i++}if(value!="")arr.push(value);value=arr;$.i18n.map[key]=arr}if(value.length==0)return"";if(value.lengh==1&&typeof(value[0])=="string")return value[0];var s="";for(i=0;i<value.length;i++){if(typeof(value[i])=="string")s+=value[i];else if(value[i]+1<arguments.length)s+=arguments[value[i]+1];else s+="{"+value[i]+"}"}return s};$.i18n.browserLang=function(){return normaliseLanguageCode(navigator.language||navigator.userLanguage)};function loadAndParseFile(filename,settings){$.ajax({url:filename,async:false,cache:settings.cache,contentType:'text/plain;charset='+settings.encoding,dataType:'text',success:function(data,status){parseData(data,settings.mode)}})}function parseData(data,mode){var parsed='';var parameters=data.split(/\n/);var regPlaceHolder=/(\{\d+\})/g;var regRepPlaceHolder=/\{(\d+)\}/g;var unicodeRE=/(\\u.{4})/ig;for(var i=0;i<parameters.length;i++){parameters[i]=parameters[i].replace(/^\s\s*/,'').replace(/\s\s*$/,'');if(parameters[i].length>0&&parameters[i].match("^#")!="#"){var pair=parameters[i].split('=');if(pair.length>0){var name=unescape(pair[0]).replace(/^\s\s*/,'').replace(/\s\s*$/,'');var value=pair.length==1?"":pair[1];while(value.match(/\\$/)=="\\"){value=value.substring(0,value.length-1);value+=parameters[++i].replace(/\s\s*$/,'')}for(var s=2;s<pair.length;s++){value+='='+pair[s]}value=value.replace(/^\s\s*/,'').replace(/\s\s*$/,'');if(mode=='map'||mode=='both'){var unicodeMatches=value.match(unicodeRE);if(unicodeMatches){for(var u=0;u<unicodeMatches.length;u++){value=value.replace(unicodeMatches[u],unescapeUnicode(unicodeMatches[u]))}}$.i18n.map[name]=value}if(mode=='vars'||mode=='both'){value=value.replace(/"/g,'\\"');checkKeyNamespace(name);if(regPlaceHolder.test(value)){var parts=value.split(regPlaceHolder);var first=true;var fnArgs='';var usedArgs=[];for(var p=0;p<parts.length;p++){if(regPlaceHolder.test(parts[p])&&(usedArgs.length==0||usedArgs.indexOf(parts[p])==-1)){if(!first){fnArgs+=','}fnArgs+=parts[p].replace(regRepPlaceHolder,'v$1');usedArgs.push(parts[p]);first=false}}parsed+=name+'=function('+fnArgs+'){';var fnExpr='"'+value.replace(regRepPlaceHolder,'"+v$1+"')+'"';parsed+='return '+fnExpr+';};'}else{parsed+=name+'="'+value+'";'}}}}}eval(parsed)}function checkKeyNamespace(key){var regDot=/\./;if(regDot.test(key)){var fullname='';var names=key.split(/\./);for(var i=0;i<names.length;i++){if(i>0){fullname+='.'}fullname+=names[i];if(eval('typeof '+fullname+' == "undefined"')){eval(fullname+'={};')}}}}function getFiles(names){return(names&&names.constructor==Array)?names:[names]}function normaliseLanguageCode(lang){lang=lang.toLowerCase();if(lang.length>3){lang=lang.substring(0,3)+lang.substring(3).toUpperCase()}return lang}function unescapeUnicode(str){var codes=[];var code=parseInt(str.substr(2),16);if(code>=0&&code<Math.pow(2,16)){codes.push(code)}var unescaped='';for(var i=0;i<codes.length;++i){unescaped+=String.fromCharCode(codes[i])}return unescaped}var cbSplit;if(!cbSplit){cbSplit=function(str,separator,limit){if(Object.prototype.toString.call(separator)!=="[object RegExp]"){if(typeof cbSplit._nativeSplit=="undefined")return str.split(separator,limit);else return cbSplit._nativeSplit.call(str,separator,limit)}var output=[],lastLastIndex=0,flags=(separator.ignoreCase?"i":"")+(separator.multiline?"m":"")+(separator.sticky?"y":""),separator=RegExp(separator.source,flags+"g"),separator2,match,lastIndex,lastLength;str=str+"";if(!cbSplit._compliantExecNpcg){separator2=RegExp("^"+separator.source+"$(?!\\s)",flags)}if(limit===undefined||+limit<0){limit=Infinity}else{limit=Math.floor(+limit);if(!limit){return[]}}while(match=separator.exec(str)){lastIndex=match.index+match[0].length;if(lastIndex>lastLastIndex){output.push(str.slice(lastLastIndex,match.index));if(!cbSplit._compliantExecNpcg&&match.length>1){match[0].replace(separator2,function(){for(var i=1;i<arguments.length-2;i++){if(arguments[i]===undefined){match[i]=undefined}}})}if(match.length>1&&match.index<str.length){Array.prototype.push.apply(output,match.slice(1))}lastLength=match[0].length;lastLastIndex=lastIndex;if(output.length>=limit){break}}if(separator.lastIndex===match.index){separator.lastIndex++}}if(lastLastIndex===str.length){if(lastLength||!separator.test("")){output.push("")}}else{output.push(str.slice(lastLastIndex))}return output.length>limit?output.slice(0,limit):output};cbSplit._compliantExecNpcg=/()??/.exec("")[1]===undefined;cbSplit._nativeSplit=String.prototype.split}String.prototype.split=function(separator,limit){return cbSplit(this,separator,limit)}})(jQuery);

  
    // 设置语言类型： 默认为中文
    var i18nLanguage = "zh-CN";

    //cookie操作
    var getCookie = function(name, value, options) {
        if (typeof value != 'undefined') { // name and value given, set cookie
            options = options || {};
            if (value === null) {
                value = '';
                options.expires = -1;
            }
            var expires = '';
            if (options.expires && (typeof options.expires == 'number' || options.expires.toUTCString)) {
                var date;
                if (typeof options.expires == 'number') {
                    date = new Date();
                    date.setTime(date.getTime() + (options.expires * 24 * 60 * 60 * 1000));
                } else {
                    date = options.expires;
                }
                expires = '; expires=' + date.toUTCString(); // use expires attribute, max-age is not supported by IE
            }
            var path = options.path ? '; path=' + options.path : '';
            var domain = options.domain ? '; domain=' + options.domain : '';
            var s = [cookie, expires, path, domain, secure].join('');
            var secure = options.secure ? '; secure' : '';
            var c = [name, '=', encodeURIComponent(value)].join('');
            var samsite = ';SameSite=Strict'
            var cookie = [c, expires, path, domain, secure,samsite].join('')
            document.cookie = cookie;


        } else { // only name given, get cookie
            var cookieValue = null;
            if (document.cookie && document.cookie != '') {
                var cookies = document.cookie.split(';');
                for (var i = 0; i < cookies.length; i++) {
                    var cookie = jQuery.trim(cookies[i]);
                    // Does this cookie string begin with the name we want?
                    if (cookie.substring(0, name.length + 1) == (name + '=')) {
                        cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                        break;
                    }
                }
            }
            return cookieValue;
        }
    };

    //执行页面i18n方法
    var execI18n = function(){
        //获取一下资源文件名
        var optionEle = $("#i18n_pagename");
        if (optionEle.length < 1) {
            return false;
        };

        var sourceName = optionEle.attr('content');
        sourceName = sourceName.split('-');

        //首先获取用户浏览器设备之前选择过的语言类型
        if (getCookie("userLanguage")) {
            i18nLanguage = getCookie("userLanguage");
        }

        // 需要引入 i18n 文件
        if ($.i18n === undefined) {
            return false;
        };

        //这里需要进行i18n的翻译
        jQuery.i18n.properties({
            name : sourceName, //资源文件名称
            path : 'i18n/' + i18nLanguage +'/', //资源文件路径
            mode : 'map', //用Map的方式使用资源文件中的值
            language : i18nLanguage,
            callback : function() {
                //加载成功后设置显示内容

                //1.html()
                var insertEle = $(".i18n");
                insertEle.each(function() {
                    // 根据i18n元素的 name 获取内容写入
                    $(this).html($.i18n.prop($(this).attr('name')));
                });

                //2.自定义属性的值
                //跟上边的区别就是可以给html标签的任何属性可以赋值，
                var insertInputEle = $(".i18n-input");
                insertInputEle.each(function() {
                    var selectAttr = $(this).attr('selectattr');
                    if (!selectAttr) {
                        selectAttr = "value";
                    };
                    $(this).attr(selectAttr, $.i18n.prop($(this).attr('selectname')));
                });


                //一般情况下，我们标签里面的内容如果要做国际化，需要使用 $('#id').text($.i18n.prop('proName')); 来给标签赋值，
                // 现在问题来了，我们开发一个界面，有很多地方都需要去做国际化，我们总不能这样每一个页面每一个标签通过这种方式去赋值吧，
                // 这样工作量不是一点大，于是乎博主想，有没有一种比较好的通用的解决方案去给这些需要做国际化的标签统一赋值呢。
                // html的data属性似乎是一个不错的选择！它具有可读性强、可维护性强、兼容jquery的data()方法等优点。
                // 比如我们修改国际化组件的方法如下
                //<input class="typeahead" type="text" id="menu_search" data-i18n-placeholder = "searchPlaceholder"/>
                //<span data-i18n-text="setting"></span>

                //3.placeholderEle
                var placeholderEle =  $('[data-i18n-placeholder]');
                placeholderEle.each(function () {
                    //$(this).attr('placeholder', $.i18n.prop($(this).data('i18n-placeholder')));
                    var attr = $(this).attr('data-i18n-dyna');
                    if(attr){
                        //有占位符
                        var originalStr= $.i18n.prop($(this).data('i18n-placeholder'))
                        originalStr = originalStr.replace("{0}",attr);
                        $(this).attr('placeholder', originalStr);

                        var attrDyn = $(this).attr('data-i18n-msg');
                        if(attrDyn){
                            attrDyn = attrDyn.replace("{0}",attr);
                            $(this).attr('data-i18n-msg', attrDyn);
                        }
                    }else{
                        $(this).attr('placeholder', $.i18n.prop($(this).data('i18n-placeholder')));
                    }
                });

                //4.text()
                var textEle = $('[data-i18n-text]')
                textEle.each(function () {
                    $(this).text($.i18n.prop($(this).data('i18n-text')));
                });

                //5.val(xx)
                var valueEle = $('[data-i18n-value]');
                valueEle.each(function () {
                    $(this).val($.i18n.prop($(this).data('i18n-value')));
                });

            }
        });
    }

    /**
     * @param key
     */
    function prop(key) {
        return $.i18n.prop(key);
    }

    function propLocal(key) {
        // 设置语言类型： 默认为中文
        var i18nLanguage = "zh-CN";
        if (getCookie("userLanguage")) {
            i18nLanguage = getCookie("userLanguage");
        }

        if("en" == i18nLanguage){
            //英文
            switch(key){
                case 'resetPwd19':
                    return 'Please wait...';
                case 'abmLogin20':
                    return 'Resend';
            }

        }else{
            //中文
            switch(key){
                case 'resetPwd19':
                    return '请稍后…';
                case 'abmLogin20':
                    return '后重试';
            }
        }
    }

    function i18nById(id) {
        var  $tempContent = $('#'+id);
        if(!$tempContent){
            return
        }

        //1.html()
        var insertEle = $tempContent.find(".i18n");
        insertEle.each(function() {
            $(this).html($.i18n.prop($(this).attr('name')));
        });

        //2.自定义属性的值
        var insertInputEle = $tempContent.find(".i18n-input");
        insertInputEle.each(function() {
            var selectAttr = $(this).attr('selectattr');
            if (!selectAttr) {
                selectAttr = "value";
            };
            $(this).attr(selectAttr, $.i18n.prop($(this).attr('selectname')));
        });

        //3.placeholderEle
        var placeholderEle =  $tempContent.find('[data-i18n-placeholder]');
        placeholderEle.each(function () {
            var attr = $(this).attr('data-i18n-dyna');
            if(attr){
                //有占位符
                var originalStr= $.i18n.prop($(this).data('i18n-placeholder'))
                originalStr = originalStr.replace("{0}",attr);
                $(this).attr('placeholder', originalStr);

                var attrDyn = $(this).attr('data-i18n-msg');
                if(attrDyn){
                    attrDyn = attrDyn.replace("{0}",attr);
                    $(this).attr('data-i18n-msg', attrDyn);
                }
            }else{
                $(this).attr('placeholder', $.i18n.prop($(this).data('i18n-placeholder')));
            }
        });

        //4.text()
        var textEle = $tempContent.find('[data-i18n-text]')
        textEle.each(function () {
            $(this).text($.i18n.prop($(this).data('i18n-text')));
        });

        //5.val(xx)
        var valueEle = $tempContent.find('[data-i18n-value]');
        valueEle.each(function () {
            $(this).val($.i18n.prop($(this).data('i18n-value')));
        });
    }

    return {
        execI18n: execI18n,
        getCookie: getCookie,
        prop: prop,
        propLocal: propLocal,
        i18nById: i18nById
    }
});
