package org.qzproject.dynamic.thread.pool.sdk.trigger.listener;

import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.qzproject.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import org.qzproject.dynamic.thread.pool.sdk.registry.IRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.prefs.NodeChangeListener;

public class ThreadPoolConfigAdjustZookeeperListener {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustZookeeperListener.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegistryService registry;

    private final CuratorFramework zookeeperClient;

    private final NodeCache nodeCache;

    private final String applicationName;

    public ThreadPoolConfigAdjustZookeeperListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registry, CuratorFramework zookeeperClient, ApplicationContext applicationContext) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
        this.zookeeperClient = zookeeperClient;
        this.applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        this.nodeCache = new NodeCache(zookeeperClient, "/" + RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY + "/" + applicationName);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                final byte[] data = nodeCache.getCurrentData().getData();
                String changedData = new String(data, StandardCharsets.UTF_8);
                handleChangedData(changedData);
            }
        });
        try {
            this.nodeCache.start();
            logger.info("start zookeeper listener");
        } catch (Exception e) {
            logger.error("error when set zookeeper listener");
        }
    }

    public void handleChangedData(String changedData) {
        // 在这里处理接收到的消息
        List<ThreadPoolConfigEntity> threadPoolConfigEntities= JSON.parseArray(changedData,ThreadPoolConfigEntity.class);
        logger.info("get message");
        System.out.println("处理消息: " + changedData);
    }

}
