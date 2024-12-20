package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.EncodeTools;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

/**
 * RSA 非对称加密/解密
 */
public class RsaCrypto {
    /**
     * 定义加密方式
     */
    private final static String KEY_RSA = "RSA";// "RSA/ECB/PKCS1Padding"

    /**
     * 定义签名算法
     */
    private final static String KEY_RSA_SIGNATURE = "MD5withRSA";

    /**
     * 定义公钥算法
     */
    private final static String KEY_RSA_PUBLIC_KEY = "RSAPublicKey";

    /**
     * 定义私钥算法
     */
    private final static String KEY_RSA_PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 初始化密钥
     * <p>
     * 注意这里是生成密钥对 KeyPair，再由密钥对获取公私钥
     *
     * @return 密钥对
     */
    public static Map<String, byte[]> init() {
        return getKeyPair(KEY_RSA, 1024, KEY_RSA_PUBLIC_KEY, KEY_RSA_PRIVATE_KEY);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param privateKey 私钥
     * @param data       加密数据
     * @return 数字签名
     */
    public static String sign(String privateKey, byte[] data) {
        byte[] sign = sign(KEY_RSA_SIGNATURE, (PrivateKey) restoreKey(false, privateKey), data);

        return EncodeTools.base64EncodeToString(sign);
    }

    public static byte[] sign(String algorithmName, PrivateKey privateKey, byte[] data) {
        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initSign(privateKey);
            signature.update(data);

            return signature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException("签名计算失败", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + KEY_RSA_SIGNATURE, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        }
    }

    /**
     * 校验数字签名
     *
     * @param data      加密数据
     * @param publicKey 公钥
     * @param sign      数字签名
     * @return 校验成功返回true，失败返回 false
     */
    public static boolean verify(byte[] data, String publicKey, String sign) {
        try {
            Signature signature = Signature.getInstance(KEY_RSA_SIGNATURE);
            signature.initVerify((PublicKey) restoreKey(true, publicKey));
            signature.update(data);

            return signature.verify(EncodeTools.base64Decode(sign));
        } catch (SignatureException e) {
            throw new RuntimeException("签名计算失败", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + KEY_RSA_SIGNATURE, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        }
    }

    // --------------------------------------------------------------------------------------

    /**
     * 还原公钥/私钥
     *
     * @param isPublic 是否公钥，反之私钥
     * @param key      公钥或私钥的字符串表示
     * @return 还原后的公钥或私钥对象，如果还原失败则返回null
     */
    private static Key restoreKey(boolean isPublic, String key) {
        byte[] bytes = EncodeTools.base64Decode(key);

        try {
            KeyFactory f = KeyFactory.getInstance(KEY_RSA);
            return isPublic ? f.generatePublic(new X509EncodedKeySpec(bytes)) : f.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + KEY_RSA, e);
        }
    }

    // ------------------------- PUBLIC KEY ------------------------

    /**
     * 处理公钥
     *
     * @param isEncrypt 是否加密(true)，反之为解密（false）
     * @param data      需要加密或解密的数据
     * @param key       公钥或私钥的字符串表示
     * @return 加密或解密后的字节数据
     */
    private static byte[] action(boolean isEncrypt, boolean isPublic, byte[] data, String key) {
        int mode = isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        return CommonUtil.doCipher(KEY_RSA, mode, restoreKey(isPublic, key), data, null);
    }

    /**
     * 公钥加密
     *
     * @param data 待加密数据
     * @param key  公钥
     * @return 加密后的字节数组
     */
    public static byte[] encryptByPublicKey(byte[] data, String key) {
        return action(true, true, data, key);
    }

    /**
     * 公钥解密
     *
     * @param data 加密数据
     * @param key  公钥
     * @return 解密后的字节数组
     */
    public static byte[] decryptByPublicKey(byte[] data, String key) {
        return action(false, true, data, key);
    }

    // ------------------------- PRIVATE KEY ------------------------

    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @param key  私钥
     * @return 加密后的字节数组
     */
    public static byte[] encryptByPrivateKey(byte[] data, String key) {
        return action(true, false, data, key);
    }

    /**
     * 私钥解密
     *
     * @param data 加密数据
     * @param key  私钥
     * @return 解密后的字节数组
     */
    public static byte[] decryptByPrivateKey(byte[] data, String key) {
        return action(false, false, data, key);
    }

    // ------------------------- GET KEY AS STRING ------------------------

    /**
     * 获取公钥
     *
     * @param map 个字符串到字节数组的映射，包含密钥对等信息
     * @return 公钥
     */
    public static String getPublicKey(Map<String, byte[]> map) {
        return getKey(KEY_RSA_PUBLIC_KEY, map);
    }

    /**
     * 获取私钥
     *
     * @param map 个字符串到字节数组的映射，包含密钥对等信息
     * @return 私钥
     */
    public static String getPrivateKey(Map<String, byte[]> map) {
        return getKey(KEY_RSA_PRIVATE_KEY, map);
    }

    // ------------- BASE ------------

    /**
     * 获取指定名称的密钥
     *
     * @param name 密钥名称
     * @param map  密钥映射表
     * @return 密钥的 Base64 编码
     */
    public static String getKey(String name, Map<String, byte[]> map) {
        byte[] bytes = map.get(name);

        return EncodeTools.base64EncodeToString(bytes);
    }

    /**
     * 生成一对密钥，并返回密钥对的 Base64 编码
     *
     * @param generator  密钥生成器
     * @param publicKey  公钥名称
     * @param privateKey 私钥名称
     * @return 密钥对的 Base64 编码
     */
    public static Map<String, byte[]> getKeyPair(KeyPairGenerator generator, String publicKey, String privateKey) {
        KeyPair keyPair = generator.generateKeyPair();

        return Map.of(publicKey, keyPair.getPublic().getEncoded(), privateKey, keyPair.getPrivate().getEncoded());
    }

    /**
     * 生成密钥对
     *
     * @param algorithmName 算法
     * @param keySize       密钥长度
     * @param publicKey     公钥文件路径
     * @param privateKey    私钥文件路径
     * @return 密钥对
     */
    public static Map<String, byte[]> getKeyPair(String algorithmName, int keySize, String publicKey, String privateKey) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName);
            generator.initialize(keySize);

            return getKeyPair(generator, publicKey, privateKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + algorithmName, e);
        }
    }
}