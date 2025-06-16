/**
 * Copyright (C) 2025 Frank Cheung
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ajaxjs.sqlman.crud;

import com.ajaxjs.sqlman.DataAccessException;
import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.annotation.Id;
import com.ajaxjs.sqlman.annotation.Table;
import com.ajaxjs.sqlman.crud.model.SqlParams;
import com.ajaxjs.sqlman.crud.model.TableModel;
import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;
import com.ajaxjs.sqlman.util.Utils;
import com.ajaxjs.util.reflect.Methods;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;

/**
 * CRUD for Entity
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class Entity extends Sql implements IEntity {
    /**
     * Create a JDBC action with global connection
     */
    public Entity() {
        super();
    }

    /**
     * Create a JDBC action with specified connection
     */
    public Entity(Connection conn) {
        super(conn);
    }

    /**
     * Create a JDBC action with specified data source
     */
    public Entity(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 实体在数据库的建模信息
     */
    private TableModel tableModel;

    public Entity setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
        return this;
    }

    public Entity setTableName(String tableName) {
        tableModel = new TableModel();
        tableModel.setTableName(tableName);

        return this;
    }

    /**
     * Entity 实体，可以是 Map or Java Bean
     */
    private Object javaBean;

    @Override
    public IEntity input(Object javaBean) {
        this.javaBean = javaBean;

        if (tableModel == null) { // 表示省略 setTableModel() 配置，那么的话，从实体 class 获取元数据
            tableModel = new TableModel();

            Table annotation = javaBean.getClass().getAnnotation(Table.class);

            if (annotation == null)
                throw new IllegalStateException("未指定 @Table 注解，无法获取表名");

            tableModel.setTableName(annotation.value());
            tableModel.setAutoIns(annotation.isAutoIns());
        }

        return this;
    }

    @Override
    public <T extends Serializable> CreateResult<T> create(Class<T> idTypeClz) {
        SqlParams sp = BeanWriter.entity2InsertSql(getTableName(), javaBean);
        setSql(sp.sql);
        setParams(sp.values);

        return create(tableModel.isAutoIns(), idTypeClz);
    }

    /**
     * 修改实体
     * 将一个 Map 转换成更新语句的 SqlParams 对象，可允许任意的过滤条件，而不只是 WHERE id = 1
     *
     * @param where 查询条件，直接是 SQL WHERE 语句的后面部分（不用包含 WHERE 字符），可允许任意的过滤条件
     * @return 成功修改的行数，一般为 1
     */
    @Override
    public UpdateResult update(String where) {
        if (where == null) {
            log.warn("You're executing UPDATE, R U sure??? All records will be effected!");

            return update();
        } else {
            SqlParams sp = BeanWriter.entity2UpdateSql(getTableName(), javaBean, null, null);
            sp.sql += " WHERE " + where;

            setSql(sp.sql);
            setParams(sp.values);

            return super.update();
        }
    }

    @Override
    public UpdateResult update() {
        if (javaBean != null) { // do bean update
            Object id = getIdValue();

            if (id == null) {
                log.warn("You're executing UPDATE, R U sure?? All records will be effected!");
                throw new DataAccessException("未指定 id，这将会是批量全体更新！");
            }

            SqlParams sp = BeanWriter.entity2UpdateSql(getTableName(), javaBean, tableModel.getIdField(), id);
            setSql(sp.sql);
            setParams(sp.values);
        }

        // if there's no bean, then keeps the old way

        return super.update();
    }

    private String getTableName() {
        if (javaBean instanceof Map) {
            return tableModel.getTableName();
        } else {
            Table annotation = javaBean.getClass().getAnnotation(Table.class);

            if (annotation != null)
                return annotation.value();
            else
                return tableModel.getTableName();
        }
    }

    private Object getIdValue() {
        Object id;

        if (javaBean instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) javaBean;
            id = map.get(tableModel.getIdField());
        } else {
            String idFieldName;
            Id annotation = javaBean.getClass().getAnnotation(Id.class);

            if (annotation != null)
                idFieldName = annotation.value();
            else
                idFieldName = tableModel.getIdField();

            String getId = Utils.changeColumnToFieldName("get_" + idFieldName);
            id = Methods.executeMethod(javaBean, getId);
        }

        return id;
    }

    /**
     * 命名空间，标识
     */
    private String namespace;

    /**
     * 查询列表的时候，是否自动加上按照日期排序
     */
    private boolean listOrderByDate = true;

    /**
     * 实体类引用名称
     */
    private String clzName;

    /**
     * 是否加入租户数据隔离
     */
    private boolean isTenantIsolation;

    /**
     * 当前用户的约束
     */
    private boolean isCurrentUserOnly;

    public final static String DUMMY_STR = "1=1";

    private final static String SELECT_SQL = "SELECT * FROM %s WHERE " + DUMMY_STR;

    @Override
    public <T extends Serializable> Entity info(T id) {
        String sql = getSql();// 尝试获取已经定义好的 SQL 语句

        // 如果已经定义了 SQL 语句且不为空，则处理查询参数的动态替换
        if (!StringUtils.hasText(sql)) {
            // 如果没有预定义SQL，则根据表名和ID字段生成一个默认的查询 SQL
            sql = String.format(SELECT_SQL, tableModel.getTableName());
            sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + tableModel.getIdField() + " = ?");
            setParams(new Object[]{id});
        }

//        sql = limitToCurrentUser(sql);// 限制查询结果只包含当前用户的数据
//
//        if (isTenantIsolation())// 根据是否启用了租户隔离，动态添加租户ID查询条件
//            sql = TenantService.addTenantIdQuery(sql);

        setSql(sql);
        return this;
    }

    public Entity list() {
        return list(null);
    }

    public Entity list(String where) {
        String sql = getSql();

        if (StringUtils.hasText(sql)) {
//            sql = SmallMyBatis.handleSql(sql, DataServiceUtils.getQueryStringParams());
        } else {
            if (isListOrderByDate()) {
                String createDateField = getTableModel().getCreateDateField();
                String tableName = tableModel.getTableName();

                sql = String.format(SELECT_SQL + " ORDER BY " + createDateField + " DESC", tableName);
            } else
                sql = String.format(SELECT_SQL, tableModel.getTableName());
        }

        if (getTableModel().isHasIsDeleted())
            sql = sql.replace(DUMMY_STR, DUMMY_STR + " AND " + getTableModel().getDelField() + " != 1");

//        sql = limitToCurrentUser(sql);
//
//        if (isTenantIsolation())
//            sql = TenantService.addTenantIdQuery(sql);

        if (where != null)
            sql = sql.replace(DUMMY_STR, DUMMY_STR + where);

        setSql(sql);
        return this;
    }
}
