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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.prefs.NodeChangeListener;

public class ThreadPoolConfigAdjustZookeeperListener {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustZookeeperListener.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegistryService registry;

    private final CuratorFramework zookeeperClient;

    private Map<String,NodeCache> nodeCacheMap;

    public ThreadPoolConfigAdjustZookeeperListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistryService registry, CuratorFramework zookeeperClient) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
        this.zookeeperClient = zookeeperClient;
        this.nodeCacheMap=new HashMap<>();
        List<ThreadPoolConfigEntity> threadPoolConfigEntities=dynamicThreadPoolService.queryThreadPoolList();
        for (ThreadPoolConfigEntity threadPoolConfigEntity: threadPoolConfigEntities) {
            String zookeeperPath = "/" + RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY + "/" + threadPoolConfigEntity.getAppName() + "/" + threadPoolConfigEntity.getThreadPoolName();
            NodeCache nodeCache = new NodeCache(zookeeperClient, zookeeperPath);
            nodeCache.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    final byte[] data = nodeCache.getCurrentData().getData();
                    String changedData = new String(data, StandardCharsets.UTF_8);
                    handleChangedData(changedData);
                }
            });
            try {
                nodeCache.start();
                logger.info("start zookeeper listener");
            } catch (Exception e) {
                logger.error("error when set zookeeper listener");
            }
            this.nodeCacheMap.put(threadPoolConfigEntity.getThreadPoolName(),nodeCache);
        }
    }

    public void handleChangedData(String changedData) {
        ThreadPoolConfigEntity threadPoolConfigEntity= JSON.parseObject(changedData,ThreadPoolConfigEntity.class);
        logger.info("get new thread pool config by zookeeper");
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);
        logger.info("update new thread pool config by zookeeper done");
    }

}
