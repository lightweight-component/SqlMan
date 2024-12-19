package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.io.Resources;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.*;
import java.util.Base64;

/**
 * 证书和回调报文解密
 */
public class WeiXinCrypto {
    /**
     * AEAD_AES_256_GCM 解密
     *
     * @param aesKey         key 密钥，ApiV3Key，长度必须为32个字节
     * @param associatedData 相关数据
     * @param nonce          随机字符串
     * @param cipherText     密文
     * @return 解密后的文本
     */
    public static String aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText) {
        if (aesKey.length != 32)
            throw new IllegalArgumentException("无效的 ApiV3Key，长度必须为32个字节");

        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);

        return CommonUtil.doCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, aesKey, spec, cipherText, associatedData);
    }

    /**
     * 解密小程序提供的加密数据，返回包含手机号码等信息的 JSON 对象
     *
     * @param iv         前端给的
     * @param cipherText 前端给的，密文
     * @param sessionKey 后端申请返回
     * @return 解密后的文本
     */
    public static String aesDecryptPhone(String iv, String cipherText, String sessionKey) {
        IvParameterSpec spec = new IvParameterSpec(EncodeTools.base64Decode(iv));

        return CommonUtil.doCipher("AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, EncodeTools.base64Decode(sessionKey), spec, cipherText, null);
    }

    //----------------- RSA 加密、解密 -------------------
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    /**
     * 加密
     *
     * @param message     数据
     * @param certificate 证书
     * @return 加密后的文本
     */
    public static String encryptOAEP(String message, X509Certificate certificate) {
        byte[] bytes = CommonUtil.doCipher(TRANSFORMATION, Cipher.ENCRYPT_MODE, certificate.getPublicKey(), message.getBytes(StandardCharsets.UTF_8));

        return EncodeTools.base64EncodeToString(bytes);
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param privateKey 商户私钥
     * @return 解密后的文本
     */
    public static String decryptOAEP(String cipherText, PrivateKey privateKey) {
        byte[] cipherData = CommonUtil.doCipher(TRANSFORMATION, Cipher.DECRYPT_MODE, privateKey, Base64.getDecoder().decode(cipherText));

        return new String(cipherData, StandardCharsets.UTF_8);
    }

    /**
     * 使用 RSA 加密算法对消息进行加密
     *
     * @param message  待加密的消息
     * @param certPath 证书的路径，用于获取加密密钥
     * @return 加密后的消息
     * @throws UncheckedIOException 如果证书读取过程中发生 IO 错误，则抛出运行时异常
     */
    public static String rsaEncrypt(String message, String certPath) {
        try (InputStream in = Resources.getResource(certPath)) {// 从输入流中加载 X.509 证书
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(in);
            cert.checkValidity();

            return encryptOAEP(message, cert);
        } catch (IOException e) {
            throw new UncheckedIOException("IC 错误", e);
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("证书已过期", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("证书尚未生效", e);
        } catch (CertificateException e) {
            throw new RuntimeException("无效的证书", e);
        }
    }

    /**
     * 对签名数据进行签名。
     * <p>
     * 使用商户私钥对待签名串进行 SHA256 with RSA 签名，并对签名结果进行 Base64 编码得到签名值。
     *
     * @param privateKey 商户私钥
     * @param data       数据
     * @return 签名结果
     */
    public static String rsaSign(PrivateKey privateKey, byte[] data) {
        return EncodeTools.base64EncodeToString(RsaCrypto.sign("SHA256withRSA", privateKey, data));
    }
}
