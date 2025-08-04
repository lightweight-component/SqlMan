package com.ajaxjs.sqlman.habalance;

import com.ajaxjs.sqlman.habalance.model.NodeHealthValue;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 高可用DB从库HealthCheck Task
 * 该类实现了Runnable接口,可以独立运行,其作用是监听从库相关jdbcTemplate列表的健康值,如果有变化或者某个从库挂掉会通知其监听器修改jdbcTemplate节点的健康值。
 * 可以看到该类定义了监听管理器,再run方法中5秒执行一次jdbcTemplate节点健康性检查,如果发现当前健康值和节点之前健康值不一致,会触发监听器通知其修改节点健康值属性
 */
@Slf4j
public class HealthCheckTask implements Runnable {
    /**
     * 运行中标志
     */
    private volatile boolean runFlag = true;

    /**
     * DB 从库J dbc 列表
     */
    private final List<JdbcOperations> crmB_N_JdbcList;

    /**
     * Node健康值Bean列表
     */
    private final ArrayList<NodeHealthValue> healthyValueList = new ArrayList<>();

    /**
     * 健康值 PropertyChangeSupport
     */
    protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    public HealthCheckTask(List<JdbcOperations> crmB_N_JdbcList) {
        this.crmB_N_JdbcList = crmB_N_JdbcList; // DB从库Jdbc列表
        int len = crmB_N_JdbcList.size();
        NodeHealthValue nodeBean;

        for (int i = 0; i < len; i++) {
            nodeBean = new NodeHealthValue();
            nodeBean.setNodeIndex(i);
            nodeBean.setHealthValue(0); // 初始状态为正常
            healthyValueList.add(nodeBean);
        }
    }

    /**
     * 添加 健康值 PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * 移除 健康值 PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

    /**
     * 取得健康值
     *
     * @return -1: 表示已经Down; 0-4级;
     */
    private int getHealthyValue(JdbcOperations jdbcOpers) {
        int result = 0;

        try {
            long beforeMS = System.currentTimeMillis();
            jdbcOpers.queryForObject("select 1 from dual", Integer.class); // query 主要用来做DB存活性以及性能探测
            long afterMS = System.currentTimeMillis(), diff = afterMS - beforeMS;

            // log.warn("diff=" + diff);
            if (diff >= 0 && diff <= 500) // 小于500毫秒为0级
                // 执行时长0-500ms内为正常
                result = 0;
            else if (diff > 500 && diff <= 1000) // 500毫秒到1秒之间是1级
                // 执行时长500-1000ms内为降1级
                result = 1;
            else if (diff > 1000 && diff <= 3000) // 1秒到3秒之间是2级
                result = 2;
            else if (diff > 3000 && diff <= 5000) // 3秒到5秒之间是3级
                result = 3;
            else if (diff > 5000) // 大于5秒是4级
                result = 4;
        } catch (Exception e) {// 如果出现异常,认为DB挂掉
            result = -1;
        }

        return result;
    }

    /**
     * 检测所有 Node 的健康值(5秒钟检查一下 )
     */
    @Override
    public void run() {
        while (runFlag) {
            try {
                int len = crmB_N_JdbcList.size(), healthyValue;
                JdbcOperations jdbcOpers;
                NodeHealthValue nodeBean;

                for (int i = 0; i < len; i++) {
                    jdbcOpers = crmB_N_JdbcList.get(i);
                    healthyValue = getHealthyValue(jdbcOpers);// 获得jdbc节点健康值
                    nodeBean = healthyValueList.get(i);// 健康节点

                    if (nodeBean.getHealthValue() == healthyValue) // 上一次节点健康值和当前健康值一致,不需要变动
                        continue;

                    // 节点健康值有变化
                    NodeHealthValue oldValue = new NodeHealthValue();// 节点旧健康值
                    oldValue.setNodeIndex(i);
                    oldValue.setHealthValue(nodeBean.getHealthValue());

                    NodeHealthValue newValue = new NodeHealthValue();// 节点新健康值
                    newValue.setNodeIndex(i);
                    newValue.setHealthValue(healthyValue);

                    PropertyChangeEvent event = new PropertyChangeEvent(this, "healthyValue", oldValue, newValue);
                    listeners.firePropertyChange(event);// 触发监听器操作

                    nodeBean.setHealthValue(healthyValue);// 设置节点的新健康值
                    nodeBean.setLastUpdate(new Date());
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
