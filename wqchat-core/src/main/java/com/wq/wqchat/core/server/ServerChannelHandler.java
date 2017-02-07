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


import com.wq.wqchat.api.PacketReceiver;
import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.connection.ConnectionManager;
import com.wq.wqchat.api.event.ConnectionCloseEvent;
import com.wq.wqchat.api.protocol.Command;
import com.wq.wqchat.api.protocol.Packet;
import com.wq.wqchat.netty.connection.NettyConnection;
import com.wq.wqchat.tools.common.Profiler;
import com.wq.wqchat.tools.config.CC;
import com.wq.wqchat.tools.event.EventBus;
import com.wq.wqchat.tools.log.Logs;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public final class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannelHandler.class);

    private static final long profile_slowly_limit = CC.mp.monitor.profile_slowly_duration.toMillis();

    private final boolean security; //是否启用加密
    private final ConnectionManager connectionManager;
    private final PacketReceiver receiver;

    public ServerChannelHandler(boolean security, ConnectionManager connectionManager, PacketReceiver receiver) {
        this.security = security;
        this.connectionManager = connectionManager;
        this.receiver = receiver;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        byte cmd = packet.cmd;

        try {
            Profiler.start("time cost on [channel read]: ", packet.toString());
            Connection connection = connectionManager.get(ctx.channel());
            LOGGER.debug("channelRead conn={}, packet={}", ctx.channel(), connection.getSessionContext(), msg);
            connection.updateLastReadTime();
            receiver.onReceive(packet, connection);
        } finally {
            Profiler.release();
            if (Profiler.getDuration() > profile_slowly_limit) {
                Logs.PROFILE.info("Read Packet[cmd={}] Slowly: \n{}", Command.toCMD(cmd), Profiler.dump());
            }
            Profiler.reset();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        Logs.CONN.error("client caught ex, conn={}", connection);
        LOGGER.error("caught an ex, channel={}, conn={}", ctx.channel(), connection, cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.info("client connected conn={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        EventBus.I.post(new ConnectionCloseEvent(connection));
        Logs.CONN.info("client disconnected conn={}", connection);
    }
}