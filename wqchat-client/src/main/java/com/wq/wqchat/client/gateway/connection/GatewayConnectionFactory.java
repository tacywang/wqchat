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

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.service.Listener;
import com.wq.wqchat.api.srd.ServiceListener;
import com.wq.wqchat.common.message.BaseMessage;
import com.wq.wqchat.tools.config.CC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;


public abstract class GatewayConnectionFactory implements ServiceListener {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static GatewayConnectionFactory create() {
        return CC.mp.net.udpGateway() ? new GatewayUDPConnectionFactory() : new GatewayTCPConnectionFactory();
    }

    public void init(Listener listener) {
        listener.onSuccess();
    }

    abstract public void clear();

    abstract public Connection getConnection(String hostAndPort);

    abstract public <M extends BaseMessage> boolean send(String hostAndPort, Function<Connection, M> creator, Consumer<M> sender);

    abstract public <M extends BaseMessage> boolean broadcast(Function<Connection, M> creator, Consumer<M> sender);

}
