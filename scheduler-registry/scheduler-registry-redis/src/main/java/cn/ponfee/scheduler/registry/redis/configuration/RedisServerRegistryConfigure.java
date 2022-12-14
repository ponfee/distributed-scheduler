/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.registry.redis.configuration;

import cn.ponfee.scheduler.core.base.JobConstants;
import cn.ponfee.scheduler.core.base.Supervisor;
import cn.ponfee.scheduler.core.base.Worker;
import cn.ponfee.scheduler.registry.SupervisorRegistry;
import cn.ponfee.scheduler.registry.WorkerRegistry;
import cn.ponfee.scheduler.registry.redis.RedisServerRegistry;
import cn.ponfee.scheduler.registry.redis.RedisSupervisorRegistry;
import cn.ponfee.scheduler.registry.redis.RedisWorkerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

import static cn.ponfee.scheduler.core.base.JobConstants.SPRING_BEAN_NAME_CURRENT_SUPERVISOR;
import static cn.ponfee.scheduler.core.base.JobConstants.SPRING_BEAN_NAME_CURRENT_WORKER;

/**
 * Redis server register & discovery configuration.
 *
 * @author Ponfee
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({StringRedisTemplate.class, RedisServerRegistry.class})
public class RedisServerRegistryConfigure {

    /**
     * Configuration redis supervisor registry.
     */
    @Configuration(proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    @DependsOn(SPRING_BEAN_NAME_CURRENT_SUPERVISOR)
    @ConditionalOnBean({Supervisor.class})
    public static class RedisSupervisorRegistryConfiguration {

        /**
         * RedisAutoConfiguration has auto-configured two redis template objects.
         * <p>RedisTemplate<Object, Object> redisTemplate
         * <p>StringRedisTemplate           stringRedisTemplate
         *
         * @param stringRedisTemplate the auto-configured redis template by spring container
         * @return SupervisorRegistry
         * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
         */
        @Bean
        @ConditionalOnMissingBean
        public SupervisorRegistry supervisorRegistry(@Value("${" + JobConstants.SCHEDULER_NAMESPACE + ":}") String namespace,
                                                     StringRedisTemplate stringRedisTemplate) {
            return new RedisSupervisorRegistry(namespace, stringRedisTemplate);
        }
    }

    /**
     * Configuration redis worker registry.
     */
    @Configuration(proxyBeanMethods = false)
    @AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
    @DependsOn(SPRING_BEAN_NAME_CURRENT_WORKER)
    @ConditionalOnBean({Worker.class})
    public static class RedisWorkerRegistryConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public WorkerRegistry workerRegistry(@Value("${" + JobConstants.SCHEDULER_NAMESPACE + ":}") String namespace,
                                             StringRedisTemplate stringRedisTemplate) {
            return new RedisWorkerRegistry(namespace, stringRedisTemplate);
        }
    }

}
