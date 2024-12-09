package com.ajaxjs.sqlman.util;


import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectUtil {
    /**
     * 根据类、方法的字符串和参数列表获取方法对象，支持重载的方法
     *
     * @param obj    可以是实例对象，也可以是类对象
     * @param method 方法名称
     * @param args   明确的参数类型列表
     * @return 匹配的方法对象，null 表示找不到
     */
    public static Method getMethod(Object obj, String method, Class<?>... args) {
        Class<?> cls = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        try {
            return ObjectUtils.isEmpty(args) ? cls.getMethod(method) : cls.getMethod(method, args);
        } catch (NoSuchMethodException | SecurityException e) {
            System.err.println("类找不到这个方法 " + method);
            return null;
        }
    }

    /**
     * 把参数转换为类对象列表
     * 这个 Java 函数将一个可变参数列表转换为一个类对象列表。它接受一个可变参数 args，返回一个 Class 类型的数组 clazz，
     * 数组长度与参数列表的长度相同，并且每个元素的类型与对应参数的类型相同。
     *
     * @param args 可变参数列表
     * @return 类对象列表
     */
    public static Class<?>[] args2class(Object[] args) {
        Class<?>[] clazz = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            clazz[i] = args[i].getClass();

        return clazz;
    }

    /**
     * 调用方法
     *
     * @param instance 对象实例，bean
     * @param method   方法对象名称
     * @param args     参数列表
     * @return 执行结果
     */
    public static Object executeMethod(Object instance, String method, Object... args) {
        // 没有方法对象，先找到方法对象。可以支持方法重载，按照参数列表
        Class<?>[] clazz = args2class(args);
        Method methodObj = getMethod(instance.getClass(), method, clazz);

        return methodObj != null ? executeMethod(instance, methodObj, args) : null;
    }

    /**
     * 调用方法，该方法不会抛出异常
     *
     * @param instance 对象实例，bean
     * @param method   方法对象
     * @param args     参数列表
     * @return 执行结果
     */
    public static Object executeMethod(Object instance, Method method, Object... args) {
        try {
            return executeMethod_Throwable(instance, method, args);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 调用方法
     *
     * @param instance 对象实例，bean
     * @param method   方法对象
     * @param args     参数列表
     * @return 执行结果
     * @throws Throwable 任何异常
     */
    public static Object executeMethod_Throwable(Object instance, Method method, Object... args) throws Throwable {
        if (instance == null || method == null)
            return null;

        try {
            return args == null || args.length == 0 ? method.invoke(instance) : method.invoke(instance, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
            Throwable e;

            if (e1 instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e1).getTargetException();
                throw e;
            }

            throw e1;
        }
    }

    /**
     * 获取泛型类型数组。
     *
     * @param type 要获取泛型类型数组的 Type 对象
     * @return 返回泛型类型数组。如果指定的 Type 对象不是 ParameterizedType 类型，则返回 null。
     */
    public static Type[] getActualType(Type type) {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType) type).getActualTypeArguments();
        else {
            System.err.println(type + " 很可能不是一个泛型");
            return null;
        }
    }

    /**
     * 获取方法返回值里面的泛型，如 List&lt;String&gt; 里面的 String，而不是 T。
     *
     * @param method 方法
     * @return 实际类型，可能多个
     */
    public static Type[] getGenericReturnType(Method method) {
        return getActualType(method.getGenericReturnType());
    }

    /**
     * 获取方法返回值里面的泛型，如 List&lt;String&gt; 里面的 String，而不是 T。
     * 这个方法获取第一个类型，并转换为 Class
     *
     * @param method 方法
     * @return 第一个实际类型
     */
    public static Class<?> getGenericFirstReturnType(Method method) {
        Type[] type = getGenericReturnType(method);

        return type == null ? null : type2class(type[0]);
    }

    /**
     * 获取如 List&lt;String&gt; 里面的泛型类型
     *
     * @param clz 类必须先指向一个实例，参见
     *            <a href="https://stackoverflow.com/questions/8436055/how-to-get-class-of-generic-type-when-there-is-no-parameter-of-it">...</a>
     * @return 实际类型
     */
    public static Type[] getActualType(Class<?> clz) {
        return getActualType(clz.getGenericSuperclass());
    }

    /**
     * 获取实际类
     *
     * @param clz 类型
     * @return 实际类
     */
    public static Class<?> getActualClass(Class<?> clz) {
        Type[] actualType = getActualType(clz);

        return type2class(actualType[0]);
    }

    /**
     * Type 接口转换为 Class
     *
     * @param type Type 接口
     * @return Class
     */
    public static Class<?> type2class(Type type) {
        return type instanceof Class ? (Class<?>) type : null;
    }
}
