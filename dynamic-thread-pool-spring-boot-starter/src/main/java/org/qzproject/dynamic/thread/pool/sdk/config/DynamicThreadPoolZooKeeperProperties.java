package org.qzproject.dynamic.thread.pool.sdk.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dynamic.thread.pool.zookeeper.config", ignoreInvalidFields = true)
@Getter
@Setter
public class DynamicThreadPoolZooKeeperProperties {

    private String connectString;
    private int baseSleepTimeMs;
    private int maxRetries;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;

}
