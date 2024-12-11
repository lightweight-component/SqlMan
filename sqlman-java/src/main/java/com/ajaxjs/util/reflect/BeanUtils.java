package com.ajaxjs.util.reflect;


import com.ajaxjs.sqlman.annotation.IgnoreDB;
import com.ajaxjs.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

@Slf4j
public class BeanUtils {
    /**
     * 根据方法名称来截取属性名称，例如把 getter 的 getXxx() 转换为 xxx 的字段名
     *
     * @param method 方法名称
     * @param action set|get
     * @return 属性名称
     */
    public static String getFieldName(String method, String action) {
        method = method.replace(action, StrUtil.EMPTY_STRING);

        return Character.toString(method.charAt(0)).toLowerCase() + method.substring(1);
    }

    /**
     * 设置 Java Bean 的值
     *
     * @param bean      Bean 实体
     * @param fieldName 字段名
     * @param value     值
     */
    public static void setBeanValue(Object bean, String fieldName, Object value) {
        try {
            Field field = bean.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            // the exception here doesn't need to handle
            log.warn("访问字段时候 {} 失败", fieldName);
        } catch (NoSuchFieldException e) {
            log.warn("No Such Field: {}", fieldName);
        }
    }

    /**
     * 调用 bean 对象的 setter 方法 参考 Spring 的 <code>ReflectionUtils.setField(null, null, null);</code>
     *
     * @param bean  Bean 对象
     * @param name  属性名称，前缀不要带 set
     * @param value 要设置的属性值
     */
    public static void setProperty(Object bean, String name, Object value) {
        String setMethodName = "set" + StringUtils.capitalize(name);
        Objects.requireNonNull(bean, bean + "执行：" + setMethodName + " 未发现类");
//		Objects.requireNonNull(value, bean + "执行：" + setMethodName + " 未发现参数 value");
        Class<?> clazz = bean.getClass();
        // 要把参数父类的也包括进来
        Method method = Methods.getMethodByUpCastingSearch(clazz, setMethodName, value);

        // 如果没找到，那就试试接口的……
        if (method == null)
            method = Methods.getDeclaredMethodByInterface(clazz, setMethodName, value);

        // 如果没找到，那忽略参数类型，只要匹配方法名称即可。这会发生在：由于被注入的对象有可能经过了 AOP 的动态代理，所以不能通过上述逻辑找到正确的方法
        if (method == null)
            method = Methods.getSuperClassDeclaredMethod(clazz, setMethodName);

        // 最终还是找不到
        Objects.requireNonNull(method, "找不到目标方法[" + clazz.getSimpleName() + "." + setMethodName + "(" + value.getClass().getSimpleName() + ")]");

        Methods.executeMethod(bean, method, value);
    }

    @FunctionalInterface
    public interface EachFieldArg {
        void item(String key, Object value, PropertyDescriptor property);
    }

    /**
     * 遍历一个 Java Bean
     *
     * @param bean Java Bean
     * @param fn   执行的任务，参数有 key, value, property
     */
    public static void eachField(Object bean, EachFieldArg fn) {
        try {
            PropertyDescriptor[] props = Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors();
            eachField(bean, props, fn);
        } catch (IntrospectionException e) {
            log.warn("获取 Bean 信息时候错误", e);
        }
    }

    /**
     * 遍历一个 Java Bean
     *
     * @param bean  Java Bean
     * @param props 属性集合
     * @param fn    执行的任务，参数有 key, value, property
     */
    public static void eachField(Object bean, PropertyDescriptor[] props, EachFieldArg fn) {
        try {
            if (props == null || props.length == 0)
                return;

            for (PropertyDescriptor property : props) {
                String key = property.getName();
                Method getter = property.getReadMethod();// 得到 property 对应的 getter 方法

                if (getter.getAnnotation(IgnoreDB.class) != null)
                    continue;

                Object value = getter.invoke(bean); // 原始默认值，不过通常是没有指定的

                if (value != null && value.equals("class"))  // 过滤 class 属性
                    continue;

                fn.item(key, value, property);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("遍历一个 Java Bean 时候 Illegal Argument: ", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("遍历一个 Java Bean 并访问字段时候失败", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("遍历一个 Java Bean 失败 InvocationTargetException，原因： " + e.getCause(), e.getCause());
        }
    }

    /**
     * 遍历 Java Bean 对象的所有字段，并对每个字段执行指定的操作。
     * 注意 static 成员无效
     *
     * @param bean 要遍历的 Java Bean 对象。
     * @param fn   对每个字段要执行的操作，类型为 BiConsumer，其中第一个参数为字段名，第二个参数为字段值。
     */
    public static void eachFields(Object bean, BiConsumer<String, Object> fn) {
        eachFields2(bean.getClass(), (name, field) -> {
            try {
                fn.accept(name, field.get(bean));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("访问字段时候 " + field + " 失败", e);
            }
        });
    }

    /**
     * 遍历给定类的所有非静态字段，并对每个字段执行给定的操作。
     *
     * @param clz 要遍历的类
     * @param fn  对于每个字段执行的操作
     */
    public static void eachFields2(Class<?> clz, BiConsumer<String, Field> fn) {
        Field[] fields = clz.getFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))// 如果是静态的字段，则跳过
                continue;

            fn.accept(field.getName(), field);// 对于当前字段执行给定的操作
        }
    }

    /**
     * 常量转换为 Map
     * 获取指定类中的所有 int 类型常量的名称和值，并返回它们构成的 Map 对象。
     *
     * @param clz 常量类，一般为接口
     * @return 常量的 Map 格式
     */
    public static Map<String, Integer> getConstantsInt(Class<?> clz) {
        Map<String, Integer> map = new HashMap<>();// 创建一个空的 HashMap 对象，用于存储常量名称和值的映射关系
        Field[] fields = clz.getDeclaredFields();
        Object instance = Clazz.newInstance(clz);

        for (Field field : fields) {
            String descriptor = Modifier.toString(field.getModifiers());// 获得其属性的修饰

            // 判断该属性是否为 public static final 修饰的 int 类型常量
            if (descriptor.equals("public static final")) {
                try {
                    map.put(field.getName(), (int) field.get(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("常量转换为 Map 访问字段失败。", e);
                }
            }
        }

        return map;
    }
}
