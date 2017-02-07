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

package com.wq.wqchat.client.connect;

import com.google.common.eventbus.Subscribe;
import com.wq.wqchat.api.event.ConnectionCloseEvent;
import com.wq.wqchat.netty.client.NettyTCPClient;
import com.wq.wqchat.tools.event.EventBus;

import io.netty.channel.ChannelHandler;

public class ConnectClient extends NettyTCPClient {
    private final ConnClientChannelHandler handler;

    public ConnectClient(String host, int port, ClientConfig config) {
        handler = new ConnClientChannelHandler(config);
        EventBus.I.register(this);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return handler;
    }

    @Subscribe
    void on(ConnectionCloseEvent event) {
        this.stop();
    }

    @Override
    protected int getWorkThreadNum() {
        return 1;
    }
}
