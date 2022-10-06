package cn.ponfee.scheduler.samples;

import cn.ponfee.scheduler.common.base.model.Result;
import cn.ponfee.scheduler.common.util.Jsons;
import cn.ponfee.scheduler.core.exception.JobException;
import cn.ponfee.scheduler.core.handle.Checkpoint;
import cn.ponfee.scheduler.core.handle.JobHandler;
import cn.ponfee.scheduler.core.model.SchedJob;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计任意0<m<=n的[m, n]的素数个数
 *
 * @author Ponfee
 */
public class PrimeCountJobHandler extends JobHandler<Void> {

    private static final long DEFAULT_BLOCK_SIZE = 100_000_000L; // 默认以每块1亿分批统计

    /**
     * 拆分任务，自由控制任务的拆分数量
     *
     * @param job the schedule job
     * @return task list
     * @throws JobException if split occur error
     */
    @Override
    public List<SplitTask> split(SchedJob job) throws JobException {
        JobParam jobParam = Jsons.fromJson(job.getJobParam(), JobParam.class);
        long m = jobParam.getM();
        long n = jobParam.getN();
        long blockSize = jobParam.getBlockSize() == null ? DEFAULT_BLOCK_SIZE : jobParam.getBlockSize();
        Assert.isTrue(m > 0, "Number M must be greater than zero.");
        Assert.isTrue(n >= m, "Number N cannot less than M.");
        Assert.isTrue(blockSize > 0, "Block size must be greater than zero.");
        Assert.isTrue(jobParam.getParallel() > 0, "Parallel must be greater than zero.");

        int parallel = n == m ? 0 : (int) Math.min(((n - m) + blockSize - 1) / blockSize, jobParam.getParallel());
        List<SplitTask> result = new ArrayList<>(parallel);
        for (int i = 0; i < parallel; i++) {
            TaskParam taskParam = new TaskParam();
            taskParam.setStart(m + blockSize * i);
            taskParam.setBlockSize(blockSize);
            taskParam.setStep(blockSize * parallel);
            taskParam.setN(n);
            result.add(new SplitTask(Jsons.toJson(taskParam)));
        }
        return result;
    }

    /**
     * 执行任务的逻辑实现
     *
     * @param checkpoint the checkpoint
     * @return execute result
     * @throws Exception if execute occur error
     */
    @Override
    public Result<Void> execute(Checkpoint checkpoint) throws Exception {
        TaskParam taskParam = Jsons.fromJson(task().getTaskParam(), TaskParam.class);
        long start = taskParam.getStart();
        long blockSize = taskParam.getBlockSize();
        long step = taskParam.getStep();
        long n = taskParam.getN();
        Assert.isTrue(start > 0, "Start must be greater than zero.");
        Assert.isTrue(blockSize > 0, "Block size must be greater than zero.");
        Assert.isTrue(step > 0, "Step must be greater than zero.");
        Assert.isTrue(n > 0, "N must be greater than zero.");

        ExecuteSnapshot execution;
        if (StringUtils.isEmpty(task().getExecuteSnapshot())) {
            execution = new ExecuteSnapshot(start);
        } else {
            execution = Jsons.fromJson(task().getExecuteSnapshot(), ExecuteSnapshot.class);
            if (execution.getNext() == null || execution.isFinished()) {
                Assert.isTrue(execution.isFinished() && execution.getNext() == null, "Invalid execute snapshot data.");
                return Result.SUCCESS;
            }
        }

        long blockStep = blockSize - 1, next = execution.getNext();
        long lastTime = System.currentTimeMillis(), currTime;
        while (next <= n) {
            long count = Prime.MillerRabin.countPrimes(next, Math.min(next + blockStep, n));
            execution.increment(count);

            next += step;
            if (next > n) {
                execution.setNext(null);
                execution.setFinished(true);
            } else {
                execution.setNext(next);
            }

            currTime = System.currentTimeMillis();
            if (execution.isFinished() || (currTime - lastTime) > 5000) {
                checkpoint.checkpoint(task().getTaskId(), Jsons.toJson(execution));
            }
            lastTime = currTime;
        }
        return Result.SUCCESS;
    }

    @Data
    public static class JobParam implements Serializable {
        private long m;
        private long n;
        private Long blockSize; // 分块统计，每块的大小
        private int parallel;   // 并行度：子任务数量
    }

    @Data
    public static class TaskParam implements Serializable {
        private long start;
        private long blockSize;
        private long step;
        private long n;
    }

    @Data
    @NoArgsConstructor
    public static class ExecuteSnapshot implements Serializable {
        private Long next;
        private long count;
        private boolean finished;

        public ExecuteSnapshot(long start) {
            this.next = start;
            this.count = 0;
            this.finished = false;
        }

        public void increment(long delta) {
            this.count += delta;
        }
    }

}
