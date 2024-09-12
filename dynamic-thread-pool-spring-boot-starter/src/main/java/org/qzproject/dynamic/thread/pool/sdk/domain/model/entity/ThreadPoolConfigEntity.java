package org.qzproject.dynamic.thread.pool.sdk.domain.model.entity;

public class ThreadPoolConfigEntity {

    /**
     * application name
     */
    private String appName;

    /**
     * thread pool name
     */
    private String threadPoolName;

    /**
     * core thread pool size
     */
    private int corePoolSize;

    /**
     * max thread pool size
     */
    private int maximumPoolSize;

    /**
     * currently alive thread counter
     */
    private int activeCount;

    /**
     * currently thread counter in pool
     */
    private int poolSize;

    /**
     * queue type
     */
    private String queueType;

    /**
     * task in queue
     */
    private int queueSize;

    /**
     * available task in queue
     */
    private int remainingCapacity;

    public ThreadPoolConfigEntity() {
    }

    public ThreadPoolConfigEntity(String appName, String threadPoolName) {
        this.appName = appName;
        this.threadPoolName = threadPoolName;
    }

    public String getAppName() {
        return appName;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(int remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

}

