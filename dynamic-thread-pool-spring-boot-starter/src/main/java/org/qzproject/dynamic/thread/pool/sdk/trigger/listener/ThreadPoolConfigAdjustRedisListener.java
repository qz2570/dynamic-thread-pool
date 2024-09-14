package org.qzproject.dynamic.thread.pool.sdk.trigger.listener;

import com.alibaba.fastjson.JSON;
import org.qzproject.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.qzproject.dynamic.thread.pool.sdk.registry.IRegistryService;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ThreadPoolConfigAdjustRedisListener implements MessageListener<ThreadPoolConfigEntity> {

    private Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustRedisListener.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegistryService registry;

    public ThreadPoolConfigAdjustRedisListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    @Override
    public void onMessage(CharSequence charSequence, ThreadPoolConfigEntity threadPoolConfigEntity) {
        logger.info("Dynamic thread pool, update thread pool parameters:{} core thread size:{} max thread size:{}", threadPoolConfigEntity.getThreadPoolName(), threadPoolConfigEntity.getCorePoolSize(), threadPoolConfigEntity.getMaximumPoolSize());
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);

        // update new parameters
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);

        ThreadPoolConfigEntity threadPoolConfigEntityCurrent = dynamicThreadPoolService.queryThreadPoolConfigByName(threadPoolConfigEntity.getThreadPoolName());
        registry.reportThreadPoolConfigParameter(threadPoolConfigEntityCurrent);
        logger.info("Dynamic thread pool, update new config to redis registry：{}", JSON.toJSONString(threadPoolConfigEntity));
    }

}

