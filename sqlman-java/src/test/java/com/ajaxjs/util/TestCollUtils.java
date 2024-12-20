package com.ajaxjs.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCollUtils {
    private List<String> nonEmptyList;
    private List<String> emptyList;
    private List<String> nullList;

    @BeforeEach
    public void setUp() {
        nonEmptyList = new ArrayList<>();
        nonEmptyList.add("A");
        nonEmptyList.add("B");

        emptyList = new ArrayList<>();

        nullList = null;
    }

    @Test
    public void getList_NullOrEmptyList_ReturnsEmptyList() {
        List<String> result = CollUtils.getList(nullList);
        assertTrue(result.isEmpty(), "Expected an empty list when input is null");

        result = CollUtils.getList(emptyList);
        assertTrue(result.isEmpty(), "Expected an empty list when input is empty");
    }

    @Test
    public void getList_NonEmptyList_ReturnsSameList() {
        List<String> result = CollUtils.getList(nonEmptyList);
        assertEquals(nonEmptyList, result, "Expected the same non-empty list");
    }

    @Test
    public void printArray_NullArray_LogsDebugMessage() {
        // Since this method only logs, we can check if the logs were generated as expected
        // However, in this test case, we will just check if no exception is thrown
        assertDoesNotThrow(() -> CollUtils.printArray(null), "Printing null array should not throw an exception");
    }

    @Test
    public void printArray_EmptyArray_LogsDebugMessage() {
        // Since this method only logs, we will just check if no exception is thrown
        assertDoesNotThrow(() -> CollUtils.printArray(new String[0]), "Printing empty array should not throw an exception");
    }

    @Test
    public void printArray_NonEmptyArray_LogsArrayElements() {
        // Since this method only logs, we will just check if no exception is thrown
        assertDoesNotThrow(() -> CollUtils.printArray(new String[]{"A", "B"}), "Printing non-empty array should not throw an exception");
    }

    @Test
    public void concat_TwoArrays_ReturnsConcatenatedArray() {
        String[] first = {"A", "B"};
        String[] second = {"C", "D"};
        String[] expected = {"A", "B", "C", "D"};

        String[] result = CollUtils.concat(first, second);
        assertArrayEquals(expected, result, "Expected concatenated array");
    }

    @Test
    public void addAll_MultipleArrays_ReturnsConcatenatedArray() {
        Integer[] first = {1, 2};
        Integer[] second = {3, 4};
        Integer[] third = {5, 6};
        Integer[] expected = {1, 2, 3, 4, 5, 6};

        Integer[] result = CollUtils.addAll(first, second, third);
        assertArrayEquals(expected, result, "Expected concatenated array");
    }

    @Test
    public void newArray_CreatesNewArray() {
        Integer[] array = CollUtils.newArray(Integer.class, 5);
        assertNotNull(array, "Expected a new array");
        assertEquals(5, array.length, "Expected array of length 5");
    }

    @Test
    public void arrayList_FromArray_ReturnsArrayList() {
        String[] elements = {"A", "B", "C"};
        List<String> expected = new ArrayList<>();
        expected.add("A");
        expected.add("B");
        expected.add("C");

        List<String> result = CollUtils.arrayList(elements);
        assertEquals(expected, result, "Expected ArrayList with same elements");
    }

    @Test
    public void findOne_FirstMatchingElement_ReturnsElement() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");

        String result = CollUtils.findOne(list, s -> s.startsWith("B"));
        assertEquals("Banana", result, "Expected 'Banana' as the first matching element");
    }

    @Test
    public void findOne_NoMatchingElement_ReturnsNull() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");

        String result = CollUtils.findOne(list, s -> s.startsWith("D"));
        assertNull(result, "Expected null as there is no matching element");
    }

    @Test
    public void findSome_MatchingElements_ReturnsList() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");
        list.add("Apricot");

        List<String> result = CollUtils.findSome(list, s -> s.startsWith("A"));
        assertNotNull(result, "Expected a non-null list");
        assertTrue(result.contains("Apple"), "Expected list to contain 'Apple'");
        assertTrue(result.contains("Apricot"), "Expected list to contain 'Apricot'");
    }

    @Test
    public void findSome_NoMatchingElements_ReturnsEmptyList() {
        List<String> list = new ArrayList<>();
        list.add("Apple");
        list.add("Banana");
        list.add("Cherry");

        List<String> result = CollUtils.findSome(list, s -> s.startsWith("D"));
        assertNotNull(result, "Expected a non-null list");
        assertTrue(result.isEmpty(), "Expected empty list as there are no matching elements");
    }

    @Test
    public void intList2Arr_ConvertsListToIntArray() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        int[] expected = {1, 2, 3};
        int[] result = CollUtils.intList2Arr(list);
        assertArrayEquals(expected, result, "Expected int array with same elements");
    }

    @Test
    public void stringArr2intArr_ConvertsStringToIntArray() {
        String input = "1,2,3";
        int[] expected = {1, 2, 3};
        int[] result = CollUtils.stringArr2intArr(input);
        assertArrayEquals(expected, result, "Expected int array with parsed elements");
    }
}
