package com.ajaxjs.data.data_service;

import com.ajaxjs.data.DataAccessObject;
import com.ajaxjs.data.PageResult;
import com.ajaxjs.data.SmallMyBatis;
import com.ajaxjs.data.crud.CRUD_Service;
import com.ajaxjs.data.crud.FastCRUD;
import com.ajaxjs.data.crud.FastCRUD_Service;
import com.ajaxjs.data.crud.TableModel;
import com.ajaxjs.data.jdbc_helper.JdbcWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 数据服务
 */
@Slf4j
@Data
public abstract class DataService implements DataServiceController {
    private DataAccessObject dao;

    public final Map<String, DataServiceConfig> namespaces = new HashMap<>();

    /**
     * 根据命名空间获取特定的 DataServiceConfig
     *
     * @param namespace 第一个命名空间标识
     * @return 返回根据两个命名空间标识找到的 DataServiceConfig
     */
    private DataServiceConfig getConfig(String namespace) {
        if (!namespaces.containsKey(namespace))
            throw new IllegalStateException("命名空间 " + namespace + " 没有配置 DataServiceConfig");

        return namespaces.get(namespace);
    }

    /**
     * 根据命名空间获取特定的 DataServiceConfig
     *
     * @param namespace  第一个命名空间标识
     * @param namespace2 第二个命名空间标识
     * @return 返回根据两个命名空间标识找到的 DataServiceConfig
     */
    private DataServiceConfig getConfig(String namespace, String namespace2) {
        if (!namespaces.containsKey(namespace)) // 检查第一个命名空间是否配置了 BaseCRUD，如果没有配置则抛出异常
            throw new IllegalStateException("命名空间 " + namespace + " 没有配置 BaseCRUD");

        DataServiceConfig parent = namespaces.get(namespace); // 通过第一个命名空间获取 BaseCRUD 实例
        DataServiceConfig cfg = parent.getChildren().get(namespace2);     // 通过第二个命名空间从父实例的子实例映射中获取特定的 BaseCRUD 实例

        if (cfg == null) // 检查第二个命名空间是否配置了 BaseCRUD，如果没有配置则抛出异常
            throw new IllegalStateException("命名空间 " + namespace2 + " 没有配置 BaseCRUD");

        return cfg;
    }

    public interface CMD_TYPE {
        String SINGLE = "SINGLE";

        String CRUD = "CRUD";
    }

    private static boolean isSingle(DataServiceConfig cfg) {
        return CMD_TYPE.SINGLE.equals(cfg.getType());
    }

    private FastCRUD<Map<String, Object>, Long> initFastCRUD(DataServiceConfig config) {
        FastCRUD<Map<String, Object>, Long> crud = new FastCRUD<>();
        BeanUtils.copyProperties(config, crud);
        crud.setDao(getDao());
        crud.setBeforeCreate(beforeCreate);
        crud.setBeforeUpdate(beforeUpdate);
        crud.setBeforeDelete(beforeDelete);

        return crud;
    }

    private Map<String, Object> info(DataServiceConfig config, Long id) {
        FastCRUD<Map<String, Object>, Long> crud = initFastCRUD(config);

        if (isSingle(config))
            crud.setInfoSql(config.getSql());

        return crud.infoMap(id);
    }

    @Override
    public Map<String, Object> info(String namespace, Long id) {
        DataServiceConfig config = getConfig(namespace);

        return info(config, id);
    }

    @Override
    public Map<String, Object> info(String namespace, String namespace2, Long id) {
        DataServiceConfig config = getConfig(namespace, namespace2);

        return info(config, id);
    }

    private List<Map<String, Object>> list(DataServiceConfig config) {
        FastCRUD<Map<String, Object>, Long> crud = initFastCRUD(config);

        if (isSingle(config))
            crud.setListSql(config.getSql());

        String where = FastCRUD_Service.getWhereClause(Objects.requireNonNull(DataServiceUtils.getRequest()));
        System.out.println("ds:" + getDao().hashCode());

        return crud.listMap(where);
    }

    @Override
    public List<Map<String, Object>> list(String namespace) {
        DataServiceConfig config = getConfig(namespace);

        return list(config);
    }

    @Override
    public List<Map<String, Object>> list(String namespace, String namespace2) {
        DataServiceConfig config = getConfig(namespace, namespace2);

        return list(config);
    }

    private PageResult<Map<String, Object>> page(DataServiceConfig config) {
        FastCRUD<Map<String, Object>, Long> crud = initFastCRUD(config);

        if (isSingle(config))
            crud.setListSql(config.getSql());

        String where = FastCRUD_Service.getWhereClause(Objects.requireNonNull(DataServiceUtils.getRequest()));

        return crud.pageMap(where);
    }

    @Override
    public PageResult<Map<String, Object>> page(String namespace) {
        DataServiceConfig config = getConfig(namespace);

        return page(config);
    }

    @Override
    public PageResult<Map<String, Object>> page(String namespace, String namespace2) {
        DataServiceConfig config = getConfig(namespace, namespace2);

        return page(config);
    }

    private Long create(DataServiceConfig config, Map<String, Object> params) {
        if (isSingle(config))
            config.setCreateSql(config.getSql());

        if (beforeCreate != null)
            beforeCreate.accept(params);

        if (StringUtils.hasText(config.getCreateSql())) {
            String sql = SmallMyBatis.handleSql(config.getCreateSql(), params);
            JdbcWriter jdbcWriter = ((CRUD_Service) dao).getWriter();

            return (Long) jdbcWriter.insert(sql);
        } else {// 无 SQL
            FastCRUD<Map<String, Object>, Long> crud = initFastCRUD(config);

            return crud.create(params);
        }
    }

