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
package com.ajaxjs.sqlman.v1;

import com.ajaxjs.sqlman.model.CreateResult;
import com.ajaxjs.sqlman.model.UpdateResult;

import java.io.Serializable;

/**
 * 通用实体快速的 CRUD。这个服务无须 DataService
 * 提供默认 CRUD 的逻辑，包含常见情况的 SQL。
 */
public interface IEntity extends DAO {
    /**
     * 查询单笔记录，以 Java Bean 格式返回
     *
     * @return 查询单笔记录，可以是 Bean 或者 Map，如果为 null 表示没数据
     */
    <T extends Serializable> Entity info(T id);

    /**
     * 输入实体
     *
     * @param entity 实体，可以是 Map or Java Bean
     * @return
     */
    IEntity input(Object entity);

    /**
     * 新建记录
     *
     * @param idTypeClz ID 类型的类引用
     * @return 新增主键，为兼顾主键类型，返回的类型设为同时兼容 int/long/string 的 Serializable
     */
    <T extends Serializable> CreateResult<T> create(Class<T> idTypeClz);

    /**
     * 新建记录
     *
     * @return 新建的结果，不关心新增主键
     */
    CreateResult<?> create();

    UpdateResult update(String where);
}
