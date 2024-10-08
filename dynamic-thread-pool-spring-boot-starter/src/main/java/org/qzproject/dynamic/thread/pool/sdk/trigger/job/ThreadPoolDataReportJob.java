package org.qzproject.dynamic.thread.pool.sdk.trigger.job;

import com.alibaba.fastjson.JSON;
import org.qzproject.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.qzproject.dynamic.thread.pool.sdk.registry.IRegistryService;
import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Random;

public class ThreadPoolDataReportJob {

    private final Logger logger = LoggerFactory.getLogger(ThreadPoolDataReportJob.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegistryService registry;

    public ThreadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void execReportThreadPoolList() {

        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);
        logger.info("Dynamic thread pool, insert new thread pool：{}", JSON.toJSONString(threadPoolConfigEntities));

        for (ThreadPoolConfigEntity threadPoolConfigEntity : threadPoolConfigEntities) {
            registry.reportThreadPoolConfigParameter(threadPoolConfigEntity);
            logger.info("dynamic thread pool, insert thread pool parameters：{}", JSON.toJSONString(threadPoolConfigEntity));
        }
    }
}

