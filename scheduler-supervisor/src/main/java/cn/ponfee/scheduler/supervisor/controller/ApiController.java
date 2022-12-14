/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.supervisor.controller;

import cn.ponfee.scheduler.common.base.model.Result;
import cn.ponfee.scheduler.core.enums.ExecuteState;
import cn.ponfee.scheduler.core.enums.JobState;
import cn.ponfee.scheduler.core.enums.Operations;
import cn.ponfee.scheduler.core.enums.RunState;
import cn.ponfee.scheduler.core.exception.JobException;
import cn.ponfee.scheduler.core.model.SchedJob;
import cn.ponfee.scheduler.supervisor.manager.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Provides to external invoke, for manage the sched job & track & task
 *
 * @author Ponfee
 */
@RestController
@RequestMapping("api")
public class ApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    private final JobManager jobManager;

    public ApiController(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    // ------------------------------------------------------------------ sched job

    @PostMapping("job/add")
    public Result<Void> addJob(@RequestBody SchedJob job) {
        jobManager.addJob(job);
        return Result.success();
    }

    @PutMapping("job/update")
    public Result<Void> updateJob(@RequestBody SchedJob job) {
        LOG.info("Do updating sched job {}", job.getJobId());
        jobManager.updateJob(job);
        return Result.success();
    }

    @DeleteMapping("job/delete")
    public Result<Void> deleteJob(@RequestParam("jobId") long jobId) {
        LOG.info("Do deleting sched job {}", jobId);
        jobManager.deleteJob(jobId);
        return Result.success();
    }

    @GetMapping("job/get")
    public Result<SchedJob> getJob(@RequestParam("jobId") long jobId) {
        return Result.success(jobManager.getJob(jobId));
    }

    @PostMapping("job/disable")
    public Result<Boolean> disableJob(@RequestParam("jobId") long jobId) {
        LOG.info("Do disable sched job {}", jobId);
        return Result.success(jobManager.changeJobState(jobId, JobState.DISABLE));
    }

    @PostMapping("job/enable")
    public Result<Boolean> enableJob(@RequestParam("jobId") long jobId) {
        LOG.info("Do enable sched job {}", jobId);
        return Result.success(jobManager.changeJobState(jobId, JobState.ENABLE));
    }

    @PostMapping("job/trigger")
    public Result<Void> triggerJob(@RequestParam("jobId") long jobId) throws JobException {
        LOG.info("Do manual trigger the sched job {}", jobId);
        jobManager.trigger(jobId);
        return Result.success();
    }

    // ------------------------------------------------------------------ sched track

    @PostMapping("track/pause")
    public Result<Boolean> pauseTrack(@RequestParam("trackId") long trackId) {
        LOG.info("Do pausing sched track {}", trackId);
        return Result.success(jobManager.pauseTrack(trackId));
    }

    @PostMapping("track/cancel")
    public Result<Boolean> cancelTrack(@RequestParam("trackId") long trackId) {
        LOG.info("Do canceling sched track {}", trackId);
        return Result.success(jobManager.cancelTrack(trackId, Operations.MANUAL_CANCEL));
    }

    @PostMapping("track/resume")
    public Result<Boolean> resumeTrack(@RequestParam("trackId") long trackId) {
        LOG.info("Do resuming sched track {}", trackId);
        return Result.success(jobManager.resume(trackId));
    }

    @PostMapping("track/fresume")
    public Result<Void> forceResumeTrack(@RequestParam("trackId") long trackId) {
        LOG.info("Do force resuming sched track {}", trackId);
        jobManager.forceUpdateState(trackId, RunState.WAITING.value(), ExecuteState.WAITING.value());
        return Result.success();
    }

    @PutMapping("track/fupdate_state")
    public Result<Void> forceUpdateTrackState(@RequestParam("trackId") long trackId,
                                              @RequestParam("trackTargetState") int trackTargetState,
                                              @RequestParam("taskTargetState") int taskTargetState) {
        // verify the state
        RunState.of(trackTargetState);
        ExecuteState.of(taskTargetState);

        LOG.info("Do force update sched track state {} - {} - {}", trackId, trackTargetState, taskTargetState);
        jobManager.forceUpdateState(trackId, trackTargetState, taskTargetState);
        return Result.success();
    }

    @DeleteMapping("track/delete")
    public Result<Void> deleteTrack(@RequestParam("trackId") long trackId) {
        LOG.info("Do deleting sched track {}", trackId);

        jobManager.deleteTrack(trackId);
        return Result.success();
    }

    @GetMapping("track/get")
    public Result<Object[]> getTrack(@RequestParam("trackId") long trackId) {
        return Result.success(new Object[]{
            jobManager.getTrack(trackId),
            jobManager.getTasks(trackId)
        });
    }

}
