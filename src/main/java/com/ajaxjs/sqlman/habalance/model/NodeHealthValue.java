package com.ajaxjs.sqlman.habalance.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 负载均衡用的 Node 健康值 Bean
 */
@Data
public class NodeHealthValue implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Node index
     */
    private int nodeIndex = -1;

    /**
     * Node健康值
     */
    private int healthValue = 0;

    /**
     * Node健康值的最后更新时间
     */
    private Date lastUpdate = new Date();
}
