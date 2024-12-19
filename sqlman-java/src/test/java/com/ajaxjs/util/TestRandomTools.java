package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import static com.ajaxjs.util.RandomTools.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestRandomTools {
    @Test
    void testGenerateRandomNumberOld() {
        int randomNumber = generateRandomNumberOld(6);
        System.out.println(randomNumber);
        assertTrue(randomNumber > 100000);

        randomNumber = generateRandomNumber(6);
        System.out.println(randomNumber);
        assertTrue(randomNumber > 100000);
    }

    @Test
    void testGenerateRandomString() {
        String randomString = generateRandomString(6);
        System.out.println(randomString);

        assertNotNull(randomString);
    }
}
