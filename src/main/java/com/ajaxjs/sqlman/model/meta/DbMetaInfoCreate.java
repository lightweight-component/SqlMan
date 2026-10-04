package com.ajaxjs.sqlman.model.meta;

import java.util.Map;

/**
 * The meta-information of a database creating entity, for which table to create, and to return newly id.
 */
public class DbMetaInfoCreate<T> extends DbMetaInfoBase {
    Class<T> idDataType;

    /**
     * Is this id field auto increment?
     */
    boolean isAutoIns;

    /**
     * Creates metadata for a bean to be inserted.
     *
     * @param bean the entity bean.
     */
    public DbMetaInfoCreate(Object bean) {
        super(bean);
    }

    /**
     * Creates metadata for a map to be inserted.
     *
     * @param map       the entity values.
     * @param tableName the target table name.
     */
    public DbMetaInfoCreate(Map<String, Object> map, String tableName) {
        super(map, tableName);
    }
}
