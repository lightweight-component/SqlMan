package com.ajaxjs.sqlman.crud.api;


import com.ajaxjs.sqlman.model.PageResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * CRUD 控制器
 */
public interface CrudController {
    /**
     * 单笔详情
     *
     * @param namespace  实体的命名空间
     * @param namespace2 实体的命名空间2
     * @param id         实体 id
     * @return 实体 Map
     */
    @GetMapping({"/{namespace}/{namespace2}/{id}", "/{namespace}/{id}"})
    Map<String, Object> info(@PathVariable String namespace, @PathVariable String namespace2, @PathVariable Long id);

    /**
     * 实体列表
     *
     * @param namespace  实体的命名空间
     * @param namespace2 实体的命名空间2
     * @return 实体列表
     */
    @GetMapping("/{namespace}/{namespace2}/list")
    List<Map<String, Object>> list(@PathVariable String namespace, @PathVariable String namespace2, HttpServletRequest req);

    /**
     * 分页获取实体列表
     *
     * @param namespace  实体的命名空间
     * @param namespace2 实体的命名空间2
     * @return 实体列表
     */
    @GetMapping("/{namespace}/{namespace2}/page")
    PageResult<Map<String, Object>> page(@PathVariable String namespace, @PathVariable String namespace2);

    /**
     * 创建实体
     *
     * @param namespace  实体的命名空间
     * @param namespace2 实体的命名空间2
     * @param params     实体
     * @return 实体 id
     */
    @PostMapping("/{namespace}/{namespace2}")
    Long create(@PathVariable String namespace, @PathVariable String namespace2, @RequestParam Map<String, Object> params);

    /**
     * 修改实体
     *
     * @param namespace  实体的命名空间
     * @param namespace2 实体的命名空间2
     * @param params     实体
     * @return 是否成功
     */
    @PutMapping("/{namespace}/{namespace2}")
    Boolean update(@PathVariable String namespace, @PathVariable String namespace2, @RequestParam Map<String, Object> params);

    /**
     * 删除实体
     *
     * @param namespace  实体的命名空间
     * @param namespace2 实体的命名空间2
     * @param id         实体 id
     * @return 是否成功
     */
    @DeleteMapping("/{namespace}/{namespace2}/{id}")
    Boolean delete(@PathVariable String namespace, @PathVariable String namespace2, @PathVariable Long id);

    /**
     * 重新加载数据库的配置
     *
     * @return 是否成功
     */
    @GetMapping("/reload_config")
    boolean reloadConfig();
}