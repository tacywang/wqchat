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

package com.wq.wqchat.test.client;

import com.wq.wqchat.api.service.BaseService;
import com.wq.wqchat.api.service.Listener;
import com.wq.wqchat.api.spi.common.CacheManagerFactory;
import com.wq.wqchat.api.spi.common.ServiceDiscoveryFactory;
import com.wq.wqchat.api.srd.ServiceNames;
import com.wq.wqchat.api.srd.ServiceNode;
import com.wq.wqchat.cache.redis.manager.RedisManager;
import com.wq.wqchat.client.connect.ClientConfig;
import com.wq.wqchat.client.connect.ConnClientChannelHandler;
import com.wq.wqchat.netty.codec.PacketDecoder;
import com.wq.wqchat.netty.codec.PacketEncoder;
import com.wq.wqchat.tools.Jsons;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

import static com.wq.wqchat.client.connect.ConnClientChannelHandler.CONFIG_KEY;

public final class ConnClientBoot extends BaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnClientBoot.class);

    private Bootstrap bootstrap;
    private NioEventLoopGroup workerGroup;

    @Override
    protected void doStart(Listener listener) throws Throwable {
        ServiceDiscoveryFactory.create().syncStart();
        CacheManagerFactory.create().init();

        this.workerGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)//
                .option(ChannelOption.TCP_NODELAY, true)//
                .option(ChannelOption.SO_REUSEADDR, true)//
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60 * 1000)
                .option(ChannelOption.SO_RCVBUF, 5 * 1024 * 1024)
                .channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() { // (4)
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("decoder", new PacketDecoder());
                ch.pipeline().addLast("encoder", PacketEncoder.INSTANCE);
                ch.pipeline().addLast("handler", new ConnClientChannelHandler());
            }
        });

        listener.onSuccess();
    }

    @Override
    protected void doStop(Listener listener) throws Throwable {
        if (workerGroup != null) workerGroup.shutdownGracefully();
        ServiceDiscoveryFactory.create().syncStop();
        RedisManager.I.destroy();
        listener.onSuccess();
    }

    public List<ServiceNode> getServers() {
        return ServiceDiscoveryFactory.create().lookup(ServiceNames.CONN_SERVER);
    }

    public ChannelFuture connect(InetSocketAddress remote, InetSocketAddress local, ClientConfig clientConfig) {
        ChannelFuture future = local != null ? bootstrap.connect(remote, local) : bootstrap.connect(remote);
        if (future.channel() != null) future.channel().attr(CONFIG_KEY).set(clientConfig);
        future.addListener(f -> {
            if (f.isSuccess()) {
                future.channel().attr(CONFIG_KEY).set(clientConfig);
                LOGGER.info("start netty client success, remote={}, local={}", remote, local);
            } else {
                LOGGER.error("start netty client failure, remote={}, local={}", remote, local, f.cause());
            }
        });
        return future;
    }

    public ChannelFuture connect(String host, int port, ClientConfig clientConfig) {
        return connect(new InetSocketAddress(host, port), null, clientConfig);
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public NioEventLoopGroup getWorkerGroup() {
        return workerGroup;
    }
}