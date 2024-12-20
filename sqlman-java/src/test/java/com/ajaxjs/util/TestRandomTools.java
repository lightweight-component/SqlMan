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

        randomNumber = generateRandomNumberOld();
        System.out.println(randomNumber);
        assertTrue(randomNumber > 100000);

        randomNumber = generateRandomNumberOld(6);
        System.out.println(randomNumber);
        assertTrue(randomNumber > 100000);

        randomNumber = generateRandomNumber();
        System.out.println(randomNumber);
        assertTrue(randomNumber > 100000);
    }

    @Test
    void testGenerateRandomString() {
        String randomString = generateRandomString(6);
        System.out.println(randomString);

        assertNotNull(randomString);

        randomString = generateRandomString();
        System.out.println(randomString);
        assertNotNull(randomString);

        randomString = generateRandomStringOld(6);
        System.out.println(randomString);

        assertNotNull(randomString);
    }

    @Test
    public void uuid_WhenRemoveIsTrue_ShouldNotContainHyphens() {
        String generatedUuid = uuid(true);
        assertFalse(generatedUuid.contains("-"), "Generated UUID should not contain hyphens when isRemove is true.");
    }

    @Test
    public void uuid_WhenRemoveIsFalse_ShouldContainHyphens() {
        String generatedUuid = uuid();
        System.out.println(generatedUuid);
        assertNotNull(generatedUuid);
        generatedUuid = uuid(false);
        assertTrue(generatedUuid.contains("-"), "Generated UUID should contain hyphens when isRemove is false.");
    }
}
