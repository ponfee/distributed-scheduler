package cn.ponfee.scheduler.registry;

import cn.ponfee.scheduler.common.util.ClassUtils;
import cn.ponfee.scheduler.core.base.JobConstants;
import cn.ponfee.scheduler.core.base.Server;
import cn.ponfee.scheduler.core.base.Supervisor;
import cn.ponfee.scheduler.core.base.Worker;
import org.springframework.util.Assert;

import java.lang.reflect.Modifier;

/**
 * Server role definition.
 *
 * @author Ponfee
 */
public enum ServerRole {

    /**
     * Worker
     */
    WORKER(Worker.class, JobConstants.SCHEDULER_KEY_PREFIX + ".workers"),

    /**
     * Supervisor
     */
    SUPERVISOR(Supervisor.class, JobConstants.SCHEDULER_KEY_PREFIX + ".supervisors"),

    ;

    private final Class<? extends Server> type;
    private final String registryKey;

    ServerRole(Class<? extends Server> type, String registryKey) {
        Assert.isTrue(!Modifier.isAbstract(type.getModifiers()), "Server type cannot be abstract class: " + type);
        this.type = type;
        this.registryKey = registryKey;
    }

    public <T extends Server> Class<T> type() {
        return (Class<T>) type;
    }

    public String registryKey() {
        return registryKey;
    }

    public <T extends Server> T deserialize(String text) {
        return ClassUtils.invoke(type(), "deserialize", new Object[]{text});
    }

    public static ServerRole of(Class<? extends Server> type) {
        for (ServerRole value : values()) {
            if (type == value.type) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown server type: " + type);
    }

}