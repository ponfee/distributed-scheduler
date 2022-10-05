package cn.ponfee.scheduler.samples;

import cn.ponfee.scheduler.core.base.HttpProperties;
import cn.ponfee.scheduler.dispatch.redis.configuration.EnableRedisTaskDispatching;
import cn.ponfee.scheduler.registry.consul.configuration.ConsulProperties;
import cn.ponfee.scheduler.registry.consul.configuration.EnableConsulServerRegistry;
import cn.ponfee.scheduler.registry.redis.configuration.EnableRedisServerRegistry;
import cn.ponfee.scheduler.supervisor.configuration.EnableSupervisor;
import cn.ponfee.scheduler.supervisor.configuration.SupervisorProperties;
import cn.ponfee.scheduler.worker.configuration.EnableWorker;
import cn.ponfee.scheduler.worker.configuration.WorkerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Scheduler application based spring boot
 *
 * @author Ponfee
 */
@EnableConfigurationProperties({
    SupervisorProperties.class,
    WorkerProperties.class,
    HttpProperties.class,
    ConsulProperties.class
})
@EnableSupervisor
@EnableWorker
@EnableConsulServerRegistry // EnableRedisServerRegistry、EnableConsulServerRegistry
@EnableRedisTaskDispatching // EnableRedisTaskDispatching、EnableHttpTaskDispatching
@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class
    },
    scanBasePackages = {
        "cn.ponfee.scheduler.supervisor",
        "cn.ponfee.scheduler.samples.config"
    }
)
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

}
