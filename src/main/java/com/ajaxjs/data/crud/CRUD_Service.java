package com.ajaxjs.data.crud;

import com.ajaxjs.data.*;
import com.ajaxjs.data.data_service.DataServiceUtils;
import com.ajaxjs.data.jdbc_helper.JdbcReader;
import com.ajaxjs.data.jdbc_helper.JdbcWriter;
import com.ajaxjs.data.jdbc_helper.common.IdField;
import com.ajaxjs.data.jdbc_helper.common.TableName;
import com.ajaxjs.util.ListUtils;
import com.ajaxjs.util.reflect.Methods;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class CRUD_Service implements DataAccessObject {
    private JdbcReader reader;

    private JdbcWriter writer;

    /**
     *
     */
    private SmallMyBatis smallMyBatis;

    @Override
    public <T> T queryOne(Class<T> clz, String sql, Object... params) {
        return reader.queryOne(sql, clz, params);
    }

    @Override
    public <T> T info(Class<T> beanClz, String sql, Object... params) {
        return reader.queryAsBean(beanClz, sql, params);
    }

    @Override
    public <T> T infoBySqlId(Class<T> beanClz, String sqlId, Map<String, Object> mapParams, Object... params) {
        String sql = smallMyBatis.handleSql(mapParams, sqlId);

        return reader.queryAsBean(beanClz, sql, params);
    }

    @Override
    public Map<String, Object> infoMap(String sql, Object... params) {
        return reader.queryAsMap(sql, params);
    }

    @Override
    public Map<String, Object> infoMapBySqlId(String sqlId, Map<String, Object> mapParams, Object... params) {
        String sql = smallMyBatis.handleSql(mapParams, sqlId);

        return reader.queryAsMap(sql, params);
    }

    @Override
    public <T> List<T> list(Class<T> beanClz, String sql, Object... params) {
        return ListUtils.getList(reader.queryAsBeanList(beanClz, sql, params));
    }

    @Override
    public <T> List<T> listById(Class<T> beanClz, String sqlId, Map<String, Object> mapParams, Object... params) {
        String sql = smallMyBatis.handleSql(mapParams, sqlId);

        return ListUtils.getList(reader.queryAsBeanList(beanClz, sql, params));
    }

    @Override
    public List<Map<String, Object>> listMap(String sql, Object... params) {
        return ListUtils.getList(reader.queryAsMapList(sql, params));
    }

    @Override
    public List<Map<String, Object>> listMapBySqlId(String sqlId, Map<String, Object> mapParams, Object... params) {
        String sql = smallMyBatis.handleSql(mapParams, sqlId);

        return ListUtils.getList(reader.queryAsMapList(sql, params));
    }

    @Override
    public <T> PageResult<T> page(Class<T> beanClz, String sql, Map<String, Object> paramsMap) {
        sql = SmallMyBatis.handleSql(sql, paramsMap);

        PageEnhancer p = new PageEnhancer();
        p.setJdbcReader(reader);
        p.initSql(sql, DataServiceUtils.getRequest());

        return p.page(beanClz);
    }

    @Override
    public <T> PageResult<T> pageBySqlId(Class<T> beanClz, String sqlId, Map<String, Object> mapParams) {
        String sql = smallMyBatis.handleSql(mapParams, sqlId);

        PageEnhancer p = new PageEnhancer();
        p.setJdbcReader(reader);
        p.initSql(sql, DataServiceUtils.getRequest());

        return p.page(beanClz);
    }

    @Override
    public Long create(String talebName, Object entity, String idField) {
        writer.setTableName(talebName);

        if (StringUtils.hasText(idField))
            writer.setIdField(idField); // 如果ID字段名不为空，则设置 ID 字段名

        return (Long) writer.create(entity);
    }

    @Override
    public Long createWithIdField(Object entity, String idField) {
        return create(getTableName(entity), entity, idField);
    }

    @Override
    public Long createWithIdField(Object entity) {
        return createWithIdField(entity, getIdField(entity));
    }

    @Override
    public Long create(Object entity) {
        return create(getTableName(entity), entity, null);
    }

    @Override
    public boolean update(String talebName, Object entity, String idField) {
        writer.setTableName(talebName);

        if (StringUtils.hasText(idField))
            writer.setIdField(idField);
        else throw new DataAccessException("未指定 id，这将会是批量全体更新！");

        return writer.update(entity) > 0;
    }

    @Override
    public boolean update(String talebName, Object entity) {
        return update(talebName, entity, null);
    }

    @Override
    public boolean update(Object entity) {
        return update(getTableName(entity), entity);
    }

    @Override
    public boolean updateWithIdField(Object entity) {
        return updateWithIdField(entity, getIdField(entity));
    }

    @Override
    public boolean updateWithIdField(Object entity, String idField) {
        return update(getTableName(entity), entity, idField);
    }

    @Override
    public boolean updateWithWhere(Object entity, String where) {
        String talebName = getTableName(entity);

        writer.setTableName(talebName);
        writer.setWhere(where);

        return writer.updateWhere(entity, where) > 0;
    }

    @Override
    public boolean delete(Object entity, Serializable id) {
        return delete(getTableName(entity), id);
    }

    @Override
    public boolean delete(String talebName, Serializable id) {
        writer.setTableName(talebName);

        return writer.delete(id);
    }

    @Override
    public boolean delete(Object entity) {
        Object id = Methods.executeMethod(entity, "getId");

        if (id != null) {
            return delete(entity, (Serializable) id);
        } else {
            System.err.println("没有 getId()");
            return false;
        }
    }

    /**
     * 获取实体类上的表名（通过注解）
     *
     * @param entity 实体类
     * @return 表名
     */
    public static String getTableName(Object entity) {
        TableName tableNameA = entity.getClass().getAnnotation(TableName.class);
        if (tableNameA == null)
            throw new RuntimeException("实体类未提供表名");

        return tableNameA.value();
    }

    /**
     * 获取实体类上的 Id 字段名称（通过注解）
     *
     * @param entity 实体类
     * @return 表名
     */
    public static String getIdField(Object entity) {
        IdField annotation = entity.getClass().getAnnotation(IdField.class);

        if (annotation == null)
            throw new DataAccessException("没设置 IdField 注解，不知哪个主键字段");

        return annotation.value();
    }

    public static CRUD_Service factory(Connection conn) {
        JdbcWriter writer = new JdbcWriter();
        writer.setConn(conn);

        JdbcReader reader = new JdbcReader();
        reader.setConn(conn);

        CRUD_Service crud = new CRUD_Service();
        crud.setReader(reader);
        crud.setWriter(writer);

        return crud;
    }
}