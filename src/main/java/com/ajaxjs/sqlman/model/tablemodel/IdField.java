package com.ajaxjs.sqlman.model.tablemodel;

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
    private boolean isAutoIns = true;

    /**
     * ID 类型，1=自增；2=雪花；3=UUID
     */
    private IdType idType;

    /**
     * ID 类型的类引用
     */
    private Class<? extends Serializable> idTypeClz = Integer.class;
}
