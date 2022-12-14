/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.core.base;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Worker configuration properties.
 *
 * @author Ponfee
 */
@ConfigurationProperties(prefix = JobConstants.HTTP_KEY_PREFIX)
@Data
public class HttpProperties {

    /**
     * Http rest connect timeout milliseconds, default 2000.
     */
    private int connectTimeout = 2000;

    /**
     * Http rest read timeout milliseconds, default 5000.
     */
    private int readTimeout = 5000;

    /**
     * Http rest max retry times, default 3.
     */
    private int maxRetryTimes = 3;

}
