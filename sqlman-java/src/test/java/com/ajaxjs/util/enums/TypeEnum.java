package com.ajaxjs.util.enums;

/**
 * TypeEnum
 */
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
