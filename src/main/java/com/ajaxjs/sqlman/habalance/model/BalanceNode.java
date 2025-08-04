package com.ajaxjs.sqlman.habalance.model;

import lombok.Data;

/**
 * 负载均衡用的 NodeBean
 */
@Data
public class BalanceNode {
    /**
     * Node Id节点id
     */
    private String nodeId = null;

    /**
     * Node配置的权重，默认是100
     */
    private float weight = 100;

    /**
     * 权重修正系数-1.0 - +1.0
     */
    private float correctionWeight = 0;

    /**
     * 当前权重，运行时的动态权值
     */
    private float currentWeight = 0;

    /**
     * 最大失败次数
     */
    private int maxFails = 0;

    /**
     * 多长时间内出现 max_fails 次失败便认为后端 down 掉了
     */
    private int failTimeout = 0;

    /**
     * Node的downFlag，默认是1
     */
    private int downFlag = 1;

    /**
     * Node的backupFlag，默认是0
     */
    private int backupFlag = 0;
}
