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
import com.wq.wqchat.common.message.ErrorMessage;
import com.wq.wqchat.tools.log.Logs;

import static com.wq.wqchat.common.ErrorCode.OFFLINE;
import static com.wq.wqchat.common.ErrorCode.PUSH_CLIENT_FAILURE;
import static com.wq.wqchat.common.ErrorCode.ROUTER_CHANGE;


public final class GatewayErrorHandler extends BaseMessageHandler<ErrorMessage> {

    @Override
    public ErrorMessage decode(Packet packet, Connection connection) {
        return new ErrorMessage(packet, connection);
    }

    @Override
    public void handle(ErrorMessage message) {
        if (message.cmd == Command.GATEWAY_PUSH.cmd) {
            PushRequest request = PushRequestBus.I.getAndRemove(message.getSessionId());
            if (request == null) {
                Logs.PUSH.warn("receive a gateway response, but request has timeout. message={}", message);
                return;
            }

            Logs.PUSH.warn("receive an error gateway response, message={}", message);
            if (message.code == OFFLINE.errorCode) {//用户离线
                request.onOffline();
            } else if (message.code == PUSH_CLIENT_FAILURE.errorCode) {//下发到客户端失败
                request.onFailure();
            } else if (message.code == ROUTER_CHANGE.errorCode) {//用户路由信息更改
                request.onRedirect();
            }
        }
    }
}
