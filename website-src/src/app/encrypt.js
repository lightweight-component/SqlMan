define(['jsEncrypt', 'smCrypto'], function (JSEncrypt, smCrypto) {

    var rsa = {
        encrypt: function (publicKey, data) {
            var encrypt = new JSEncryptExports.JSEncrypt();
            publicKey = publicKey.replace(/\ +/g, "");//去掉空格
            publicKey = publicKey.replace(/[ ]/g, "");//去掉空格
            publicKey = publicKey.replace(/[\r\n]/g, "");//去掉回车换行
            encrypt.setPublicKey(publicKey);
            return encrypt.encrypt(data);
        },
        encryptWithSalt: function (publicKey, data, salt) {
            return this.encrypt(publicKey, data + "|" + salt);
        }
    }

    var sm = {
        encrypt: function (publicKey, data) {
            var encrypt = smCrypto.sm2;
            publicKey = publicKey.replace(/\ +/g, "");//去掉空格
            publicKey = publicKey.replace(/[ ]/g, "");//去掉空格
            publicKey = publicKey.replace(/[\r\n]/g, "");//去掉回车换行
            return '04' + encrypt.doEncrypt(data, publicKey);
        },
        encryptWithSalt: function (publicKey, data, salt) {
            return this.encrypt(publicKey, data + "|" + salt);
        }
    }

    function getEncoder(alg) {
        if (alg === "RSA") {
            return rsa;
        } else if (alg === "SM2") {
            return sm;
        }
        throw '无法处理的加密算法：' + alg;
    }

    return {getEncoder: getEncoder};
});