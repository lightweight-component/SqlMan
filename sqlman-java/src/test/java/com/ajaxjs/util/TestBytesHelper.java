package com.ajaxjs.util;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBytesHelper {
    @Test
    public void testParseHexStr2Byte() {
        byte[] bs = BytesHelper.parseHexStr2Byte("1A2B3C");
        assert bs != null;
        assertEquals(0x1A, bs[0]);
    }
}
