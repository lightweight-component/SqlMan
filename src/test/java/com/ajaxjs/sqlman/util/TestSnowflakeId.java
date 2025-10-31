package com.ajaxjs.sqlman.util;

import org.junit.jupiter.api.Test;

class TestSnowflakeId {
    @Test
    void test1() {
        for (int i = 0; i < 30; i++)
            System.out.println(SnowflakeId.get());
    }

    @Test
    void test2() {
        SnowflakeId worker = new SnowflakeId(1);

        for (int i = 0; i < 30; i++)
            System.out.println(worker.nextId());
    }

    @Test
    void test3() {
        for (int i = 0; i < 30; i++)
            System.out.println(SnowflakeId.get());
    }
}
