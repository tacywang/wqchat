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

package com.wq.wqchat.client.gateway.connection;

import static com.wq.wqchat.api.srd.ServiceNames.GATEWAY_SERVER;
import static com.wq.wqchat.tools.config.CC.mp.net.gateway_client_num;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.HostAndPort;
import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.event.ConnectionConnectEvent;
import com.wq.wqchat.api.service.Listener;
import com.wq.wqchat.api.spi.common.ServiceDiscoveryFactory;
import com.wq.wqchat.api.srd.ServiceDiscovery;
import com.wq.wqchat.api.srd.ServiceNode;
import com.wq.wqchat.client.gateway.GatewayClient;
import com.wq.wqchat.common.message.BaseMessage;
import com.wq.wqchat.tools.event.EventBus;


public class GatewayTCPConnectionFactory extends GatewayConnectionFactory {

    private final Map<String, List<Connection>> connections = Maps.newConcurrentMap();

    private GatewayClient client;

    @Override
    public void init(Listener listener) {
        EventBus.I.register(this);
        client = new GatewayClient();
        client.start().join();

        ServiceDiscovery discovery = ServiceDiscoveryFactory.create();
        discovery.subscribe(GATEWAY_SERVER, this);
        discovery.lookup(GATEWAY_SERVER).forEach(this::add);
        listener.onSuccess();
    }

    @Override
    public void onServiceAdded(String path, ServiceNode node) {
        add(node);
    }

    @Override
    public void onServiceUpdated(String path, ServiceNode node) {
        removeClient(node);
        add(node);
    }

    @Override
    public void onServiceRemoved(String path, ServiceNode node) {
        removeClient(node);
        logger.warn("Gateway Server zkNode={} was removed.", node);
    }

    public void clear() {
        connections.values().forEach(l -> l.forEach(Connection::close));
        if (client != null) {
            client.stop().join();
        }
        ServiceDiscoveryFactory.create().unsubscribe(GATEWAY_SERVER, this);
    }

    @Override
    public Connection getConnection(String hostAndPort) {
        List<Connection> connections = this.connections.get(hostAndPort);
        if (connections == null || connections.isEmpty()) {
            return null;//TODO create client
        }

        int L = connections.size();

        Connection connection;
        if (L == 1) {
            connection = connections.get(0);
        } else {
            connection = connections.get((int) (Math.random() * L % L));
        }

        if (connection.isConnected()) {
            return connection;
        }

        reconnect(connection, hostAndPort);
        return getConnection(hostAndPort);
    }

    @Override
    public <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> creator, Consumer<M> sender) {
        Connection connection = getConnection(hostAndPort);
        if (connection == null) return false;// gateway server 找不到，直接返回推送失败

        sender.accept(creator.apply(connection));
        return true;
    }

    @Override
    public <M extends BaseMessage> boolean broadcast(Function<Connection, M> creator, Consumer<M> sender) {
        if (connections.isEmpty()) return false;
        connections
                .values()
                .stream()
                .filter(connections -> connections.size() > 0)
                .forEach(connections -> sender.accept(creator.apply(connections.get(0))));
        return true;
    }

    private void reconnect(Connection connection, String hostAndPort) {
        HostAndPort h_p = HostAndPort.fromString(hostAndPort);
        connections.get(hostAndPort).remove(connection);
        connection.close();
        addConnection(h_p.getHostText(), h_p.getPort());
    }

    private void removeClient(ServiceNode node) {
        if (node != null) {
            List<Connection> clients = connections.remove(getHostAndPort(node.getHost(), node.getPort()));
            if (clients != null) {
                clients.forEach(Connection::close);
            }
        }
    }

    private void add(ServiceNode node) {
        for (int i = 0; i < gateway_client_num; i++) {
            addConnection(node.getHost(), node.getPort());
        }
    }

    private void addConnection(String host, int port) {
        client.connect(host, port).addListener(f -> {
            if (!f.isSuccess()) {
                logger.error("create gateway connection ex, host={}, port", host, port, f.cause());
            }
        });
    }

    @Subscribe
    void on(ConnectionConnectEvent event) {
        Connection connection = event.connection;
        InetSocketAddress address = (InetSocketAddress) connection.getChannel().remoteAddress();
        String hostAndPort = getHostAndPort(address.getAddress().getHostAddress(), address.getPort());
        connections.computeIfAbsent(hostAndPort, key -> new ArrayList<>(gateway_client_num)).add(connection);
    }

    private static String getHostAndPort(String host, int port) {
        return host + ":" + port;
    }
}
