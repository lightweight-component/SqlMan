package com.ajaxjs.sqlman.habalance;

import com.ajaxjs.sqlman.habalance.model.BalanceNode;
import com.ajaxjs.sqlman.habalance.model.NodeHealthValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class Operations implements PropertyChangeListener {
    /**
     * DB从库server列表
     */
    private final ArrayList<BalanceNode> serverList = new ArrayList<>();

    /**
     * DB从库server列表用的readWriteLock
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * 备机列表(查询库)
     */
    protected List<JdbcOperations> crmB_N_JdbcList;

    /**
     * 主机ip(写库)
     */
    protected JdbcOperations crmJdbc;

    public Operations(List<JdbcOperations> crmB_N_JdbcList, JdbcOperations crmJdbc) {
        this.crmB_N_JdbcList = crmB_N_JdbcList;
        this.crmJdbc = crmJdbc;
        int len = crmB_N_JdbcList.size(); // 生成相应的serverList
        BalanceNode nodeBean;

        for (int i = 0; i < len; i++) {
            nodeBean = new BalanceNode();
            nodeBean.setDownFlag(0); // 初始状态为正常
            nodeBean.setCorrectionWeight(0);
            nodeBean.setCurrentWeight(0);

            serverList.add(nodeBean);
        }

        int threadNum = 1;  // Thread数
        ExecutorService threadPool = Executors.newFixedThreadPool(threadNum); // 新建一个 ThreadPool
        HealthCheckTask task;

        for (int i = 0; i < threadNum; i++) {//使用threadNum个线程去监听jdbcTemplate列表的健康状态,如果出现健康值变动,触发监听器修改节点的健康状态
            task = new HealthCheckTask(crmB_N_JdbcList);// 新作成一个Task
            task.addPropertyChangeListener(this);
            threadPool.execute(task);   // 提交task到ThreadPool
        }
    }

    /**
     * healthyValue属性改变通知处理。 根据healthyValue修改节点的DownFlag和权重修正系数。
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equalsIgnoreCase("healthyValue")) {
            Object newBean = event.getNewValue();

            if (newBean instanceof NodeHealthValue) {
                NodeHealthValue valueBean = (NodeHealthValue) newBean;
                int nodeIndex = valueBean.getNodeIndex(), healthValue = valueBean.getHealthValue();

                if (healthValue < 0)
                    setNodeDownFlag(nodeIndex, 1);  // 故障发生
                else
                    setNodeDownFlag(nodeIndex, 0);// 故障恢复

                float correctionWeight = 0;  // 权重修正系数

                if (healthValue == 0)
                    correctionWeight = 0;
                else if (healthValue == 1)
                    correctionWeight = -0.1f;
                else if (healthValue == 2)
                    correctionWeight = -0.3f;
                else if (healthValue == 3)
                    correctionWeight = -0.5f;
                else if (healthValue == 4)
                    correctionWeight = -1.0f;

                setNodeCorrectionWeight(nodeIndex, correctionWeight); // 设置节点的权重修正系数
                log.warn("nodeIndex=" + nodeIndex + ",healthValue=" + healthValue + ",correctionWeight=" + correctionWeight);
            }
        }
    }

    /**
     * 设置节点的 downFlag
     */
    public void setNodeDownFlag(int nodeIndex, int downFlag) {
        if (nodeIndex < 0 || nodeIndex >= serverList.size())
            return;

        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();

        try {
            BalanceNode nodeBean = serverList.get(nodeIndex);
            nodeBean.setDownFlag(downFlag);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 设置节点的权重修正系数
     */
    public void setNodeCorrectionWeight(int nodeIndex, float correctionWeight) {
        if (nodeIndex < 0 || nodeIndex >= serverList.size())
            return;

        Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();

        try {
            BalanceNode nodeBean = serverList.get(nodeIndex);
            nodeBean.setCorrectionWeight(correctionWeight);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 使用负载均衡器取得一个可用 JdbcOperations
     */
    protected JdbcOperations getJdbcOperations() {
        JdbcOperations result = null;
        int resultIndex;

        Lock readLock = readWriteLock.readLock();
        readLock.lock();

        try {
            resultIndex = getForwardServerIndex(serverList);  // 取得一个 server index
            //log.warn("resultIndex=" + resultIndex);
        } finally {
            readLock.unlock();
        }

        log.info("<<<<<<<<<<<<<<<<<<<<获取的负载均衡jdbcOperations节点resultIndex:" + resultIndex);
        if (resultIndex >= 0)
            result = crmB_N_JdbcList.get(resultIndex);

        return result;
    }

    /**
     * 取得将使用的 BalanceNode index
     */
    public static int getForwardServerIndex(ArrayList<BalanceNode> serverList) {
        if (CollectionUtils.isEmpty(serverList))
            return -1;

        int len = serverList.size(), result = -1;

        if (len == 1) {
            // (2)仅有一台server时
            if (serverList.get(0).getDownFlag() == 1)
                result = -1; // 无可用的server
            else
                // 仅这一台
                result = 0;

            return result;
        }

        // (3)有多台后端服务器
        BalanceNode bestBean = null, tempBean;

        float currentWeight, totalEffectiveWeight = 0; // 所有服务器总权重

        // 遍历所有服务器, 按照各台服务器的当前权值进行选择
        for (int i = 0; i < len; i++) {
            tempBean = serverList.get(i);

            if (tempBean.getDownFlag() == 1)
                continue; // Down 掉的 server,将被 skip

            // 计算当前权值，包含权重修正系数
            currentWeight = tempBean.getCurrentWeight() + tempBean.getWeight() * (1 + tempBean.getCorrectionWeight());
            tempBean.setCurrentWeight(currentWeight);

            // 总的权重计算，包含权重修正系数
            totalEffectiveWeight += tempBean.getWeight() * (1 + tempBean.getCorrectionWeight());

            // 选择
            if (bestBean == null || bestBean.getCurrentWeight() < tempBean.getCurrentWeight()) {
                bestBean = tempBean; // 选择当前权重最大的 serverNode
                result = i;
            }
        }

        if (bestBean == null)
            return -1; // 无可用的 server
        else {  // 该服务器这次被选中，因此要降低权值,以便下次计算
            currentWeight = bestBean.getCurrentWeight() - totalEffectiveWeight;
            bestBean.setCurrentWeight(currentWeight);
        }

        return result;
    }
}
