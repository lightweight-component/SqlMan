package com.ajaxjs.data.util;

import com.ajaxjs.util.DateHelper;
import com.ajaxjs.util.ListUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 尝试转换目标类型，注意并不是所有的类型都可以进行转换
 */
@Data
@Slf4j
public class ConvertBasicValue {
    /**
     * 字符串转换为数组时候，所使用的分隔符，默认为 ,
     */
    private static String diver = ",";

    /**
     * 安全地将对象转换为指定的类类型。
     * <p>
     * 该方法提供了一种类型安全的方式将对象转换为目标类类型T。它首先调用basicConvert方法进行实际的转换操作，
     * 然后通过泛型的类型擦除机制，将转换后的对象强制转换为T类型并返回。使用此方法可以避免直接的强制类型转换，
     * 从而减少因类型不匹配导致的ClassCastException的风险。
     * <p>
     * 指定 Class 类型的转换，少了强类型转换的步骤
     *
     * @param value 需要转换的对象。
     * @param clz   目标类类型，用于指定转换后的类型。
     * @param <T>   泛型参数，表示目标类类型。
     * @return 转换后的对象，类型为T。
     */
    @SuppressWarnings("unchecked")
    public static <T> T basicCast(Object value, Class<T> clz) {
        return (T) basicConvert(value, clz);
    }

    /**
     * 根据送入的类型作适当转换
     *
     * @param value 送入的值
     * @param clz   期待的类型
     * @return 已经转换类型的值
     */
    public static Object basicConvert(Object value, Class<?> clz) {
        if (value == null)
            return null;

        if (clz == null) {
            log.warn("Clz can not be null");
            return null;
        }

        if (clz == String.class)
            return value.toString();
        else if (clz == boolean.class || clz == Boolean.class)
            return toBoolean(value);
        else if (clz == int.class || clz == Integer.class)
            return object2int(value);
        else if (clz == long.class || clz == Long.class)
            return object2long(value);
        else if (clz == float.class || clz == Float.class)
            return object2float(value);
        else if (clz == double.class || clz == Double.class)
            return object2double(value);
        else if (clz == Date.class)
            return DateHelper.object2Date(value);
        else if (clz == BigDecimal.class) {
            if (value instanceof String || value instanceof Number)
                return new BigDecimal(value.toString());
            else
                log.warn("value: [{}] type:[{}] can not be converted to BigDecimal", value, value.getClass().getName());
        } else if (clz.isArray())
            return toArray(value, clz);
        else if (clz.isEnum()) {
            Object[] enumConstants = clz.getEnumConstants();
            boolean isNumber = value instanceof Integer;

            for (Object obj : enumConstants) { // value 跟枚举类型比较
                if (isNumber) {
                    Enum<?> e = ((Enum<?>) obj);

                    if (e.ordinal() == ((Integer) value))
                        return e.ordinal();
                } else if (obj.toString().equals(value))
                    return obj;
            }
        }

        return null;
    }

    /**
     * 转换到数组
     */
    @SuppressWarnings("unchecked")
    static Object toArray(Object value, Class<?> clz) {
        if (clz == String[].class) {
            // 复数
            if (value instanceof ArrayList) {
                ArrayList<String> list = (ArrayList<String>) value;
                return list.toArray(new String[0]);
            } else if (value instanceof String)
                return ((String) value).split(diver);// 用于数组的分隔符
            else
                log.warn("String[] value: [{}] type:[{}] can not be converted to [{}]", value, value.getClass().getName(), clz);
        } else if (clz == int[].class /*|| clz == Integer[].class*/) {
            // 复数
            if (value instanceof String)
                return ListUtils.stringArr2intArr((String) value);
            else if (value instanceof List)
                return ListUtils.intList2Arr((List<Integer>) value);
            else
                log.warn("int[] value: [{}] type:[{}] can not be converted to [{}]", value, value.getClass().getName(), clz);
        }

        System.err.println("More array type is on the way.");

        return null;
    }

