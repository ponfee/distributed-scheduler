/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.redis;

import cn.ponfee.scheduler.common.base.exception.CheckedThrowing;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

/**
 * Embedded redis server.
 * <p><a href="https://github.com/ponfee/embedded-redis">github embedded redis</a>
 * <p><a href="https://blog.csdn.net/qq_45565645/article/details/125052006">redis configuration1</a>
 * <p><a href="https://huaweicloud.csdn.net/633564b3d3efff3090b55531.html">redis configuration2</a>
 *
 * @author Ponfee
 */
public final class EmbeddedRedisServerKstyrc {

    public static void main(String[] args) {
        RedisServer redisServer = start();
        Runtime.getRuntime().addShutdownHook(new Thread(CheckedThrowing.runnable(redisServer::stop)));
    }

    public static RedisServer start() {
        RedisServer redisServer = RedisServerBuilder.newBuilder()
            //.redisExecProvider(customRedisProvider)
            .port(6379)
            .slaveOf("localhost", 6378)
            .setting("requirepass 123456")

            // redis 6.0 ACL: https://blog.csdn.net/qq_29235677/article/details/121475204
            //   command: "ACL SETUSER username on >password ~<key-pattern> +@<category>"
            //   config file: "user username on >password ~<key-pattern> +@<category>"
            //.setting("ACL SETUSER test123 on >123456 ~* +@all")

            .setting("daemonize no")
            .setting("appendonly no")
            .setting("slave-read-only no")
            .setting("maxmemory 128M")
            .build();

        System.out.println("Embedded kstyrc redis server starting...");
        redisServer.start();
        System.out.println("Embedded kstyrc redis server started!");

        return redisServer;
    }

}
