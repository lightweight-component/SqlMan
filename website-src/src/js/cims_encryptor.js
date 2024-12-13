/**
 * 前端js加密
 * @param encryptor 全局加解密方案配置
 * @constructor
 * @author chengjinyun
 * @date 2019-06-20
 */
function CimsEncryptor(encryptor) {
    var NATIONAL = "NATIONAL";

    /**
     * 公钥加密
     * @param publicKeyBase64 公钥
     * @param data 待加密数据
     */
    this.encrypt = function (publicKeyBase64, data) {
        var enc;
        if (!encryptor) {
            throw "oops! IAM encryptor_config is undefined,please check your code or contact the administrator complete the config.";
        }
        var publicKey = publicKeyBase64;
        if (encryptor === NATIONAL) {
            // 国密
            publicKey = publicKey.replace(/\ +/g, "");//去掉空格
            publicKey = publicKey.replace(/[ ]/g, "");//去掉空格
            publicKey = publicKey.replace(/[\r\n]/g, "");//去掉回车换行
            return '04' + sm2.doEncrypt(data, publicKey);
        } else {
            // 商密
            var encrypt = new JSEncrypt();
            publicKey = publicKey.replace(/\ +/g, "");//去掉空格
            publicKey = publicKey.replace(/[ ]/g, "");//去掉空格
            publicKey = publicKey.replace(/[\r\n]/g, "");//去掉回车换行
            encrypt.setPublicKey(publicKey);
            enc = encrypt.encrypt(data);
        }
        return enc;
    }

    this.hexStringToByte = function (str) {
        var pos = 0;
        var len = str.length;
        if (len % 2 !== 0) {
            return null;
        }
        len /= 2;
        var hexA = [];
        for (var i = 0; i < len; i++) {
            var s = str.substr(pos, 2);
            var v = parseInt(s, 16);
            hexA.push(v);
            pos += 2;
        }
        return hexA;
    }
}