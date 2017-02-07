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

package com.wq.wqchat.bootstrap.job;

import com.wq.wqchat.api.spi.net.DnsMappingManager;
import com.wq.wqchat.netty.http.NettyHttpClient;
import com.wq.wqchat.tools.config.CC;


public final class HttpProxyBoot extends BootJob {

    @Override
    protected void start() {
        if (CC.mp.http.proxy_enabled) {
            NettyHttpClient.I().syncStart();
            DnsMappingManager.create().start();
        }

        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        if (CC.mp.http.proxy_enabled) {
            NettyHttpClient.I().syncStop();
            DnsMappingManager.create().stop();
        }
    }
}
