package com.ajaxjs.sqlman_v2.meta;

import com.ajaxjs.sqlman.annotation.Id;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.reflect.Methods;

import java.util.Map;

/**
 * The meta-information of a database updating entity, for which table and which row to update.
 */
//@Data
public class DbMetaInfoUpdate extends DbMetaInfoBase {
    String idField;

    public DbMetaInfoUpdate(Map<String, Object> map, String tableName, String idField) {
        super(map, tableName);
        this.idField = idField;
    }

    public DbMetaInfoUpdate(Map<String, Object> map, String tableName) {
        this(map, tableName, "id");
    }

    public DbMetaInfoUpdate(Object bean, String idField) {
        super(bean);
        this.idField = idField;
    }

    public String getIdFieldNameByAnnotation() {
        if (entity instanceof Map)
            throw new UnsupportedOperationException("Map can't contain a annotation with db meta info.");

        Id annotation = entity.getClass().getAnnotation(Id.class);

        idField = annotation == null ? null : annotation.value();

        return idField;
    }

    public Object getIdValue() {
        if (idField == null)
            throw new UnsupportedOperationException("Please specific id field name or call getIdFieldNameByAnnotation() first.");

        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) entity;
            return map.get(idField);
        } else {
            String getId = Utils.changeColumnToFieldName("get_" + idField);

            return Methods.executeMethod(entity, getId);
        }
    }
}
