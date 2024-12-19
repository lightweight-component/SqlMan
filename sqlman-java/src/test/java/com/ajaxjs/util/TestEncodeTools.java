package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static com.ajaxjs.util.EncodeTools.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEncodeTools {
    @Test
    void testBase64() {
        String result = base64EncodeToString("abc");
        System.out.println(result);
        assertEquals("YWJj", result);

        result = base64EncodeToStringUtf8("中国人");
        System.out.println(result);
        assertEquals("5Lit5Zu95Lq6", result);

        assertEquals("中国人", base64DecodeToStringUtf8(base64EncodeToStringUtf8("中国人")));
    }

    @Test
     void testUrlChinese() {
		assertEquals("%E4%B8%AD%E5%9B%BD", urlEncode("中国"));
		assertEquals("中国", urlDecode("%E4%B8%AD%E5%9B%BD"));
    }
}
