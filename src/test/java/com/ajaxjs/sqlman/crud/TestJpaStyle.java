package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.testcase.Address;
import com.ajaxjs.sqlman.testcase.AddressDAO;
import com.ajaxjs.sqlman.v1.JpaStyle;
import org.junit.jupiter.api.Test;

public class TestJpaStyle {

    static class Proxy implements AddressDAO {

        @Override
        public Address info(Integer integer) {
            return null;
        }
    }

    static class Proxy2 implements JpaStyle<Address, Integer> {

        @Override
        public Address info(Integer integer) {
            return null;
        }
    }


    @Test
    public void test() {
        Proxy p = new Proxy();
        Proxy2 p2 = new Proxy2();


    }
}
