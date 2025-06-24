package com.ajaxjs.data.crud;

import com.ajaxjs.data.PageResult;
import com.ajaxjs.data.data_service.DataServiceUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FastCRUD_Service implements FastCRUD_Controller {
    public final Map<String, FastCRUD<Map<String, Object>, Long>> namespaces = new HashMap<>();

    public boolean isInit;

    public void init() {
    }

    private FastCRUD<Map<String, Object>, Long> getCRUD(String namespace) {
        init();

        if (!namespaces.containsKey(namespace))
            throw new IllegalStateException("命名空间 " + namespace + " 没有配置 BaseCRUD");

        return namespaces.get(namespace);
    }

    @Override
    public Map<String, Object> info(String namespace, Long id) {
        return getCRUD(namespace).infoMap(id);
    }

    @Override
    public List<Map<String, Object>> list(String namespace) {
        String where = getWhereClause(Objects.requireNonNull(DataServiceUtils.getRequest()));

        return getCRUD(namespace).listMap(where);
    }

    @Override
    public PageResult<Map<String, Object>> page(String namespace) {
        String where = getWhereClause(Objects.requireNonNull(DataServiceUtils.getRequest()));

        return getCRUD(namespace).page(where);
    }

    @Override
    public Long create(String namespace, Map<String, Object> params) {
        return getCRUD(namespace).create(params);
    }

    @Override
    public Boolean update(String namespace, Map<String, Object> params) {
        return getCRUD(namespace).update(params);
    }

    @Override
    public Boolean delete(String namespace, Long id) {
        return getCRUD(namespace).delete(id);
    }

    /**
     * 基于 URL 的 QueryString，设计一个条件查询的参数规范，可以转化为 SQL 的 Where 里面的查询
     *
     * @param request 请求对象
     * @return SQL Where 语句
     */
    public static String getWhereClause(HttpServletRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();   // 获取所有 QueryString 参数
        StringBuilder whereClause = new StringBuilder(); // 创建一个用于存储 SQL 查询的 StringBuilder

        // 遍历所有参数
        for (String parameterName : parameters.keySet()) {
            // 跳过不符合条件的参数
            if (!parameterName.startsWith("q_"))
                continue;

            // 获取参数值
            String[] parameterValues = parameters.get(parameterName);

            // 构建 SQL 查询
            whereClause.append(" AND ");
            whereClause.append(parameterName.substring(2));

            // 处理单值参数
            if (parameterValues.length == 1) {
                String value = DataServiceUtils.escapeSqlInjection(parameterValues[0]).trim();
                whereClause.append(" = ");
                whereClause.append("'").append(value).append("'");
            } else {
                // 处理数组参数
                whereClause.append(" IN (");

                if (parameterValues.length > 0) {
                    for (String parameterValue : parameterValues) {
                        whereClause.append("'");
                        whereClause.append(DataServiceUtils.escapeSqlInjection(parameterValue).trim());
                        whereClause.append("',");
                    }

                    whereClause.deleteCharAt(whereClause.length() - 1);
                }

                whereClause.append(")");
            }
        }

        return whereClause.toString();// 返回 SQL 查询
    }
}
