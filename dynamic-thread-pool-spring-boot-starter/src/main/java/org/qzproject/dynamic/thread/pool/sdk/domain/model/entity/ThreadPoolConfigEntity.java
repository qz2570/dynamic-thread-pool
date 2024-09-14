package org.qzproject.dynamic.thread.pool.sdk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    public ThreadPoolConfigEntity(String appName, String threadPoolName) {
        this.appName = appName;
        this.threadPoolName = threadPoolName;
    }


}

