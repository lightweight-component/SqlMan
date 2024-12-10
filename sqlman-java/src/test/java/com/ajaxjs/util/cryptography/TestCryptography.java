package com.ajaxjs.util.cryptography;


import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestCryptography {
    String key = "abc";
    String word = "123";

    @Test
    public void testAES() {
        String encWord = AesCrypto.getInstance().AES_encode(word, key);
        assertEquals(word, AesCrypto.getInstance().AES_decode(encWord, key));
    }

    @Test
    public void testDES() {
        String encWord = AesCrypto.getInstance().DES_encode(word, key);
        assertEquals(word, AesCrypto.getInstance().DES_decode(encWord, key));
    }

    @SuppressWarnings("restriction")
    @Test
    public void test3DES() {
        // 添加新安全算法,如果用JCE就要把它添加进去
        // 这里addProvider方法是增加一个新的加密算法提供者(个人理解没有找到好的答案,求补充)
//		Security.addProvider(new com.sun.crypto.provider.SunJCE());
        // byte数组(用来生成密钥的)
        final byte[] keyBytes = {0x11, 0x22, 0x4F, 0x58, (byte) 0x88, 0x10, 0x40, 0x38, 0x28, 0x25, 0x79, 0x51, (byte) 0xCB, (byte) 0xDD, 0x55, 0x66, 0x77, 0x29, 0x74,
                (byte) 0x98, 0x30, 0x40, 0x36, (byte) 0xE2};
        String word = "This is a 3DES test. 测试";

        byte[] encoded = AesCrypto.encryptTripleDES(keyBytes, word);

        assertEquals(word, AesCrypto.decryptTripleDES(keyBytes, encoded));
    }

    @Test
    public void testPBE() {
        // 加密前的原文
        String word = "hello world !!!";
        // 口令
        String key = "qwert";

        // 初始化盐
        byte[] salt = AesCrypto.initSalt();
        byte[] encData = AesCrypto.encryptPBE(key, salt, word);
        assertEquals(word, AesCrypto.decryptPBE(key, salt, encData));
    }

    @Test
    public void testRSA() {
        // 生成公钥私钥
        Map<String, byte[]> map = RsaCrypto.init();
        String publicKey = RsaCrypto.getPublicKey(map), privateKey = RsaCrypto.getPrivateKey(map);

//		System.out.println("公钥: \n\r" + publicKey);
//		System.out.println("私钥： \n\r" + privateKey);
//		System.out.println("公钥加密--------私钥解密");

        String word = "你好，世界！";

        byte[] encWord = RsaCrypto.encryptByPublicKey(word.getBytes(), publicKey);
        String decWord = new String(RsaCrypto.decryptByPrivateKey(encWord, privateKey));
//		System.out.println("加密前: " + word + "\n\r" + "解密后: " + decWord);
        assertEquals(word, decWord);

//		System.out.println("私钥加密--------公钥解密");

        String english = "Hello, World!";
        byte[] encEnglish = RsaCrypto.encryptByPrivateKey(english.getBytes(), privateKey);
        String decEnglish = new String(RsaCrypto.decryptByPublicKey(encEnglish, publicKey));
//		System.out.println("加密前: " + english + "\n\r" + "解密后: " + decEnglish);

        assertEquals(english, decEnglish);
//		System.out.println("私钥签名——公钥验证签名");

        // 产生签名
        String sign = RsaCrypto.sign(privateKey, encEnglish);
//		System.out.println("签名:\r" + sign);
        // 验证签名
        assertTrue(RsaCrypto.verify(encEnglish, publicKey, sign));
    }
}