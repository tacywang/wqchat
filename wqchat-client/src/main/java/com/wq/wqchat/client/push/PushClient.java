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

package com.wq.wqchat.client.push;

import java.util.Set;
import java.util.concurrent.FutureTask;

import com.wq.wqchat.api.push.PushContext;
import com.wq.wqchat.api.push.PushException;
import com.wq.wqchat.api.push.PushResult;
import com.wq.wqchat.api.push.PushSender;
import com.wq.wqchat.api.service.BaseService;
import com.wq.wqchat.api.service.Listener;
import com.wq.wqchat.api.spi.common.CacheManagerFactory;
import com.wq.wqchat.api.spi.common.ServiceDiscoveryFactory;
import com.wq.wqchat.client.gateway.connection.GatewayConnectionFactory;
import com.wq.wqchat.common.router.CachedRemoteRouterManager;
import com.wq.wqchat.common.router.RemoteRouter;

/*package*/ final class PushClient extends BaseService implements PushSender {
    private final GatewayConnectionFactory factory = GatewayConnectionFactory.create();

    private FutureTask<PushResult> send0(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return PushRequest.build(factory, ctx).broadcast();
        } else {
            Set<RemoteRouter> remoteRouters = CachedRemoteRouterManager.I.lookupAll(ctx.getUserId());
            if (remoteRouters == null || remoteRouters.isEmpty()) {
                return PushRequest.build(factory, ctx).onOffline();
            }
            FutureTask<PushResult> task = null;
            for (RemoteRouter remoteRouter : remoteRouters) {
                task = PushRequest.build(factory, ctx).send(remoteRouter);
            }
            return task;
        }
    }

    @Override
    public FutureTask<PushResult> send(PushContext ctx) {
        if (ctx.isBroadcast()) {
            return send0(ctx.setUserId(null));
        } else if (ctx.getUserId() != null) {
            return send0(ctx);
        } else if (ctx.getUserIds() != null) {
            FutureTask<PushResult> task = null;
            for (String userId : ctx.getUserIds()) {
                task = send0(ctx.setUserId(userId));
            }
            return task;
        } else {
            throw new PushException("param error.");
        }
    }

    @Override
    protected void doStart(Listener listener) throws Throwable {
        ServiceDiscoveryFactory.create().syncStart();
        CacheManagerFactory.create().init();
        PushRequestBus.I.syncStart();
        factory.init(listener);
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        ServiceDiscoveryFactory.create().syncStart();
        CacheManagerFactory.create().destroy();
        PushRequestBus.I.stop(listener);
        factory.clear();
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }
}
