package com.ajaxjs.sqlman.util;

import java.util.List;
import java.util.Map;

public interface JsonUtil {
    /**
     * Converts a JSON string to an object of the specified type.
     *
     * @param json      The JSON string representing the data to be converted.
     * @param valueType The class type of the target object.
     * @param <T>       The generic type parameter indicating the type of the returned object.
     * @return The converted object of type T.
     * @throws RuntimeException If the JSON string cannot be converted to the target type.
     */
    <T> T fromJson(String json, Class<T> valueType);

    /**
     * json string converts to map
     * This method is used to convert a JSON string into a Map object with String as the key type and Object as the value type.
     * It simplifies the process of accessing and manipulating JSON data by converting it into a Map format.
     *
     * @param jsonStr The JSON string to be converted.
     * @return Returns a Map object containing the key-value pairs converted from the JSON string.
     */
    Map<String, Object> json2map(String jsonStr);

    /**
     * JSON array string converts to list with Java Bean
     *
     * @param jsonArrayStr The JSON array string, representing a list of objects
     * @param clazz        The class type of the list elements, used to specify the type of Java Bean
     * @param <T>          Generic type parameter, indicating the type of elements in the list
     * @return Returns a list of Java Bean objects converted from the JSON array string
     */
    <T> List<T> json2list(String jsonArrayStr, Class<T> clazz);

    List<Map<String, Object>> json2mapList(String jsonArrayStr);

    JsonUtil INSTANCE = null;
}
