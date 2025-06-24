package com.ajaxjs.data.crud;

import com.ajaxjs.data.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FastCRUD 控制器
 */
public interface FastCRUD_Controller {
    /**
     * 单笔详情
     *
     * @param namespace 实体的命名空间
     * @param id        实体 id
     * @return 实体 Map
     */
    @GetMapping("/{namespace}/{id}")
    Map<String, Object> info(@PathVariable String namespace, @PathVariable Long id);


    /**
     * 实体列表
     *
     * @param namespace 实体的命名空间
     * @return 实体列表
     */
    @GetMapping("/{namespace}/list")
    List<Map<String, Object>> list(@PathVariable String namespace);

    /**
     * 分页获取实体列表
     *
     * @param namespace 实体的命名空间
     * @return 实体列表
     */
    @GetMapping("/{namespace}/page")
    PageResult<Map<String, Object>> page(@PathVariable String namespace);

    /**
     * 创建实体
     *
     * @param namespace 实体的命名空间
     * @param params    实体
     * @return 实体 id
     */
    @PostMapping("/{namespace}")
    Long create(@PathVariable String namespace, @RequestParam Map<String, Object> params);

    /**
     * 修改实体
     *
     * @param namespace 实体的命名空间
     * @param params    实体
     * @return 是否成功
     */
    @PutMapping("/{namespace}")
    Boolean update(@PathVariable String namespace, @RequestParam Map<String, Object> params);

    /**
     * 删除实体
     *
     * @param namespace 实体的命名空间
     * @param id        实体 id
     * @return 是否成功
     */
    @DeleteMapping("/{namespace}/{id}")
    Boolean delete(@PathVariable String namespace, @PathVariable Long id);
}