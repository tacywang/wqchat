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

package com.wq.wqchat.client.gateway;

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Command;
import com.wq.wqchat.api.service.Listener;
import com.wq.wqchat.client.gateway.handler.GatewayErrorHandler;
import com.wq.wqchat.client.gateway.handler.GatewayOKHandler;
import com.wq.wqchat.common.MessageDispatcher;
import com.wq.wqchat.netty.udp.UDPChannelHandler;
import com.wq.wqchat.netty.udp.NettyUDPConnector;
import com.wq.wqchat.tools.Utils;
import com.wq.wqchat.tools.config.CC;
import com.wq.wqchat.tools.config.CC.mp.net.rcv_buf;
import com.wq.wqchat.tools.config.CC.mp.net.snd_buf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

import static com.wq.wqchat.common.MessageDispatcher.POLICY_LOG;


public final class GatewayUDPConnector extends NettyUDPConnector {

    private static GatewayUDPConnector I;

    public static GatewayUDPConnector I() {
        if (I == null) {
            synchronized (GatewayUDPConnector.class) {
                if (I == null) {
                    I = new GatewayUDPConnector();
                }
            }
        }
        return I;
    }

    private UDPChannelHandler channelHandler;

    private GatewayUDPConnector() {
        super(CC.mp.net.gateway_client_port);
    }

    @Override
    public void init() {
        super.init();
        MessageDispatcher receiver = new MessageDispatcher(POLICY_LOG);
        receiver.register(Command.OK, new GatewayOKHandler());
        receiver.register(Command.ERROR, new GatewayErrorHandler());
        channelHandler = new UDPChannelHandler(receiver);
        channelHandler.setMulticastAddress(Utils.getInetAddress(CC.mp.net.gateway_client_multicast));
        channelHandler.setNetworkInterface(Utils.getLocalNetworkInterface());
    }


    @Override
    public void stop(Listener listener) {
        super.stop(listener);
    }


    @Override
    protected void initOptions(Bootstrap b) {
        super.initOptions(b);
        b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);
        b.option(ChannelOption.IP_MULTICAST_TTL, 255);
        if (snd_buf.gateway_client > 0) b.option(ChannelOption.SO_SNDBUF, snd_buf.gateway_client);
        if (rcv_buf.gateway_client > 0) b.option(ChannelOption.SO_RCVBUF, rcv_buf.gateway_client);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public Connection getConnection() {
        return channelHandler.getConnection();
    }
}
