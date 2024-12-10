package com.ajaxjs.util.enums;

import org.junit.jupiter.api.Test;

public class TestEnumsUtil {
    @Test
    public void test() {
        System.out.println(EnumsUtil.ofMsg("YES", TypeEnum.class));
    }
}
