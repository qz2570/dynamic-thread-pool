package org.qzproject.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "thread.pool.executor.config", ignoreInvalidFields = true)
public class ThreadPoolConfigProperties {

    /** core thread size */
    private Integer corePoolSize = 20;
    /** max pool size */
    private Integer maxPoolSize = 200;
    /** max alive time */
    private Long keepAliveTime = 10L;
    /** max queue size */
    private Integer blockQueueSize = 5000;
    /** give up task and throw exception**/
    private String policy = "AbortPolicy";

}

