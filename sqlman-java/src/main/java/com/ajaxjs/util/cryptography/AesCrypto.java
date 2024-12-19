package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.BytesHelper;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

/**
 * AES/DES/3DES/PBE 对称加密/解密
 * <a href="https://raw.githubusercontent.com/535404515/MYSQL-TOMCAT-MONITOR/master/nlpms-task-monitor/src/main/java/com/nuoli/mysqlprotect/util/EncryptUtil.java">...</a>
 */
public class AesCrypto {
    public static AesCrypto me;

    private AesCrypto() {
        // 单例
    }

    // 双重锁
    public static AesCrypto getInstance() {
        if (me == null)
            synchronized (AesCrypto.class) {
                if (me == null) me = new AesCrypto();
            }

        return me;
    }

    /**
     * 根据指定算法和安全随机数生成一个秘密密钥，并将其以 Base64 编码的字符串形式返回
     *
     * @param algorithmName 算法名称
     * @param secure        安全随机数
     * @return Base64 编码后的秘密密钥字符串
     */
    public static String getSecretKey(String algorithmName, SecureRandom secure) {
        byte[] encoded = getSecretKey(algorithmName, 0, secure).getEncoded();

        return EncodeTools.base64EncodeToString(encoded);
    }

    /**
     * 根据给定的键获取一个安全随机数生成器实例
     * 本方法旨在为特定的键生成一致且安全的随机数
     *
     * @param key 用于生成安全随机数的键
     * @return SecureRandom实例，使用SHA1PRNG算法和给定的键作为种子
     * <p>
     * 注意：此方法在处理 NoSuchAlgorithmException 时，选择将其包装为运行时异常抛出，
     * 这是因为 SecureRandom.getInstance 的调用预计不会失败，除非 JVM 实现存在问题
     * 因此，这样的异常应该被视为致命错误，而不是被静默处理或记录
     */
    private static SecureRandom getRandom(String key) {
        SecureRandom random;

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 SHA1PRNG", e);
        }

        random.setSeed(StrUtil.getUTF8_Bytes(key));

