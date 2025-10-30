package com.ajaxjs.sqlman.v2temp.meta;

import lombok.Data;

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

    public DbMetaInfoCreate(Object bean) {
        super(bean);
    }

    public DbMetaInfoCreate(Map<String, Object> map, String tableName) {
        super(map, tableName);
    }
}
