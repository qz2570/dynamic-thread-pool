package org.qzproject.dynamic.thread.pool.sdk.trigger.job;

import com.alibaba.fastjson.JSON;
import org.qzproject.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.qzproject.dynamic.thread.pool.sdk.registry.IRegistryService;
import org.redisson.api.RTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ThreadPoolUpdateJob {

    private final Logger logger = LoggerFactory.getLogger(ThreadPoolUpdateJob.class);

    private RTopic rTopic;

    public ThreadPoolUpdateJob(RTopic rTopic) {
        this.rTopic = rTopic;
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void test_dynamicThreadPoolRedisTopic() throws InterruptedException {
        logger.info("Dynamic thread pool, insert new thread pool publish update info");
        Random random = new Random();
        ThreadPoolConfigEntity threadPoolConfigEntity = new ThreadPoolConfigEntity("dynamic-thread-pool-test-app", "threadPoolExecutor01");
        threadPoolConfigEntity.setCorePoolSize(random.nextInt(451) + 50);
        threadPoolConfigEntity.setMaximumPoolSize(random.nextInt(451) + 50);
        rTopic.publish(threadPoolConfigEntity);
    }
}
