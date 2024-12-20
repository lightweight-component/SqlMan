package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.BytesHelper;
import com.ajaxjs.util.CollUtils;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.io.Resources;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class CommonUtil {
    /**
     * 进行加密或解密，三步走
     *
     * @param algorithmName 选择的算法
     * @param mode          是解密模式还是加密模式？
     * @param key           密钥
     * @param data          输入的内容
     * @return 结果
     */
    public static byte[] doCipher(String algorithmName, int mode, Key key, byte[] data) {
        return doCipher(algorithmName, mode, key, data, null);
    }

    /**
     * 进行加密或解密，三步走
     *
     * @param algorithmName 选择的算法
     * @param mode          是解密模式还是加密模式？
     * @param key           密钥
     * @param data          输入的内容
     * @param spec          参数，可选的
     * @return 结果
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static byte[] doCipher(String algorithmName, int mode, Key key, byte[] data, AlgorithmParameterSpec spec) {
        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            /*
             * 为了防止解密时报 javax.crypto.IllegalBlockSizeException: Input length must be
             * multiple of 8 when decrypting with padded cipher 异常， 不能把加密后的字节数组直接转换成字符串
             */
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("当前 Java 环境不支持 RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("加密原串的长度不能超过214字节", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("无效的算法参数", e);
        }
    }

    public static String doCipher(String algorithmName, int mode, byte[] keyData, AlgorithmParameterSpec spec, String cipherText, byte[] associatedData) {
        SecretKeySpec key = new SecretKeySpec(keyData, "AES");

        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            if (associatedData != null)
                cipher.updateAAD(associatedData);

            return StrUtil.byte2String(cipher.doFinal(EncodeTools.base64Decode(cipherText)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("当前 Java 环境不支持 " + algorithmName, e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("加密原串的长度不能超过214字节", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("无效的算法参数", e);
        }
    }

    /**
     * 使用异或操作符对字符串进行简单加密。
     * 该方法通过将字符串的每个字节与一个固定密钥的哈希值进行异或操作，以达到加密的目的。加密后的字节数组再通过工具方法转换为十六进制字符串返回。
     * 此方法适用于对数据进行简单的安全性保护，但并非安全的加密手段，适用于对安全性要求不高的场景。
     *
     * @param res 需要加密的字符串。
     * @param key 加密使用的密钥。注意，该密钥将被转换为哈希值后用于加密操作。
     * @return 返回加密后的十六进制字符串。
     */
    public static String XOR_encode(String res, String key) {
        byte[] bs = res.getBytes();

        for (int i = 0; i < bs.length; i++)
            bs[i] = (byte) (bs[i] ^ key.hashCode());

        return BytesHelper.bytesToHexStr(bs);
    }

    /**
     * 使用异或进行解密
     *
     * @param res 需要解密的密文
     * @param key 秘钥
     * @return 结果
     */
    public static String XOR_decode(String res, String key) {
        byte[] bs = BytesHelper.parseHexStr2Byte(res);

        for (int i = 0; i < Objects.requireNonNull(bs).length; i++)
            bs[i] = (byte) (bs[i] ^ key.hashCode());

        return new String(bs);
    }

    /**
     * 使用异或操作对结果进行加密或解密。
     * 异或操作的特点是，如果对相同的两个值进行异或操作，结果是 0；而且异或操作是可逆的，即 a ^ b ^ b = a。
     * 这里使用字符串的哈希值作为密钥，因为字符串的哈希值在大多数情况下是不同的，这样可以增加解密的难度。
     * 但是需要注意，由于哈希冲突的可能性，不同字符串的哈希值可能会相同，这可能会导致解密错误。
     *
     * @param res 需要加密或解密的整数结果。
     * @param key 用于加密或解密的字符串密钥。
     * @return 经过异或操作后的加密或解密结果。
     */
    public static int XOR(int res, String key) {
        return res ^ key.hashCode();
    }

    /* ------------------------------------私钥文件工具类------------------------------------------*/

    private static String privateKeyContent;

    /**
     * 从 classpath 上指定私钥文件的路径
     *
     * @param privateKeyPath 私钥文件的路径
     * @return 私钥文件 PrivateKey
     */
    public static PrivateKey loadPrivateKeyByPath(String privateKeyPath) {
        if (privateKeyContent == null)
            privateKeyContent = Resources.getResourceText(privateKeyPath); // cache it

        return loadPrivateKey(privateKeyContent);
    }

    /**
     * 转换为 Java 里面的 PrivateKey 对象
     *
     * @param privateKey 私钥内容
     * @return 私钥对象
     */
    public static PrivateKey loadPrivateKey(String privateKey) {
        Objects.requireNonNull(privateKey, "没有私钥内容");
        privateKey = privateKey.replace("-----BEGIN PRIVATE KEY-----", StrUtil.EMPTY_STRING)
                .replace("-----END PRIVATE KEY-----", StrUtil.EMPTY_STRING)
                .replaceAll("\\s+", StrUtil.EMPTY_STRING);

        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前 Java 环境不支持 RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式", e);
        }
    }

    /**
     * 从输入流中加载私钥
     * 该方法首先将输入流中的字节读取到 ByteArrayOutputStream 中，然后将其转换为字符串形式的私钥，
     * 最后调用另一方法loadPrivateKey(String)来解析并返回私钥对象
     *
     * @param inputStream 包含私钥信息的输入流
     * @return 解析后的PrivateKey对象
     * @throws IllegalArgumentException 如果输入流中的数据无法被正确读取或解析为私钥，则抛出此异常
     */
    public static PrivateKey loadPrivateKey(InputStream inputStream) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
        byte[] buffer = new byte[1024];
        String privateKey;

        try {
            for (int length; (length = inputStream.read(buffer)) != -1; )
                os.write(buffer, 0, length);

            privateKey = os.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("无效的密钥", e);
        }

        return loadPrivateKey(privateKey);
    }

    /**
     * 反序列化证书并解密
     *
     * @param apiV3Key APIv3 密钥
     * @param pMap     下载证书的请求返回体
     * @return 证书 list
     */
    @SuppressWarnings("unchecked")
    public static Map<BigInteger, X509Certificate> deserializeToCerts(String apiV3Key, Map<String, Object> pMap) {
        byte[] apiV3KeyByte = StrUtil.getUTF8_Bytes(apiV3Key);
        List<Map<String, Object>> list = (List<Map<String, Object>>) pMap.get("data");
        Map<BigInteger, X509Certificate> newCertList = new HashMap<>();

        if (!CollUtils.isEmpty(list)) {
            for (Map<String, Object> map : list) {
                Map<String, Object> certificate = (Map<String, Object>) map.get("encrypt_certificate");

                // 解密
                String cert = WeiXinCrypto.aesDecryptToString(apiV3KeyByte, StrUtil.getUTF8_Bytes(remove(certificate.get("associated_data"))), StrUtil.getUTF8_Bytes(remove(certificate.get("nonce"))),
                        remove(certificate.get("ciphertext")));

                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X509");
                    X509Certificate x509Cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
                    x509Cert.checkValidity();
                    newCertList.put(x509Cert.getSerialNumber(), x509Cert);
                } catch (CertificateExpiredException | CertificateNotYetValidException ignored) {
                } catch (CertificateException e) {
                    throw new RuntimeException("当证书过期或尚未生效时", e);
                }
            }
        }

        return newCertList;
    }

    private static String remove(Object v) {
        return v.toString().replace("\"", "");
    }
}
