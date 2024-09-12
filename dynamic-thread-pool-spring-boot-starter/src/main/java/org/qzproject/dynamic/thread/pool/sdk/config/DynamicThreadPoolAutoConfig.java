package org.qzproject.dynamic.thread.pool.sdk.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qzproject.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import org.qzproject.dynamic.thread.pool.sdk.registry.IRegistryService;
import org.qzproject.dynamic.thread.pool.sdk.registry.redis.RedisRegistryService;
import org.qzproject.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import org.qzproject.dynamic.thread.pool.sdk.trigger.job.ThreadPoolUpdateJob;
import org.qzproject.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableConfigurationProperties(DynamicThreadPoolAutoConfigProperties.class)
@EnableScheduling
@SpringBootConfiguration
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);
    private String applicationName;
    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if (StringUtils.isBlank(applicationName)) {
            applicationName = "empty name";
            logger.warn("Dynamic thread pool start warning: SpringBoot spring.application.name is not config");
        }

        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        /**
        for (String threadPoolKey : threadPoolKeys) {
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolKey).get();
            if (null == threadPoolConfigEntity) continue;
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        }**/

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

    @Bean("dynamicThreadRedissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoConfigProperties properties) {
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);
        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;
        RedissonClient redissonClient = Redisson.create(config);
        logger.info("Dynamic thread pool redis registry initialized {} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());
        return redissonClient;
    }

    @Bean
    public IRegistryService redisRegistry(RedissonClient dynamicThreadRedissonClient) {
        return new RedisRegistryService(dynamicThreadRedissonClient);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registry) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }

    @Bean
    public RTopic rTopic(RedissonClient redissonClient, ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        topic.addListener(ThreadPoolConfigEntity.class, threadPoolConfigAdjustListener);
        return topic;
    }

    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registry, RTopic rTopic) {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry, rTopic);
    }
}
