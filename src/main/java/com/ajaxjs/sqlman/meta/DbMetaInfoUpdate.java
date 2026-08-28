package com.ajaxjs.sqlman.meta;

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

    /**
     * Creates update metadata for a map with an explicit identifier field.
     *
     * @param map       the entity values.
     * @param tableName the target table name.
     * @param idField   the identifier field name.
     */
    public DbMetaInfoUpdate(Map<String, Object> map, String tableName, String idField) {
        super(map, tableName);
        this.idField = idField;
    }

    /**
     * Creates update metadata for a map using the default identifier field.
     *
     * @param map       the entity values.
     * @param tableName the target table name.
     */
    public DbMetaInfoUpdate(Map<String, Object> map, String tableName) {
        this(map, tableName, "id");
    }

    /**
     * Creates update metadata for a bean.
     *
     * @param bean    the entity bean.
     * @param idField the identifier field name.
     */
    public DbMetaInfoUpdate(Object bean, String idField) {
        super(bean);
        this.idField = idField;
    }

    /**
     * Resolves the identifier field from the entity's {@link Id} annotation.
     *
     * @return the annotated identifier field name, or {@code null} when absent.
     * @throws UnsupportedOperationException if this metadata wraps a map.
     */
    public String getIdFieldNameByAnnotation() {
        if (entity instanceof Map)
            throw new UnsupportedOperationException("Map can't contain a annotation with db meta info.");

        Id annotation = entity.getClass().getAnnotation(Id.class);

        idField = annotation == null ? null : annotation.value();

        return idField;
    }

    /**
     * Obtains the configured identifier value from the entity.
     *
     * @return the identifier value.
     * @throws UnsupportedOperationException if no identifier field is configured.
     */
    public Object getIdValue() {
        if (idField == null)
            throw new UnsupportedOperationException("Please specific id field name or call getIdFieldNameByAnnotation() first.");

        if (entity instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) entity;
            return map.get(idField);
        } else {
            String getId = Utils.changeColumnToFieldName("get_" + idField);

            try {
                return Methods.execute(entity, getId);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
