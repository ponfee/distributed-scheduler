/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.core.base;

import cn.ponfee.scheduler.common.base.model.CodeMsg;

/**
 * Scheduler code message definitions.
 *
 * @author Ponfee
 */
public enum JobCodeMsg implements CodeMsg {

    INVALID_PARAM(400, "Invalid param."),
    SERVER_ERROR(500, "Server error."),

    LOAD_HANDLER_ERROR(1001, "Load job handler error."),
    SPLIT_JOB_FAILED(1002, "Split job failed."),

    JOB_EXECUTE_FAILED(2001, "Job execute failed."),
    PAUSE_TASK_INTERRUPTED(2002, "Task interrupted."),

    ;

    private final int code;
    private final String msg;

    JobCodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

}
