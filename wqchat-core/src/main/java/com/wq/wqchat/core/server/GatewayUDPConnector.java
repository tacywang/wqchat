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

package com.wq.wqchat.core.server;

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Command;
import com.wq.wqchat.common.MessageDispatcher;
import com.wq.wqchat.core.handler.GatewayKickUserHandler;
import com.wq.wqchat.core.handler.GatewayPushHandler;
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

    private UDPChannelHandler channelHandler;

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

    private GatewayUDPConnector() {
        super(CC.mp.net.gateway_server_port);
    }

    @Override
    public void init() {
        super.init();
        MessageDispatcher receiver = new MessageDispatcher(POLICY_LOG);
        receiver.register(Command.GATEWAY_PUSH, new GatewayPushHandler());
        receiver.register(Command.GATEWAY_KICK, new GatewayKickUserHandler());
        channelHandler = new UDPChannelHandler(receiver);
        channelHandler.setMulticastAddress(Utils.getInetAddress(CC.mp.net.gateway_server_multicast));
        channelHandler.setNetworkInterface(Utils.getLocalNetworkInterface());
    }

    @Override
    protected void initOptions(Bootstrap b) {
        super.initOptions(b);
        b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);//默认情况下，当本机发送组播数据到某个网络接口时，在IP层，数据会回送到本地的回环接口，选项IP_MULTICAST_LOOP用于控制数据是否回送到本地的回环接口
        b.option(ChannelOption.IP_MULTICAST_TTL, 255);//选项IP_MULTICAST_TTL允许设置超时TTL，范围为0～255之间的任何值
        //b.option(ChannelOption.IP_MULTICAST_IF, null);//选项IP_MULTICAST_IF用于设置组播的默认网络接口，会从给定的网络接口发送，另一个网络接口会忽略此数据,参数addr是希望多播输出接口的IP地址，使用INADDR_ANY地址回送到默认接口。
        //b.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 1024 * 1024));
        if (snd_buf.gateway_server > 0) b.option(ChannelOption.SO_SNDBUF, snd_buf.gateway_server);
        if (rcv_buf.gateway_server > 0) b.option(ChannelOption.SO_RCVBUF, rcv_buf.gateway_server);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public Connection getConnection() {
        return channelHandler.getConnection();
    }
}
