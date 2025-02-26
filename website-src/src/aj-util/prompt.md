
你可以获取一个github代码仓库，把所有代码加载进去，进行学习，分析这些代码？Here is: https://github.com/lightweight-component/aj-util

What is the purpose of this repository?

Can u write the tutorials for `EncodeTools` and `MessageDigestHelper`? u can take the unit test code as some examples. Also give these tutorials in Chinese.

Can u write a tutorial for `XmlHelper`? u can take the unit test code as some examples. Also give this tutorial in Chinese.

Here is a Tutorial for `ConvertBasicValue`. Please write tutorial again for 'BytesHelper' like  `ConvertBasicValue` did.

--------------------

 # ConvertBasicValue Tutorial

This tutorial provides an overview of the `ConvertBasicValue` class, which is part of the `lightweight-component/aj-util` library. `ConvertBasicValue` offers a collection of utility methods for converting objects to basic Java types. This guide will cover the purpose of each method and provide usage examples.

## Introduction

The `ConvertBasicValue` class provides methods for safely converting objects to various basic Java types like `String`, `boolean`, `int`, `long`, `float`, `double`, `Date`, and `BigDecimal`. These methods handle null values and attempt to parse string representations where appropriate, providing a more robust alternative to direct casting.

## Methods

### 1. `basicCast(Object value, Class<T> clz)`

Safely casts an object to a specified class type.  This method leverages `basicConvert` to perform the initial conversion and then casts the result to the desired type.

*   **Parameters:**
    *   `value`: The object to convert.
    *   `clz`: The target class type.
*   **Returns:** The converted object of type `T`.

**Example:**

```java
Integer intValue = ConvertBasicValue.basicCast("123", Integer.class);
// intValue will be 123
```

### 2. `basicConvert(Object value, Class<?> clz)`

Converts an object to a specified class type. This method handles `null` values and performs type-specific conversions.

*   **Parameters:**
    *   `value`: The object to convert.
    *   `clz`: The target class type.
*   **Returns:** The converted object, or `null` if the input value is `null`.

This method contains the core logic for different type conversions. Let's look at some of the specific conversions it handles:

*   **String:** Converts the object to a string using `value.toString()`.
*   **boolean/Boolean:** Converts the object to a boolean using the `toBoolean()` method (explained below).
*   **int/Integer:** Converts the object to an integer using the `object2int()` method (explained below).
*   **long/Long:** Converts the object to a long using the `object2long()` method (explained below).
*   **float/Float:** Converts the object to a float using the `object2float()` method.
*   **double/Double:** Converts the object to a double using the `object2double()` method.
*   **Date:** Converts the object to a Date using the `DateHelper.object2Date()` method.
*   **BigDecimal:** Converts the object to a BigDecimal if the value is a String or Number.
*   **Array:** Converts the object to an array using the `toArray()` method.
*   **Enum:** Converts the object to an enum.

### 3. `toBoolean(Object value)`

Converts an object to a boolean. This method handles various input types, including strings, numbers, and booleans.

*   **Parameters:**
    *   `value`: The object to convert.
*   **Returns:** The boolean value of the object.

**Examples:**

```java
assertTrue(ConvertBasicValue.toBoolean(true));
assertTrue(ConvertBasicValue.toBoolean("true"));
assertTrue(ConvertBasicValue.toBoolean("1"));
assertFalse(ConvertBasicValue.toBoolean("false"));
assertFalse(ConvertBasicValue.toBoolean(0));
```

### 4. `object2int(Object value)`

Converts an object to an integer. This method handles `null` values and attempts to parse string representations.

*   **Parameters:**
    *   `value`: The object to convert.
*   **Returns:** The integer value of the object, or 0 if the input value is `null`.
*   **Throws:** `IllegalArgumentException` if the object cannot be converted to an integer.

**Examples:**

```java
assertEquals(0, ConvertBasicValue.object2int(null));
assertEquals(123, ConvertBasicValue.object2int("123"));
assertEquals(-456, ConvertBasicValue.object2int("-456"));
```

### 5. `object2long(Object value)`

Converts an object to a long. This method handles `null` values and attempts to parse string representations.

*   **Parameters:**
    *   `value`: The object to convert.
*   **Returns:** The long value of the object, or 0L if the input value is `null`.
*   **Throws:** `IllegalArgumentException` if the object cannot be converted to a long.

**Examples:**

```java
assertEquals(0L, ConvertBasicValue.object2long(null));
assertEquals(123L, ConvertBasicValue.object2long("123"));
assertEquals(-456L, ConvertBasicValue.object2long("-456"));
```

### 6. `object2double(Object value)`

Converts an object to a double. This method handles `null` values and attempts to parse string representations.

*   **Parameters:**
    *   `value`: The object to convert.
*   **Returns:** The double value of the object, or 0.0 if the input value is `null`.

**Examples:**

```java
assertEquals(0.0, ConvertBasicValue.object2double(null), 0.0001);
assertEquals(3.14, ConvertBasicValue.object2double("3.14"), 0.0001);
```
 

## Conclusion

The `ConvertBasicValue` class provides a useful set of utilities for safely converting objects to basic Java types. By using these methods, you can simplify your code and handle potential `null` values and parsing errors more gracefully.  Remember to consult the library's Javadoc for the most up-to-date information and additional methods.

```