        return random;
    }

    /**
     * 获取对称加密用的 SecretKey
     *
     * @param algorithmName 加密算法
     * @param secure        可选的
     * @param keySize       可选的
     * @return SecretKey
     */
    static SecretKey getSecretKey(String algorithmName, int keySize, SecureRandom secure) {
        KeyGenerator kg;

        try {
            kg = KeyGenerator.getInstance(algorithmName);

            if (keySize != 0 && secure != null)
                kg.init(keySize, secure);
            else if (keySize == 0 && secure != null)
                kg.init(secure);
            else if (keySize != 0 && secure == null)
                kg.init(keySize);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + algorithmName, e);
        }

        return kg.generateKey();
    }

    /**
     * 使用 KeyGenerator 双向加密，DES/AES，注意这里转化为字符串的时候是将2进制转为16进制格式的字符串，不是直接转，因为会出错
     *
     * @param res           加密的原文
     * @param algorithmName 加密使用的算法名称
     * @param key           加密的秘钥
     * @param keySize       加密密钥的大小，如果为0，则使用默认大小
     * @param isEncrypt     是否加密，true 表示加密，false 表示解密
     * @return 加密或解密后的字符串
     */
    private static String keyGeneratorES(String res, String algorithmName, String key, int keySize, boolean isEncrypt) {
        SecretKey secretKey = getSecretKey(algorithmName, keySize, getRandom(key));
        SecretKeySpec sks = new SecretKeySpec(secretKey.getEncoded(), algorithmName);

        if (isEncrypt)
            return BytesHelper.bytesToHexStr(CommonUtil.doCipher(algorithmName, Cipher.ENCRYPT_MODE, sks, StrUtil.getUTF8_Bytes(res)));
        else
            return new String(CommonUtil.doCipher(algorithmName, Cipher.DECRYPT_MODE, sks, BytesHelper.parseHexStr2Byte(res)));
    }

    /**
     * 使用 DES 加密算法进行加密（可逆）
     *
     * @param res 需要加密的原文
     * @param key 秘钥
     * @return 加密结果
     */
    public String DES_encode(String res, String key) {
        return keyGeneratorES(res, "DES", key, 0, true);
    }

    /**
     * 对使用 DES 加密算法的密文进行解密（可逆）
     *
     * @param res 需要解密的密文
     * @param key 秘钥
     * @return 解密结果
     */
    public String DES_decode(String res, String key) {
        return keyGeneratorES(res, "DES", key, 0, false);
    }

    /**
     * 使用A ES 加密算法经行加密（可逆）
     *
     * @param res 需要加密的密文
     * @param key 秘钥
     * @return 加密结果
     */
    public String AES_encode(String res, String key) {
        return keyGeneratorES(res, "AES", key, 128, true);
    }

    /**
     * 对使用 AES 加密算法的密文进行解密
     *
     * @param res 需要解密的密文
     * @param key 秘钥
     * @return 解密结果
     */
    public String AES_decode(String res, String key) {
        return keyGeneratorES(res, "AES", key, 128, false);
    }

    ///////////////////////// --------------3DES----------------------------

    /**
     * 定义加密方式 支持以下任意一种算法
     *
     * <pre>
     * DES
     * DESede
     * Blowfish
     * </pre>
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String TripleDES_ALGORITHM = "DESede";

    /**
     * TripleDES(3DES) 加解密
     *
     * @param isEnc 是否加密
     * @param key   密钥
     * @param data  数据
     * @return 结果
     */
    private static byte[] initTripleDES(boolean isEnc, byte[] key, byte[] data) {
        // 根据给定的字节数组和算法构造一个密钥
        SecretKey desKey = new SecretKeySpec(key, TripleDES_ALGORITHM);
        int mode = isEnc ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        return CommonUtil.doCipher(TripleDES_ALGORITHM, mode, desKey, data, null);
    }

    /**
     * TripleDES(3DES) 加密
     *
     * @param key  加密密钥，长度为24字节
     * @param data 字节数组（根据给定的字节数组构造一个密钥）
     * @return 加密结果
     */
    public static byte[] encryptTripleDES(byte[] key, String data) {
        return initTripleDES(true, key, data.getBytes());
    }

    /**
     * TripleDES(3DES) 解密
     *
     * @param key  密钥
     * @param data 需要解密的数据
     * @return 解密结果
     */
    public static String decryptTripleDES(byte[] key, byte[] data) {
        return new String(initTripleDES(false, key, data));
    }

    ///////////////////////// --------------PBE----------------------------

    /**
     * 定义加密方式 支持以下任意一种算法
     *
     * <pre>
     * PBEWithMD5AndDES
     * PBEWithMD5AndTripleDES
     * PBEWithSHA1AndDESede
     * PBEWithSHA1AndRC2_40
     * </pre>
     */
    @SuppressWarnings("SpellCheckingInspection")
    private final static String KEY_PBE = "PBEWITHMD5andDES";

    /**
     * 初始化盐（salt）
     *
     * @return 盐（salt）
     */
    public static byte[] initSalt() {
        byte[] salt = new byte[8];
        new Random().nextBytes(salt);

        return salt;
    }

    private final static int SALT_COUNT = 100;

    /**
     * PBE 加解密
     *
     * @param isEnc 是否加密
     * @param key   密钥
     * @param data  数据
     * @return 结果
     */
    private static byte[] initPBE(boolean isEnc, String key, byte[] salt, byte[] data) {
        Key k;

        try {
            k = SecretKeyFactory.getInstance(KEY_PBE).generateSecret(new PBEKeySpec(key.toCharArray()));// 获取密钥，转换密钥
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + KEY_PBE, e);
        }

        int mode = isEnc ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        return CommonUtil.doCipher(KEY_PBE, mode, k, data, new PBEParameterSpec(salt, SALT_COUNT));
    }

    /**
     * PBE 加密
     *
     * @param key  加密密钥
     * @param salt 盐值
     * @param data 字节数组(根据给定的字节数组构造一个密钥。 )
     * @return 加密结果
     */
    public static byte[] encryptPBE(String key, byte[] salt, String data) {
        return initPBE(true, key, salt, data.getBytes());
    }

    /**
     * PBE 解密
     *
     * @param key  密钥
     * @param salt 盐值
     * @param data 需要解密的数据
     * @return 解密结果
     */
    public static String decryptPBE(String key, byte[] salt, byte[] data) {
        return new String(initPBE(false, key, salt, data));
    }
}