/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.core.param;

import cn.ponfee.scheduler.common.base.TimingWheel;
import cn.ponfee.scheduler.common.base.ToJsonString;
import cn.ponfee.scheduler.common.util.GenericUtils;
import cn.ponfee.scheduler.common.util.Jsons;
import cn.ponfee.scheduler.common.util.ObjectUtils;
import cn.ponfee.scheduler.core.base.Worker;
import cn.ponfee.scheduler.core.enums.Operations;
import cn.ponfee.scheduler.core.handle.TaskExecutor;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Task execution parameter.
 *
 * @author Ponfee
 */
@JSONType(deserializer = ExecuteParam.FastjsonDeserializer.class) // fastjson
@JsonDeserialize(using = ExecuteParam.JacksonDeserializer.class)  // jackson
public final class ExecuteParam extends ToJsonString implements TimingWheel.Timing<ExecuteParam>, Serializable {

    private static final long serialVersionUID = -6493747747321536680L;

    private final AtomicReference<Operations> operation;
    private final long taskId;
    private final long trackId;
    private final long jobId;
    private final long triggerTime;

    /**
     * ????????????(JVM??????)
     */
    private Worker worker;

    /**
     * ?????????????????????
     */
    private volatile transient TaskExecutor<?> taskExecutor;

    /**
     * Constructor
     *
     * @param operation   the operation
     * @param taskId      the task id
     * @param trackId     the track id
     * @param jobId       the job id
     * @param triggerTime the trigger time
     */
    public ExecuteParam(Operations operation, long taskId, long trackId, long jobId, long triggerTime) {
        Assert.notNull(operation, "Operation cannot null.");
        this.operation = new AtomicReference<>(operation);
        this.taskId = taskId;
        this.trackId = trackId;
        this.jobId = jobId;
        this.triggerTime = triggerTime;
    }

    /**
     * The constructor for deserialization.
     *
     * @param operation   the operation(if terminate task, this is null value)
     * @param taskId      the task id
     * @param trackId     the track id
     * @param jobId       the job id
     * @param triggerTime the trigger time
     * @param worker      the worker
     */
    private ExecuteParam(Operations operation, long taskId, long trackId, long jobId, long triggerTime, Worker worker) {
        this.operation = new AtomicReference<>(operation);
        this.taskId = taskId;
        this.trackId = trackId;
        this.jobId = jobId;
        this.triggerTime = triggerTime;
        this.worker = worker;
    }

    // ------------------------------------------------getter/setter
    public long getTaskId() {
        return taskId;
    }

    public long getTrackId() {
        return trackId;
    }

    public long getJobId() {
        return jobId;
    }

    public long getTriggerTime() {
        return triggerTime;
    }

    /**
     * For help to deserialization
     *
     * @return AtomicReference
     */
    public AtomicReference<Operations> getOperation() {
        return operation;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    // ------------------------------------------------other methods
    public boolean updateOperation(Operations expect, Operations update) {
        return this.operation.compareAndSet(expect, update);
    }

    public Operations operation() {
        return this.operation.get();
    }

    public void taskExecutor(TaskExecutor<?> taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void interrupt() {
        TaskExecutor<?> executor = this.taskExecutor;
        if (executor != null) {
            executor.interrupt();
        }
    }

    @Override
    public long timing() {
        return triggerTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExecuteParam other = (ExecuteParam) o;
        return this.operation.get() == other.operation.get()
            && this.taskId          == other.taskId
            && this.trackId         == other.trackId
            && this.jobId           == other.jobId
            && this.triggerTime     == other.triggerTime;
    }

    /**
     * Returns is whether same trigger task.
     *
     * @param other the other task
     * @return {@code true} if same trigger task
     */
    public boolean same(ExecuteParam other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return this.taskId      == other.taskId
            && this.trackId     == other.trackId
            && this.jobId       == other.jobId
            && this.triggerTime == other.triggerTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation.get().ordinal(), taskId, trackId, jobId, triggerTime);
    }

    /**
     * Serialize to string
     *
     * @return string of serialized result
     */
    public byte[] serialize() {
        // unnecessary do flip
        return ByteBuffer.allocate(33)
            .put((byte) operation.get().ordinal())
            .putLong(taskId)
            .putLong(trackId)
            .putLong(jobId)
            .putLong(triggerTime)
            .array();
    }

    /**
     * Deserialize from string.
     *
     * @param bytes the serialized byte array
     * @return TaskParam of deserialized result
     */
    public static ExecuteParam deserialize(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return new ExecuteParam(
            Operations.of(buf.get()),
            buf.getLong(),
            buf.getLong(),
            buf.getLong(),
            buf.getLong()
        );
    }


    // -----------------------------------------------------custom fastjson deserialize

    /**
     * Custom deserialize ExecuteParam based fastjson.
     */
    public static class FastjsonDeserializer implements ObjectDeserializer {
        @Override
        public ExecuteParam deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
            if (GenericUtils.getRawType(type) != ExecuteParam.class) {
                throw new UnsupportedOperationException("Cannot supported deserialize type: " + type);
            }
            return castToExecuteParam(parser.parseObject());
        }

        @Override
        public int getFastMatchToken() {
            return 0 /*JSONToken.RBRACKET*/;
        }
    }

    // -----------------------------------------------------custom jackson deserialize

    /**
     * Custom deserialize ExecuteParam based jackson.
     */
    public static class JacksonDeserializer extends JsonDeserializer<ExecuteParam> {
        @Override
        public ExecuteParam deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return castToExecuteParam(p.readValueAs(Jsons.MAP_NORMAL));
        }
    }

    private static ExecuteParam castToExecuteParam(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Operations operation = ObjectUtils.cast(map.get("operation"), Operations.class);
        long taskId = ObjectUtils.cast(map.get("taskId"), long.class);
        long trackId = ObjectUtils.cast(map.get("trackId"), long.class);
        long jobId = ObjectUtils.cast(map.get("jobId"), long.class);
        long triggerTime = ObjectUtils.cast(map.get("triggerTime"), long.class);
        Worker worker = Worker.castToWorker((Map<String, Object>) map.get("worker"));
        // operation is null if terminate task
        return new ExecuteParam(operation, taskId, trackId, jobId, triggerTime, worker);
    }

}
