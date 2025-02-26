---
title: Quick Start
subTitle: 2025-02-23 by Frank Cheung
description: TODO
date: 2025-02-23
tags:
  - last one
layout: layouts/aj-util.njk
---

# Types
The `Types` class provides methods for retrieving generic type information, converting types, and handling parameterized types. These methods facilitate efficient and convenient manipulation of Java types at runtime.

## Methods

### 1. `getActualType(Type type)`

Retrieves the actual type arguments of a parameterized type.

*   **Parameters:**
    *   `type`: The type to retrieve the actual type arguments from.
*   **Returns:** An array of `Type` representing the actual type arguments, or `null` if the input type is not a parameterized type.

**Example:**

```java
Type type = new ParameterizedType() {
    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{String.class};
    }

    @Override
    public Type getRawType() {
        return List.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
};
Type[] actualType = Types.getActualType(type);
// actualType will contain String.class
```

### 2. `getGenericReturnType(Method method)`

Retrieves the actual type arguments of a method's return type.

*   **Parameters:**
    *   `method`: The method to retrieve the return type from.
*   **Returns:** An array of `Type` representing the actual type arguments of the return type.

**Example:**

```java
Method method = TestTypes.class.getMethods()[0];
Type[] actualType = Types.getGenericReturnType(method);
// actualType will contain the generic return type of the method
```

### 3. `getGenericFirstReturnType(Method method)`

Retrieves the first actual type argument of a method's return type and converts it to a `Class`.

*   **Parameters:**
    *   `method`: The method to retrieve the return type from.
*   **Returns:** The first actual type argument as a `Class`, or `null` if the return type is not parameterized.

**Example:**

```java
Method method = TestTypes.class.getMethods()[0];
Class<?> actualType = Types.getGenericFirstReturnType(method);
// actualType will be the first generic return type of the method as a Class
```

### 4. `getActualType(Class<?> clz)`

Retrieves the actual type arguments of a class's superclass.

*   **Parameters:**
    *   `clz`: The class to retrieve the actual type arguments from.
*   **Returns:** An array of `Type` representing the actual type arguments of the class's superclass.

**Example:**

```java
Type[] actualType = Types.getActualType(ArrayList.class);
// actualType will contain the generic type arguments of ArrayList's superclass
```

### 5. `getActualClass(Class<?> clz)`

Retrieves the first actual type argument of a class's superclass and converts it to a `Class`.

*   **Parameters:**
    *   `clz`: The class to retrieve the actual type argument from.
*   **Returns:** The first actual type argument as a `Class`.

**Example:**

```java
Class<?> actualClass = Types.getActualClass(ArrayList.class);
// actualClass will be the first generic type argument of ArrayList's superclass as a Class
```

### 6. `type2class(Type type)`

Converts a `Type` to a `Class`.

*   **Parameters:**
    *   `type`: The `Type` to convert.
*   **Returns:** The `Class` representation of the `Type`, or `null` if the `Type` is not a `Class`.

**Example:**

```java
Type type = String.class;
Class<?> actualClass = Types.type2class(type);
// actualClass will be String.class
```