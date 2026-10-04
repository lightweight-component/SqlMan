package com.ajaxjs.sqlman.entity;

import com.ajaxjs.sqlman.annotation.Column;
import com.ajaxjs.sqlman.annotation.Transient;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.reflect.Fields;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Iterates the readable properties of a JavaBean as validated database column-value pairs.
 * <p>
 * Properties without getters, {@link Transient transient} properties, and {@code null}-valued
 * properties are skipped by default. A {@link Column} annotation on a getter takes precedence
 * over one on its backing field. Column names are converted to snake case and validated by
 * {@link SqlIdentifier} before they are supplied to the consumer.
 * </p>
 * <p>
 * The optional write mode controls whether {@link Column#insertable()} or
 * {@link Column#updatable()} restrictions are applied. Instances are mutable configuration
 * objects and are intended for use by one caller at a time.
 * </p>
 */
@Slf4j
public class BeanIterator {
    final Object bean;

    private final Class<?> clz;

    /**
     * Creates an iterator for the supplied JavaBean.
     *
     * @param bean the source bean whose readable properties are iterated
     * @throws NullPointerException if {@code bean} is {@code null}
     */
    public BeanIterator(Object bean) {
        this.bean = Objects.requireNonNull(bean, "BeanIterator.bean");
        clz = bean.getClass();
    }

    /**
     * Whether properties whose values are {@code null} are passed to the consumer.
     * The default is {@code false}.
     */
    @Setter
    boolean includeNull;

    /**
     * Selects whether {@link Column} write restrictions are applied.
     * <ul>
     *     <li>{@code true}: applies {@link Column#insertable()} and skips non-insertable properties.</li>
     *     <li>{@code false}: applies {@link Column#updatable()} and skips non-updatable properties.</li>
     *     <li>{@code null}: applies neither restriction, for general-purpose iteration.</li>
     * </ul>
     *
     * @param insert the written mode, or {@code null} for no write-mode filtering
     */
    @Setter
    Boolean insert;

    /**
     * Iterates eligible bean properties and supplies each resolved column name and value.
     * <p>
     * Getter-level {@link Column} annotations take precedence over field-level annotations.
     * Properties without a getter and properties annotated with {@link Transient} are skipped.
     * A getter exception is propagated as an {@link IllegalStateException}.
     * </p>
     *
     * @param everyBeanField the consumer receiving a validated snake-case column name and its value
     * @throws IllegalStateException    if bean metadata cannot be inspected or a property getter fails
     * @throws IllegalArgumentException if a resolved column name is not a valid SQL identifier
     * @throws NullPointerException     if {@code everyBeanField} is {@code null} and an eligible property is encountered
     */
    public void each(BiConsumer<String, Object> everyBeanField) {
        BeanInfo beanInfo = getBeanInfo();

        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
            String propertyName = property.getName();

            if (CommonConstant.CLASS.equals(propertyName))
                continue;

            Method method = property.getReadMethod();

            if (method == null) {
                log.warn("Skip unreadable property {}.{} because it has no getter.", clz.getName(), propertyName);
                continue;
            }

            if (method.isAnnotationPresent(Transient.class)) // ignore transient fields
                continue;

            Field field = Fields.findField(clz, propertyName); // field info. on the entity

            if (field != null && field.isAnnotationPresent(Transient.class)) // ignore transient fields
                continue;

            Column column = method.getAnnotation(Column.class);

            if (column == null && field != null)
                column = field.getAnnotation(Column.class);

            if (column != null && insert != null && (insert ? !column.insertable() : !column.updatable()))
                continue;

            String fieldName = column != null && ObjectHelper.hasText(column.name()) ? column.name() : propertyName;
            Object value = getValue(method, propertyName);

            if (includeNull || value != null) {
                String fieldStr = Utils.changeFieldToColumnName(fieldName);
                SqlIdentifier.check(fieldStr);
                everyBeanField.accept(fieldStr, value);
            }
        }
    }

    private BeanInfo getBeanInfo() {
        try {
            return Introspector.getBeanInfo(clz);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot inspect Java bean " + clz.getName() + ".", e);
        }
    }

    private Object getValue(Method method, String propertyName) {
        try {
            return method.invoke(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read Java bean property " + clz.getName() + "." + propertyName + " via its getter.", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException() == null ? e : e.getTargetException();
            throw new IllegalStateException("Cannot read Java bean property " + clz.getName() + "." + propertyName + " via its getter.", cause);
        }
    }
}
