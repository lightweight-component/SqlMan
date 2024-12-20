package com.ajaxjs.util.cryptography;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AesWeiXinUtilTest {
    private final byte[] validKey = new byte[32];// 32 bytes for AES-256
    private final byte[] validNonce = new byte[12];// 12 bytes for GCM
    private final String validCiphertext = "yourValidCiphertext"; // Base64 encoded ciphertext
    private final byte[] associatedData = new byte[0];// Typically associated data would be included here

    @Test
    public void decryptToString_ValidInputs_ShouldDecryptSuccessfully() {
        String decryptedText = WeiXinCrypto.aesDecryptToString(validKey, associatedData, validNonce, validCiphertext);
        // Add assertions to validate the decrypted text
        System.out.println(decryptedText);
    }

    @Test
    public void decryptToString_InvalidKey_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> WeiXinCrypto.aesDecryptToString(new byte[32], associatedData, validNonce, validCiphertext));
    }

    @Test
    public void decryptToString_InvalidCiphertext_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> WeiXinCrypto.aesDecryptToString(validKey, associatedData, validNonce, "invalidCiphertext"));
    }

    @Test
    public void decryptPhone_ValidInputs_ShouldDecryptSuccessfully() {
        String sessionKey = "yourSessionKey";
        String iv = "yourIV";
        String ciphertext = "yourCiphertext";
        String decryptedText = WeiXinCrypto.aesDecryptPhone(iv, ciphertext, sessionKey);
        // Add assertions to validate the decrypted text
        System.out.println(decryptedText);
    }

    @Test
    public void decryptPhone_InvalidSessionKey_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> WeiXinCrypto.aesDecryptPhone("iv", "ciphertext", "invalidSessionKey"));
    }

    @Test
    public void decryptPhone_InvalidCiphertext_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> WeiXinCrypto.aesDecryptPhone("iv", "invalidCiphertext", "sessionKey"));
    }
}