package com.ajaxjs.sqlman.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @param <T> id 新增的 id 其类型
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Create<T extends Serializable> extends Result {
    private T newlyId;
}
