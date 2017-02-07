/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   qiong.wang (夜色)
 */

package com.wq.wqchat.core.router;

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.event.RouterChangeEvent;
import com.wq.wqchat.api.router.ClientLocation;
import com.wq.wqchat.api.router.Router;
import com.wq.wqchat.common.router.RemoteRouter;
import com.wq.wqchat.common.router.RemoteRouterManager;
import com.wq.wqchat.tools.Utils;
import com.wq.wqchat.tools.event.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.wq.wqchat.common.ServerNodes.GS;


public final class RouterCenter {
    public static final Logger LOGGER = LoggerFactory.getLogger(RouterCenter.class);
    public static final RouterCenter I = new RouterCenter();

    private final LocalRouterManager localRouterManager = new LocalRouterManager();
    private final RemoteRouterManager remoteRouterManager = new RemoteRouterManager();
    private final RouterChangeListener routerChangeListener = new RouterChangeListener();
    private final UserEventConsumer userEventConsumer = new UserEventConsumer();


    /**
     * 注册用户和链接
     *
     * @param userId
     * @param connection
     * @return
     */
    public boolean register(String userId, Connection connection) {
        ClientLocation location = ClientLocation
                .from(connection)
                .setHost(Utils.getLocalIp())
                .setPort(GS.getPort());

        LocalRouter localRouter = new LocalRouter(connection);
        RemoteRouter remoteRouter = new RemoteRouter(location);

        LocalRouter oldLocalRouter = null;
        RemoteRouter oldRemoteRouter = null;
        try {
            oldLocalRouter = localRouterManager.register(userId, localRouter);
            oldRemoteRouter = remoteRouterManager.register(userId, remoteRouter);
        } catch (Exception e) {
            LOGGER.error("register router ex, userId={}, connection={}", userId, connection, e);
        }

        if (oldLocalRouter != null) {
            EventBus.I.post(new RouterChangeEvent(userId, oldLocalRouter));
            LOGGER.info("register router success, find old local router={}, userId={}", oldLocalRouter, userId);
        }

        if (oldRemoteRouter != null && oldRemoteRouter.isOnline()) {
            EventBus.I.post(new RouterChangeEvent(userId, oldRemoteRouter));
            LOGGER.info("register router success, find old remote router={}, userId={}", oldRemoteRouter, userId);
        }
        return true;
    }

    public boolean unRegister(String userId, int clientType) {
        localRouterManager.unRegister(userId, clientType);
        remoteRouterManager.unRegister(userId, clientType);
        return true;
    }

    public Router<?> lookup(String userId, int clientType) {
        LocalRouter local = localRouterManager.lookup(userId, clientType);
        if (local != null) return local;
        RemoteRouter remote = remoteRouterManager.lookup(userId, clientType);
        return remote;
    }

    public LocalRouterManager getLocalRouterManager() {
        return localRouterManager;
    }

    public RemoteRouterManager getRemoteRouterManager() {
        return remoteRouterManager;
    }

    public RouterChangeListener getRouterChangeListener() {
        return routerChangeListener;
    }
}
