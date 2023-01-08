/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.registry.consul.configuration;

import cn.ponfee.scheduler.core.base.JobConstants;
import cn.ponfee.scheduler.core.base.Supervisor;
import cn.ponfee.scheduler.core.base.Worker;
import cn.ponfee.scheduler.registry.SupervisorRegistry;
import cn.ponfee.scheduler.registry.WorkerRegistry;
import cn.ponfee.scheduler.registry.consul.ConsulServerRegistry;
import cn.ponfee.scheduler.registry.consul.ConsulSupervisorRegistry;
import cn.ponfee.scheduler.registry.consul.ConsulWorkerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;

import static cn.ponfee.scheduler.core.base.JobConstants.SPRING_BEAN_NAME_CURRENT_SUPERVISOR;
import static cn.ponfee.scheduler.core.base.JobConstants.SPRING_BEAN_NAME_CURRENT_WORKER;

/**
 * Consul server register & discovery configuration.
 *
 * @author Ponfee
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ConsulServerRegistry.class})
public class ConsulServerRegistryConfigure {

    /**
     * Configuration consul supervisor registry.
     */
    @Configuration(proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    @DependsOn(SPRING_BEAN_NAME_CURRENT_SUPERVISOR)
    @ConditionalOnBean({Supervisor.class})
    public static class ConsulSupervisorRegistryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public SupervisorRegistry supervisorRegistry(@Value("${" + JobConstants.SCHEDULER_NAMESPACE + ":}") String namespace,
                                                     ConsulRegistryProperties config) {
            return new ConsulSupervisorRegistry(namespace, config);
        }
    }

    /**
     * Configuration consul worker registry.
     */
    @Configuration(proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    @DependsOn(SPRING_BEAN_NAME_CURRENT_WORKER)
    @ConditionalOnBean({Worker.class})
    public static class ConsulWorkerRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public WorkerRegistry workerRegistry(@Value("${" + JobConstants.SCHEDULER_NAMESPACE + ":}") String namespace,
                                             ConsulRegistryProperties config) {
            return new ConsulWorkerRegistry(namespace, config);
        }
    }

}
