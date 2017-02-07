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

package com.wq.wqchat.client.gateway.handler;

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Command;
import com.wq.wqchat.api.protocol.Packet;
import com.wq.wqchat.client.push.PushRequest;
import com.wq.wqchat.client.push.PushRequestBus;
import com.wq.wqchat.common.handler.BaseMessageHandler;
import com.wq.wqchat.common.message.OkMessage;
import com.wq.wqchat.common.push.GatewayPushResult;
import com.wq.wqchat.tools.log.Logs;


public final class GatewayOKHandler extends BaseMessageHandler<OkMessage> {

    @Override
    public OkMessage decode(Packet packet, Connection connection) {
        return new OkMessage(packet, connection);
    }

    @Override
    public void handle(OkMessage message) {
        if (message.cmd == Command.GATEWAY_PUSH.cmd) {
            PushRequest request = PushRequestBus.I.getAndRemove(message.getSessionId());
            if (request == null) {
                Logs.PUSH.warn("receive a gateway response, but request has timeout. message={}", message);
                return;
            }
            request.onSuccess(GatewayPushResult.fromJson(message.data));//推送成功
        }
    }
}
