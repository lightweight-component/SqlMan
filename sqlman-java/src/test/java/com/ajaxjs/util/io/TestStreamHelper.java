package com.ajaxjs.util.io;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStreamHelper {
    private InputStream inputStream;
    private String testString;

    @BeforeEach
    public void setUp() {
        testString = "测试字符串";
        inputStream = new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void copyToString_WithDefaultCharset_ReturnsCorrectString() {
        String result = StreamHelper.copyToString(inputStream);
        assertEquals(testString + "\n", result);
    }

    @Test
    public void copyToString_WithSpecifiedCharset_ReturnsCorrectString() {
        String result = StreamHelper.copyToString(inputStream, StandardCharsets.UTF_8);
        assertEquals(testString + "\n", result);
    }

    @Test
    public void inputStream2Byte_ReturnsCorrectByteArray() {
        byte[] result = StreamHelper.inputStream2Byte(inputStream);
        assertEquals(testString, new String(result, StandardCharsets.UTF_8));
    }
}
