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

package com.wq.wqchat.core.handler;

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Packet;
import com.wq.wqchat.common.handler.BaseMessageHandler;
import com.wq.wqchat.common.message.ErrorMessage;
import com.wq.wqchat.common.message.FastConnectMessage;
import com.wq.wqchat.common.message.FastConnectOkMessage;
import com.wq.wqchat.core.session.ReusableSession;
import com.wq.wqchat.core.session.ReusableSessionManager;
import com.wq.wqchat.tools.common.Profiler;
import com.wq.wqchat.tools.config.ConfigManager;
import com.wq.wqchat.tools.log.Logs;


public final class FastConnectHandler extends BaseMessageHandler<FastConnectMessage> {

    @Override
    public FastConnectMessage decode(Packet packet, Connection connection) {
        return new FastConnectMessage(packet, connection);
    }

    @Override
    public void handle(FastConnectMessage message) {
        //从缓存中心查询session
        Profiler.enter("time cost on [query session]");
        ReusableSession session = ReusableSessionManager.I.querySession(message.sessionId);
        Profiler.release();
        if (session == null) {
            //1.没查到说明session已经失效了
            ErrorMessage.from(message).setReason("session expired").send();
            Logs.CONN.warn("fast connect failure, session is expired, sessionId={}, deviceId={}, conn={}"
                    , message.sessionId, message.deviceId, message.getConnection().getChannel());
        } else if (!session.context.deviceId.equals(message.deviceId)) {
            //2.非法的设备, 当前设备不是上次生成session时的设备
            ErrorMessage.from(message).setReason("invalid device").send();
            Logs.CONN.warn("fast connect failure, not the same device, deviceId={}, session={}, conn={}"
                    , message.deviceId, session.context, message.getConnection().getChannel());
        } else {
            //3.校验成功，重新计算心跳，完成快速重连
            int heartbeat = ConfigManager.I.getHeartbeat(message.minHeartbeat, message.maxHeartbeat);

            session.context.setHeartbeat(heartbeat);
            message.getConnection().setSessionContext(session.context);
            Profiler.enter("time cost on [send FastConnectOkMessage]");
            FastConnectOkMessage
                    .from(message)
                    .setHeartbeat(heartbeat)
                    .sendRaw();
            Profiler.release();
            Logs.CONN.info("fast connect success, session={}", session.context);
        }
    }
}
