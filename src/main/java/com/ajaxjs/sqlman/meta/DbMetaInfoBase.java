package com.ajaxjs.sqlman.meta;

import com.ajaxjs.sqlman.annotation.Table;

import java.util.Map;

public abstract class DbMetaInfoBase {
    String tableName;

    Object entity;

    /**
     * Only for Java bean pass in.
     *
     * @param bean Java bean
     */
    public DbMetaInfoBase(Object bean) {
        if (bean instanceof Map)
            throw new UnsupportedOperationException("Only for Java bean pass in.");

        entity = bean;
    }

    /**
     * If it's a map entity, should pass table name.
     *
     * @param map       Map entity
     * @param tableName Table name
     */
    public DbMetaInfoBase(Map<String, Object> map, String tableName) {
        entity = map;
        this.tableName = tableName;
    }

    public String getTableNameByAnnotation() {
        if (entity instanceof Map)
            throw new UnsupportedOperationException("Map can't contain a annotation with db meta info.");

        Table annotation = entity.getClass().getAnnotation(Table.class);

        tableName = annotation == null ? null : annotation.value();

        return tableName;
    }
}
