package com.ajaxjs.data.util;


import org.junit.Test;

public class TestSnowflakeId {
    @Test
    public void test1() {
        for (int i = 0; i < 30; i++)
            System.out.println(SnowflakeId.get());
    }

    @Test
    public void test2() {
        SnowflakeId worker = new SnowflakeId(1);

        for (int i = 0; i < 30; i++)
            System.out.println(worker.nextId());
    }

    @Test
    public void test3() {
        for (int i = 0; i < 30; i++)
            System.out.println(SnowflakeId.get());
    }
}
