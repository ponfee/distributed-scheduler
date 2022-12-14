/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.registry;

import cn.ponfee.scheduler.core.base.Server;

import java.util.List;

/**
 * Discovery server.
 *
 * @param <D> the discovery server type
 * @author Ponfee
 */
public interface Discovery<D extends Server> extends AutoCloseable {

    /**
     * Gets all alive discovered servers.
     *
     * @return all alive discovered servers
     */
    default List<D> getDiscoveredServers() {
        return getDiscoveredServers(null);
    }

    /**
     * Gets grouped alive discovered servers.
     *
     * @param group the discovered interested group
     * @return list of grouped alive discovered servers
     */
    List<D> getDiscoveredServers(String group);

    /**
     * Returns a boolean for the server is whether alive.
     *
     * @param server the server
     * @return {@code true} if is alive
     */
    default boolean isDiscoveredServerAlive(D server) {
        return getDiscoveredServers().contains(server);
    }

    /**
     * Returns discovery server role.
     *
     * @return discovery server role
     */
    ServerRole discoveryRole();

    /**
     * Close discovery.
     */
    @Override
    void close();
}
