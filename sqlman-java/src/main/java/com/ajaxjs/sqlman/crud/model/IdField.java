package com.ajaxjs.sqlman.crud.model;

import com.ajaxjs.sqlman.JdbcConstants;
import lombok.Data;

import java.io.Serializable;

@Data
public class IdField {
    /**
     * id 的字段名称
     */
    private String idField = "id";

    /**
     * 是否自增 id
     */
    private boolean isAutoIns;

    /**
     * ID 类型，1=自增；2=雪花；3=UUID
     */
    private JdbcConstants.IdType idType;

    /**
     * ID 类型的类引用
     */
    private Class<? extends Serializable> idTypeClz = Integer.class;
}
