package org.qzproject.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.qzproject.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import org.qzproject.dynamic.thread.pool.sdk.registry.IRegistryService;
import org.qzproject.dynamic.thread.pool.sdk.registry.zookeeper.ZookeeperRegistryService;
import org.qzproject.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import org.qzproject.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustZookeeperListener;
import org.redisson.Redisson;
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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableConfigurationProperties({DynamicThreadPoolRedisProperties.class, DynamicThreadPoolZooKeeperProperties.class})
@EnableScheduling
@SpringBootConfiguration
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);
    private String applicationName;

    @Bean("dynamicThreadRedissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolRedisProperties properties) {
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

    @Bean(name = "dynamicThreadZookeeperClient")
    public CuratorFramework curatorFramework(DynamicThreadPoolZooKeeperProperties properties) {
        ExponentialBackoffRetry backoffRetry = new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(), properties.getMaxRetries());
        CuratorFramework zookeeperClient = CuratorFrameworkFactory.builder()
                .connectString(properties.getConnectString())
                .retryPolicy(backoffRetry)
                .sessionTimeoutMs(properties.getSessionTimeoutMs())
                .connectionTimeoutMs(properties.getConnectionTimeoutMs())
                .build();
        zookeeperClient.start();
        return zookeeperClient;
    }

    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap, CuratorFramework zookeeperClient) {
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if (StringUtils.isBlank(applicationName)) {
            applicationName = "empty name";
            logger.warn("Dynamic thread pool start warning: SpringBoot spring.application.name is not config");
        }

        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();

        for (String threadPoolKey : threadPoolKeys) {
            try {
                String zookeeperPath = "/" + RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "/" + applicationName + "/" + threadPoolKey;
                if(zookeeperClient.checkExists().forPath(zookeeperPath)==null) continue;
                String jsonString = new String(zookeeperClient.getData().forPath(zookeeperPath), StandardCharsets.UTF_8);
                ThreadPoolConfigEntity threadPoolConfigEntity = JSON.parseObject(jsonString, ThreadPoolConfigEntity.class);
                ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
                threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
                threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
            } catch (Exception e) {
                logger.error("error when load thread config data from zookeeper");
            }
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }
    /*
    @Bean
    public IRegistryService redisRegistry(RedissonClient redissonClient) {
        return new RedisRegistryService(redissonClient);
    }
    */
    @Bean
    public IRegistryService redisRegistry(CuratorFramework curatorFramework) {
        return new ZookeeperRegistryService(curatorFramework);
    }

    /*
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
    */
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registry) {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry);
    }
    @Bean
    public ThreadPoolConfigAdjustZookeeperListener threadPoolConfigAdjustZookeeperListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registryService, CuratorFramework zookeeperClient, ApplicationContext applicationContext)
    {
        return new ThreadPoolConfigAdjustZookeeperListener(dynamicThreadPoolService,registryService,zookeeperClient,applicationContext);
    }
}
