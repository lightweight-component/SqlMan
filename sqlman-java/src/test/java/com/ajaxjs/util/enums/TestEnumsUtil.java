package com.ajaxjs.util.enums;

import org.junit.jupiter.api.Test;

public class TestEnumsUtil {
    public enum TypeEnum implements IEnum<String, String> {
        YES("YES", "是"),
        NO("NO", "否");

        private final String code;
        private final String msg;

        TypeEnum(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        @Override
        public String getCode() {
            return this.code;
        }

        @Override
        public String getMsg() {
            return this.msg;
        }
    }

    @Test
    public void test() {
        System.out.println(EnumsUtil.ofMsg("YES", TypeEnum.class));
    }
}
