package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestRegexpUtils {
    @SuppressWarnings("SpellCheckingInspection")
    static String json = "{\"magic\":254,\"len\":28,\"sysid\":3,\"compid\":1,\"payload\":{\"pitchspeed\":-2.2399216E-4,"
            + "\"roll\":2.8490436,\"pitch\":0.9943852,\"rollspeed\":-5.5177254E-5,\"yawspeed\":0.0013602356,"
            + "\"time_boot_ms\":268235,\"yaw\":0.6807801},\"checksum\":33597,\"msgid\":30,\"seq\":176}";

//    @Test
//    public void testCallback() {
//        String j = RegExpUtils.kexuejishu(json);
//        System.out.println(j);
//        Map<String, Object> map = EntityConvert.json2map(j);
//        @SuppressWarnings("unchecked")
//        Map<String, Object> payload = (Map<String, Object>) map.get("payload");
//        System.out.println(payload.get("pitchspeed"));
//    }

    @Test
    public void testRegMatch() {
        assertEquals(RegExpUtils.regMatch("^a", "abc"), "a");// 匹配结果，只有匹配第一个
        assertEquals(RegExpUtils.regMatch("^a", "abc", 0), "a");// 可指定分组
        assertEquals(RegExpUtils.regMatch("^a(b)", "abc", 1), "b");
    }
}
