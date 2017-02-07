/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     qiong.wang (夜色)
 */

package com.wq.wqchat.core.server;

import com.wq.wqchat.api.connection.ConnectionManager;
import com.wq.wqchat.api.protocol.Command;
import com.wq.wqchat.api.service.Listener;
import com.wq.wqchat.api.spi.handler.PushHandlerFactory;
import com.wq.wqchat.common.MessageDispatcher;
import com.wq.wqchat.core.handler.AckHandler;
import com.wq.wqchat.core.handler.BindUserHandler;
import com.wq.wqchat.core.handler.HandshakeHandler;
import com.wq.wqchat.netty.server.NettyTCPServer;
import com.wq.wqchat.tools.config.CC;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
public final class WebSocketServer extends NettyTCPServer {

    private static WebSocketServer I;

    private ChannelHandler channelHandler;

    private ConnectionManager connectionManager = new ServerConnectionManager(false);

    public static WebSocketServer I() {
        if (I == null) {
            synchronized (ConnectionServer.class) {
                if (I == null) {
                    I = new WebSocketServer();
                }
            }
        }
        return I;
    }

    private WebSocketServer() {
        super(CC.mp.net.ws_server_port);
    }

    @Override
    public void init() {
        super.init();
        connectionManager.init();
        MessageDispatcher receiver = new MessageDispatcher();
        receiver.register(Command.HANDSHAKE, new HandshakeHandler());
        receiver.register(Command.BIND, new BindUserHandler());
        receiver.register(Command.UNBIND, new BindUserHandler());
        receiver.register(Command.PUSH, PushHandlerFactory.create());
        receiver.register(Command.ACK, new AckHandler());
        channelHandler = new WebSocketChannelHandler(connectionManager, receiver);
    }

    @Override
    public void stop(Listener listener) {
        super.stop(listener);
        connectionManager.destroy();
    }

    @Override
    public EventLoopGroup getBossGroup() {
        return ConnectionServer.I().getBossGroup();
    }

    @Override
    public EventLoopGroup getWorkerGroup() {
        return ConnectionServer.I().getWorkerGroup();
    }

    @Override
    protected void initPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(CC.mp.net.ws_path, null, true));
        pipeline.addLast(new WebSocketIndexPageHandler());
        pipeline.addLast(getChannelHandler());
    }

    @Override
    protected void initOptions(ServerBootstrap b) {
        super.initOptions(b);
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.childOption(ChannelOption.SO_SNDBUF, 32 * 1024);
        b.childOption(ChannelOption.SO_RCVBUF, 32 * 1024);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

}
