package com.ajaxjs.util.reflect;

import com.ajaxjs.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 类相关的反射
 */
@Slf4j
public class Clazz {
    /**
     * 根据类名字符串获取类对象
     *
     * @param clzName 类全称。如果是内部类请注意用法
     * @return 类对象
     */
    public static Class<?> getClassByName(String clzName) {
        try {
            return Class.forName(clzName);
        } catch (ClassNotFoundException e) {
            log.warn("找不到这个类： {}", clzName, e);
            return null;
        }
    }

    /**
     * 根据类名字符串获取类对象，可强类型转换类型
     *
     * @param clzName 类全称
     * @param clz     要转换的目标类型
     * @param <T>     类引用
     * @return 类对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClassByName(String clzName, Class<T> clz) {
        Class<?> c = getClassByName(clzName);

        return c == null ? null : (Class<T>) c;
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
     * 已知接口类型，获取它的 class
     *
     * @param type 接口类型
     * @return 接口的类对象
     */
    public static Class<?> getClassByInterface(Type type) {
        String className = type.toString();
        className = className.replaceAll("<.*>$", StrUtil.EMPTY_STRING).replaceAll("(class|interface)\\s", StrUtil.EMPTY_STRING); // 不要泛型的字符

        return getClassByName(className);
    }

    /**
     * 获取某个类的所有接口
     *
     * @param clz 目标类
     * @return 类的所有接口
     */
    public static Class<?>[] getDeclaredInterface(Class<?> clz) {
        List<Class<?>> fields = new ArrayList<>();

        for (; clz != Object.class; clz = clz.getSuperclass()) {
            Class<?>[] currentInterfaces = clz.getInterfaces();
            fields.addAll(Arrays.asList(currentInterfaces));
        }

        return fields.toArray(new Class[0]);
    }

    /**
     * 获取 private 字段
     *
//     * @param clz       类
     * @param fieldName 字段名
     * @param fieldType 字段类型
     * @param <T>       字段类型
     * @return 字段的值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object instance, String fieldName, Class<T> fieldType) {
        MethodHandles.Lookup lookup;
        Class<?> clz = instance.getClass();

        try {
            lookup = MethodHandles.privateLookupIn(clz, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("非法访问类： " + clz, e);
        }

        MethodHandle mh;

        try {
            mh = lookup.findGetter(clz, fieldName, fieldType);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("找不到该字段 " + fieldName, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("非法访问字段： " + fieldName, e);
        }

        try {
            return (T) mh.invoke(instance);
        } catch (Throwable e) {
            throw new RuntimeException("获取 private 字段异常", e);
        }
    }

    /**
     * 根据类创建实例，可传入构造器参数。
     * 该函数根据给定的类对象和构造器参数创建一个实例。如果参数中的类是接口，将返回 null。
     * 如果参数中的构造器参数为空或长度为0，则使用类的默认无参构造函数创建实例。
     * 如果构造器参数不为空，将使用反射获取与参数类型匹配的构造函数，并使用构造函数创建实例。
     *
     * @param clz  类对象
     * @param args 获取指定参数类型的构造函数，这里传入我们想调用的构造函数所需的参数。可以不传。
     * @param <T>  类引用
     * @return 对象实例
     */
    public static <T> T newInstance(Class<T> clz, Object... args) {
        if (clz.isInterface())
            throw new IllegalArgumentException("所传递的 class 类型参数为接口 " + clz + "，无法实例化");

        if (args == null || args.length == 0) {
            try {
                return newInstance(clz.getDeclaredConstructor());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("找不到这个 " + clz.getName() + " 类的构造器。", e);
            }
        }

        Constructor<T> constructor = getConstructor(clz, Clazz.args2class(args));// 获取构造器

        return newInstance(constructor, args);
    }

    /**
     * 根据构造器创建实例
     * 该函数根据给定的构造器和参数列表创建指定类的实例。它使用反射调用构造函数来实例化对象，并在实例化失败时返回 null。
     *
     * @param constructor 类构造器
     * @param args        获取指定参数类型的构造函数，这里传入我们想调用的构造函数所需的参数。可以不传。
     * @param <T>         类引用
     * @return 对象实例
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args); // 实例化
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("实例化对象失败：" + constructor.getDeclaringClass(), e);
        }
    }

    /**
     * 传入的类是否有带参数的构造器
     * 该函数通过传入的类对象，判断该类是否有带参数的构造函数，若有则返回true，否则返回false。函数首先获取类的所有构造函数，
     * 然后遍历构造函数，判断构造函数的参数列表是否非空，若存在非空的参数列表则返回true。
     * 如果遍历完所有的构造函数都没有找到带参数的构造函数，则返回false。
     *
     * @param clz 类对象
     * @return true 表示为有带参数
     */
    public static boolean hasArgsCon(Class<?> clz) {
        Constructor<?>[] constructors = clz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length != 0)
                return true;
        }

        return false;
    }

	/**
	 * 根据类全称创建实例，并转换到其接口的类型
	 *
	 * @param className 实际类的类型
	 * @param clazz     接口类型
	 * @return 对象实例
	 */
    // @SuppressWarnings("unchecked")
    // public static <T> T newInstance(String className, Class<T> clazz) {
    // Class<?> clz = getClassByName(className);
    // return clazz != null ? (T) newInstance(clz) : null;
    // }

    /**
     * 根据类全称创建实例
     * 该函数根据给定的类全称和参数，使用反射获取类对象并创建相应类型的对象实例。返回对象实例，类型为 Object。
     *
     * @param clzName 类全称
     * @param args    根据构造函数，创建指定类型的对象,传入的参数个数需要与上面传入的参数类型个数一致
     * @return 对象实例，因为传入的类全称是字符串，无法创建泛型 T，所以统一返回 Object
     */
    public static Object newInstance(String clzName, Object... args) {
        Class<?> clazz = Clazz.getClassByName(clzName);

        return clazz != null ? newInstance(clazz, args) : null;
    }

    /**
     * 获取类的构造器，可以支持重载的构造器（不同参数的构造器）
     * 这个函数用于获取类的构造函数。它接受两个参数，一个是类对象，一个是可选的参数类型数组。
     * 如果传入了参数类型数组，则获取与该数组匹配的构造函数；如果没有传入参数类型数组，则获取空参数列表的构造函数。
     * 如果找不到合适的构造函数，会记录日志并返回 null。
     *
     * @param clz    类对象
     * @param argClz 指定构造函数的参数类型，这里传入我们想调用的构造函数所需的参数类型
     * @param <T>    类引用
     * @return 类的构造器
     */
    public static <T> Constructor<T> getConstructor(Class<T> clz, Class<?>... argClz) {
        try {
            return argClz != null ? clz.getConstructor(argClz) : clz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("找不到这个 " + clz.getName() + " 类的构造器。", e);
        } catch (SecurityException e) {
            throw new RuntimeException("获取类的构造器失败，安全问题", e);
        }
    }
}