    /**
     * true/1、 字符串 true/1/yes/on 被视为 true 返回； false/0/null、字符串 false/0/no/off/null
     * 被视为 false 返回；
     *
     * @param value 输入值
     * @return true/false
     */
    public static boolean toBoolean(Object value) {
        if (value == null)
            return false;

        if (value.equals(true) || value.equals(1) || value.equals(1L))
            return true;

        if (value instanceof String) {
            String _value = (String) value;

            if (_value.equalsIgnoreCase("yes") || _value.equalsIgnoreCase("true") || _value.equals("1") || _value.equalsIgnoreCase("on"))
                return true;

            if (_value.equalsIgnoreCase("no") || _value.equalsIgnoreCase("false") || _value.equals("0") || _value.equalsIgnoreCase("off")
                    || _value.equalsIgnoreCase("null"))
                return false;
        }

        return false;
    }

    /**
     * 转换为 int 类型
     *
     * @param value 送入的值
     * @return int 类型的值
     */
    public static int object2int(Object value) {
        if (value == null)
            return 0;

        if (value instanceof String)
            return Integer.parseInt((String) value);
        else if (value instanceof Number)
            return value instanceof Integer ? (int) value : ((Number) value).intValue();
        else {
            String toString = value.toString();

            if (toString.matches("-?\\d+"))
                return Integer.parseInt(toString);

            throw new IllegalArgumentException("This Object doesn't represent an int");
        }
    }

    /**
     * 转换为 long 类型
     *
     * @param value 送入的值
     * @return long 类型的值
     */
    public static long object2long(Object value) {
        if (value == null)
            return 0;

        if (value instanceof Long)
            return (long) value;
        else if (value instanceof BigInteger)
            return ((BigInteger) value).longValue();
        else if (value instanceof Number)
            return ((Number) value).longValue();
        else if (value instanceof String)
            return Long.parseLong((String) value);

        throw new IllegalArgumentException("This Object doesn't represent a long");
    }

    /**
     * 转换为 double 类型
     *
     * @param value 送入的值
     * @return double 类型的值
     */
    public static double object2double(Object value) {
        if (value == null)
            return 0;

        if (value instanceof Double)
            return (double) value;
        else if (value instanceof BigInteger)
            return ((BigInteger) value).doubleValue();
        else if (value instanceof Number)
            return ((Number) value).doubleValue();
        else if (value instanceof String)
            return Double.parseDouble((String) value);

        throw new IllegalArgumentException("This Object doesn't represent a double");
    }

    /**
     * 转换为 float 类型
     *
     * @param value 送入的值
     * @return float 类型的值
     */
    public static float object2float(Object value) {
        if (value == null)
            return 0;

        if (value instanceof Float)
            return (float) value;
        else if (value instanceof BigInteger)
            return ((BigInteger) value).floatValue();
        else if (value instanceof Number)
            return ((Number) value).floatValue();
        else if (value instanceof String)
            return Float.parseFloat((String) value);

        throw new IllegalArgumentException("This Object doesn't represent a float");
    }

    /**
     * 把字符串还原为 Java 里面的真实值，如 "true"--true,"123"--123,"null"--null
     *
     * @param value 字符串的值
     * @return Java 里面的值
     */
    public static Object toJavaValue(String value) {
        if (value == null)
            return null;

        value = value.trim();

        if ("".equals(value))
            return "";

        if ("null".equals(value))
            return null;

        if ("true".equalsIgnoreCase(value))
            return true;

        if ("false".equalsIgnoreCase(value))
            return false;

        // try 比较耗资源，先检查一下
        if (value.charAt(0) == '-' || (value.charAt(0) >= '0' && value.charAt(0) <= '9'))
            try {
                int int_value = Integer.parseInt(value);

                if ((String.valueOf(int_value)).equals(value)) // 判断为整形
                    return int_value;
            } catch (NumberFormatException e) {// 不能转换为数字
                try {
                    long long_value = Long.parseLong(value);
                    if ((String.valueOf(long_value)).equals(value)) // 判断为整形
                        return long_value;
                } catch (NumberFormatException e1) {
                    if (value.matches("[0-9]{1,13}(\\.[0-9]*)?"))
                        return Double.parseDouble(value);
                }
            }

        return value;
    }
//
//    private volatile static ConvertBasicValue convertValue;
//
//    /**
//     * For simple use
//     */
//    public static ConvertBasicValue getConvertValue() {
//        if (convertValue == null) {
//            synchronized (ConvertBasicValue.class) {
//                if (convertValue == null) {
//                    convertValue = new ConvertBasicValue();
//                }
//            }
//        }
//
//        return convertValue;
//    }
}
