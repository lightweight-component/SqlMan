package com.ajaxjs.sqlman.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateResult extends Result {
    /**
     * 操作成功后 影响的行数
     */
    private int effectedRows;
}
