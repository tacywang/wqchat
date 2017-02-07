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

package com.wq.wqchat.core.handler;

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Packet;
import com.wq.wqchat.common.handler.BaseMessageHandler;
import com.wq.wqchat.common.message.gateway.GatewayKickUserMessage;
import com.wq.wqchat.core.router.RouterCenter;


public final class GatewayKickUserHandler extends BaseMessageHandler<GatewayKickUserMessage> {
    @Override
    public GatewayKickUserMessage decode(Packet packet, Connection connection) {
        return new GatewayKickUserMessage(packet, connection);
    }

    @Override
    public void handle(GatewayKickUserMessage message) {
        RouterCenter.I.getRouterChangeListener().onReceiveKickRemoteMsg(message);
    }
}
