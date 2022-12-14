/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.dispatch.http;

import cn.ponfee.scheduler.common.base.TimingWheel;
import cn.ponfee.scheduler.common.spring.MarkRpcController;
import cn.ponfee.scheduler.core.param.ExecuteParam;
import cn.ponfee.scheduler.dispatch.TaskReceiver;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Task receiver based http.
 *
 * @author Ponfee
 */
public class HttpTaskReceiver extends TaskReceiver implements MarkRpcController {

    public HttpTaskReceiver(TimingWheel<ExecuteParam> timingWheel) {
        super(timingWheel);
    }

    @PostMapping(Constants.WORKER_RECEIVE_PATH)
    @Override
    public boolean receive(ExecuteParam executeParam) {
        return super.receive(executeParam);
    }

}
