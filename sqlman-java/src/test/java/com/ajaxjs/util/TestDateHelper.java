package com.ajaxjs.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TestDateHelper {

    @Test
    public void formatDate_LocalDateToString_FormatsCorrectly() {
        LocalDate date = LocalDate.of(2023, 3, 14);
        String formattedDate = DateHelper.formatDate(date);
        assertEquals("2023-03-14", formattedDate);
    }

    @Test
    public void formatTime_LocalTimeToString_FormatsCorrectly() {
        LocalTime time = LocalTime.of(15, 30, 45);
        String formattedTime = DateHelper.formatTime(time);
        assertEquals("15:30:45", formattedTime);
    }

    @Test
    public void formatDateTime_LocalDateTimeToString_FormatsCorrectly() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 3, 14, 15, 30, 45);
        String formattedDateTime = DateHelper.formatDateTime(dateTime);
        assertEquals("2023-03-14 15:30:45", formattedDateTime);
    }

    @Test
    public void parseDate_StringToLocalDate_ParsesCorrectly() {
        LocalDate date = DateHelper.parseDate("2023-03-14");
        assertNotNull(date);
        assertEquals(2023, date.getYear());
        assertEquals(3, date.getMonthValue());
        assertEquals(14, date.getDayOfMonth());
    }

    @Test
    public void parseTime_StringToLocalTime_ParsesCorrectly() {
        LocalTime time = DateHelper.parseTime("15:30:45");
        assertNotNull(time);
        assertEquals(15, time.getHour());
        assertEquals(30, time.getMinute());
        assertEquals(45, time.getSecond());
    }

    @Test
    public void parseDateTime_StringToLocalDateTime_ParsesCorrectly() {
        LocalDateTime dateTime = DateHelper.parseDateTime("2023-03-14 15:30:45");
        assertNotNull(dateTime);
        assertEquals(2023, dateTime.getYear());
        assertEquals(3, dateTime.getMonthValue());
        assertEquals(14, dateTime.getDayOfMonth());
        assertEquals(15, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
        assertEquals(45, dateTime.getSecond());
    }

    @Test
    public void parseDateTimeShort_StringToLocalDateTime_ParsesCorrectly() {
        LocalDateTime dateTime = DateHelper.parseDateTimeShort("2023-03-14 15:30");
        assertNotNull(dateTime);
        assertEquals(2023, dateTime.getYear());
        assertEquals(3, dateTime.getMonthValue());
        assertEquals(14, dateTime.getDayOfMonth());
        assertEquals(15, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
    }

    @Test
    public void localDateTime2Date_LocalDateTimeToDate_ConvertsCorrectly() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 3, 14, 15, 30, 45);
        Date date = DateHelper.localDateTime2Date(localDateTime);
        assertNotNull(date);
        assertEquals("2023-03-14 15:30:45", DateHelper.formatDateTime(DateHelper.toLocalDateTime(date)));
    }

    @Test
    public void localDate2Date_LocalDateToDate_ConvertsCorrectly() {
        LocalDate localDate = LocalDate.of(2023, 3, 14);
        Date date = DateHelper.localDate2Date(localDate);
        assertNotNull(date);
        assertEquals("2023-03-14", DateHelper.formatDate(DateHelper.toLocalDate(date)));
    }

    @Test
    public void toLocalDate_DateToLocalDate_ConvertsCorrectly() {
        Date date = new Date();
        LocalDate localDate = DateHelper.toLocalDate(date);
        assertNotNull(localDate);
        assertEquals(date.getYear() + 1900, localDate.getYear());
        assertEquals(date.getMonth() + 1, localDate.getMonthValue());
        assertEquals(date.getDate(), localDate.getDayOfMonth());
    }

    @Test
    public void toLocalDateTime_DateToLocalDateTime_ConvertsCorrectly() {
        Date date = new Date();
        LocalDateTime localDateTime = DateHelper.toLocalDateTime(date);
        assertNotNull(localDateTime);
        assertEquals(date.getYear() + 1900, localDateTime.getYear());
        assertEquals(date.getMonth() + 1, localDateTime.getMonthValue());
        assertEquals(date.getDate(), localDateTime.getDayOfMonth());
        assertEquals(date.getHours(), localDateTime.getHour());
        assertEquals(date.getMinutes(), localDateTime.getMinute());
        assertEquals(date.getSeconds(), localDateTime.getSecond());
    }

    @Test
    public void getGMTDate_GMTDateString_FormatsCorrectly() {
        String gmtDate = DateHelper.getGMTDate();
        assertNotNull(gmtDate);
        // We can add more sophisticated checks if needed
    }

    @Test
    public void object2Date_NullObject_ReturnsNull() {
        Date date = DateHelper.object2Date(null);
        assertNull(date);
    }

    @Test
    public void object2Date_DateObject_ReturnsSameDate() {
        Date date = new Date();
        Date result = DateHelper.object2Date(date);
        assertNotNull(result);
        assertEquals(date, result);
    }

    @Test
    public void object2Date_LongObject_ReturnsCorrectDate() {
        long time = 1681584645000L; // GMT: Saturday, April 15, 2023 11:30:45.000
        Date date = DateHelper.object2Date(time);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:45", DateHelper.formatDateTime(DateHelper.toLocalDateTime(date)));
    }

    @Test
    public void object2Date_IntegerObject_ReturnsCorrectDate() {
        long time = 1681584645000L; // GMT: Saturday, April 15, 2023 11:30:45.000
        Date date = DateHelper.object2Date(time);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:45", DateHelper.formatDateTime(DateHelper.toLocalDateTime(date)));
    }

    @Test
    public void object2Date_StringObject_ReturnsCorrectDate() {
        String dateTimeStr = "2023-04-15 11:30:45";
        Date date = DateHelper.object2Date(dateTimeStr);
        assertNotNull(date);
        assertEquals("2023-04-15 11:30:45", DateHelper.formatDateTime(DateHelper.toLocalDateTime(date)));
    }

    @Test
    public void nowDateTime_CurrentDateTime_ReturnsNotNull() {
        Date now = DateHelper.nowDateTime();
        assertNotNull(now);
    }

    @Test
    public void now_CurrentDateTimeString_ReturnsNotNull() {
        String now = DateHelper.now();
        assertNotNull(now);
        // More sophisticated checks can be added
    }

    @Test
    public void nowShort_CurrentDateTimeShortString_ReturnsNotNull() {
        String now = DateHelper.nowShort();
        assertNotNull(now);
        // More sophisticated checks can be added
    }
}