package com.ajaxjs.util;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class EasyLoggerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        // Redirect System.out to a ByteArrayOutputStream for capturing output
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void tearDown() {
        // Restore original System.out after tests
        System.setOut(originalOut);
    }

    @Test
    public void info_ValidFormatAndArgs_CorrectOutput() {
        String testMessage = "Test message: {}";
        String expectedMessage = ".*Test message: testValue.*";

        EasyLogger.info(testMessage, "testValue");

        String output = outContent.toString();
        System.out.println(output);
        assertEquals(expectedMessage, output);
        assertTrue(output.matches(expectedMessage));
    }

    @Test
    public void warn_ValidFormatAndArgs_CorrectOutput() {
        String testMessage = "Test warning: {}";
        String expectedMessage = "(?m)\\w+";

        EasyLogger.warn(testMessage, "testWarningValue");

        String output = outContent.toString();

        boolean test = output.matches("\\w");
        System.err.println(expectedMessage);
        System.err.println(output.toCharArray()[9]);
//        assertEquals(expectedMessage, output);
//        assertTrue(test);
        String e= "gerTest#50\u001B[0m : Test warning: testWarningValue";
        assertTrue(String.valueOf(output.toCharArray()).matches(expectedMessage));
    }

    public static void main(String[] args) {
        String e= "gerTest#50\u001B[0m : Test warning: testWarningValue";
        boolean test = e.matches(".*Test warning: testWarningValue");
        System.err.println(test);
    }
    @Test
    public void error_ValidFormatAndArgs_CorrectOutput() {
        String testMessage = "Test error: {}";
        String expectedMessage = ".*Test error: testErrorValue.*";

        EasyLogger.error(testMessage, "testErrorValue");

        String output = outContent.toString();
        assertTrue(output.matches("T"));
    }

    @Test
    public void info_NullArgs_EmptyArgsArray() {
        String testMessage = "Test message with null args:{}";

        EasyLogger.info(testMessage, (Object[]) null);

        String output = outContent.toString();
        assertTrue(output.contains("Test message with null args:"));
    }

    @Test
    public void warn_IndexOutOfBoundsException_HandlesGracefully() {
        String testMessage = "Test warning with missing arg:{} {}";

        EasyLogger.warn(testMessage, "test");

        String output = outContent.toString();
        assertTrue(output.contains("Test warning with missing arg:test null"));
    }

    @Test
    public void error_IndexOutOfBoundsException_HandlesGracefully() {
        String testMessage = "Test error with missing arg:{} {}";

        EasyLogger.error(testMessage, "test");

        String output = outContent.toString();
        assertTrue(output.contains("Test error with missing arg:test null"));
    }
}