    @Override
    public Long create(String namespace, Map<String, Object> params) {
        DataServiceConfig config = getConfig(namespace);
        final Map<String, Object> _params = DataServiceUtils.initParams(params, true);

        return create(config, _params);
    }

    @Override
    public Long create(String namespace, String namespace2, Map<String, Object> params) {
        DataServiceConfig config = getConfig(namespace, namespace2);
        final Map<String, Object> _params = DataServiceUtils.initParams(params, true);

        return create(config, _params);
    }

    private Boolean update(DataServiceConfig config, Map<String, Object> params) {
        if (isSingle(config))
            config.setUpdateSql(config.getSql());

        if (beforeUpdate != null)
            beforeUpdate.accept(params);

        if (StringUtils.hasText(config.getUpdateSql())) {
            String sql = SmallMyBatis.handleSql(config.getUpdateSql(), params);
            JdbcWriter jdbcWriter = ((CRUD_Service) dao).getWriter();

            return jdbcWriter.write(sql) > 0;
        } else {// 无 SQL
            FastCRUD<Map<String, Object>, Long> crud = initFastCRUD(config);

            return crud.update(params);
        }
    }

    @Override
    public Boolean update(String namespace, Map<String, Object> params) {
        DataServiceConfig config = getConfig(namespace);
        final Map<String, Object> _params = DataServiceUtils.initParams(params);

        return update(config, _params);
    }

    @Override
    public Boolean update(String namespace, String namespace2, Map<String, Object> params) {
        DataServiceConfig config = getConfig(namespace, namespace2);
        final Map<String, Object> _params = DataServiceUtils.initParams(params);

        return update(config, _params);
    }

    private Boolean delete(DataServiceConfig config, Long id) {
        if (isSingle(config))
            config.setDeleteSql(config.getSql());

        String sql = config.getUpdateSql();

        if (StringUtils.hasText(sql)) {
            if (beforeDelete != null)
                sql = beforeDelete.apply(config.getTableModel().isHasIsDeleted(), sql);

            sql = SmallMyBatis.handleSql(sql, null);
            JdbcWriter jdbcWriter = ((CRUD_Service) dao).getWriter();

            return jdbcWriter.write(sql, id) > 0;
        } else {// 无 SQL
            FastCRUD<Map<String, Object>, Long> crud = initFastCRUD(config);

            return crud.delete(id);
        }
    }

    @Override
    public Boolean delete(String namespace, Long id) {
        DataServiceConfig config = getConfig(namespace);

        return delete(config, id);
    }

    @Override
    public Boolean delete(String namespace, String namespace2, @PathVariable Long id) {
        DataServiceConfig config = getConfig(namespace, namespace2);

        return delete(config, id);
    }

    /**
     * 从数据库中加载配置。
     * 此方法首先清除现有的配置命名空间。接着，它从数据库中查询所有状态不为1的配置项，并进行处理：
     * - 对查询结果进行排序（按照 pid，以 pid 为-1的项首先排列）；
     * - 遍历排序后的结果，为每个配置项创建一个 CRUD 对象，并根据配置项的 pid 来建立父子关系；
     * - 最后，将所有的配置项按照其所属的命名空间保存到 namespaces 中。
     */
    @Override
    public boolean reloadConfig() {
        namespaces.clear();

        List<DataServiceConfig> list = dao.list(DataServiceConfig.class, "SELECT * FROM ds_common_api WHERE stat != 1");// 从数据库中查询所有状态不为1的配置项
        list.sort(Comparator.comparingInt(DataServiceConfig::getPid)); // 根据pid对配置项进行排序
        Map<Integer, DataServiceConfig> configMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(list)) {
            for (DataServiceConfig config : list) {

//                if (beforeCreate != null)
//                    crud.setBeforeCreate(beforeCreate);
//
//                if (beforeUpdate != null)
//                    crud.setBeforeCreate(beforeUpdate);
//
//                if (beforeDelete != null)
//                    crud.setBeforeDelete(beforeDelete);
                if (config.getTableModel() == null) {
                    TableModel t = new TableModel();
                    t.setHasIsDeleted(false);
                    config.setTableModel(t);
                }

                // 如果 pid 为 -1，表示为顶级配置，将其添加到 namespaces 中，并初始化其 children 属性
                if (config.getPid() == -1) {
                    namespaces.put(config.getNamespace(), config);

                    configMap.put(config.getId(), config);
                    config.setChildren(new HashMap<>());
                } else {
                    DataServiceConfig _crud = configMap.get(config.getPid()); // 查找并添加父级配置项的子配置项

                    if (_crud == null)
                        throw new IllegalStateException("程序错误：没有找到父级");

                    _crud.getChildren().put(config.getNamespace(), config);
                }
            }
        } else log.warn("没有 DataService 的配置");

        log.info("加载 DataService 配置成功！");

        return true;
    }

    /**
     * 创建之前的执行的回调函数，可以设置 createDate, createBy 等字段
     */
    @Autowired(required = false)
    @Qualifier("DS_beforeCreate")
    private Consumer<Map<String, Object>> beforeCreate;

    /**
     * 创建之前的执行的回调函数，可以设置 updateDate, updateBy 等字段
     */
    @Autowired(required = false)
    @Qualifier("DS_beforeUpdate")
    private Consumer<Map<String, Object>> beforeUpdate;

    /**
     * 删除之前的执行的回调函数，可以设置 updateDate, updateBy 等字段
     */
    @Autowired(required = false)
    @Qualifier("DS_beforeDelete")
    private BiFunction<Boolean, String, String> beforeDelete;
}