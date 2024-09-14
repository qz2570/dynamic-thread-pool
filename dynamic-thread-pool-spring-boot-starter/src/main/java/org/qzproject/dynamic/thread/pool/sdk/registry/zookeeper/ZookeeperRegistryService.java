package org.qzproject.dynamic.thread.pool.sdk.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.qzproject.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import org.qzproject.dynamic.thread.pool.sdk.registry.IRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZookeeperRegistryService implements IRegistryService {

    private final CuratorFramework zookeeperClient;

    private final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    public ZookeeperRegistryService(CuratorFramework zookeeperClient) {
        this.zookeeperClient = zookeeperClient;
    }

    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities) {
        if (threadPoolEntities.isEmpty())
            return;
        String zookeeperPath = "/" + RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY + "/" + threadPoolEntities.get(0).getAppName();
        try {
            if (zookeeperClient.checkExists().forPath(zookeeperPath) != null) {
                String jsonString = new String(zookeeperClient.getData().forPath(zookeeperPath), StandardCharsets.UTF_8);
                if(!jsonString.equals(JSON.toJSONString(threadPoolEntities)))
                    zookeeperClient.setData().forPath(zookeeperPath, JSON.toJSONString(threadPoolEntities).getBytes(StandardCharsets.UTF_8));
            } else
                zookeeperClient.create().creatingParentsIfNeeded().forPath(zookeeperPath, JSON.toJSONString(threadPoolEntities).getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            logger.error("error when store thread pool to zookeeper {}", JSON.toJSONString(threadPoolEntities));
        }
    }

    @Override
    public void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if (null == threadPoolConfigEntity)
            return;
        String zookeeperPath = "/" + RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY + "/" + threadPoolConfigEntity.getAppName() + "/" + threadPoolConfigEntity.getThreadPoolName();
        try {
            if (zookeeperClient.checkExists().forPath(zookeeperPath) != null) {
                String jsonString = new String(zookeeperClient.getData().forPath(zookeeperPath), StandardCharsets.UTF_8);
                if(!jsonString.equals(JSON.toJSONString(threadPoolConfigEntity)))
                    zookeeperClient.setData().forPath(zookeeperPath, JSON.toJSONString(threadPoolConfigEntity).getBytes(StandardCharsets.UTF_8));
            } else
                zookeeperClient.create().creatingParentsIfNeeded().forPath(zookeeperPath, JSON.toJSONString(threadPoolConfigEntity).getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            logger.error("error when store thread pool config parameters to zookeeper {}", JSON.toJSONString(threadPoolConfigEntity));
        }
    }
}
