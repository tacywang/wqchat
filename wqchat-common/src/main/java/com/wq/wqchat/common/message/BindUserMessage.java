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

package com.wq.wqchat.common.message;

import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Command;
import com.wq.wqchat.api.protocol.Packet;
import io.netty.buffer.ByteBuf;

import java.util.Map;


public final class BindUserMessage extends ByteBufMessage {
    public String userId;
    public String alias;
    public String tags;

    public BindUserMessage(Connection connection) {
        super(new Packet(Command.BIND, genSessionId()), connection);
    }

    public BindUserMessage(Packet message, Connection connection) {
        super(message, connection);
    }

    @Override
    public void decode(ByteBuf body) {
        userId = decodeString(body);
        alias = decodeString(body);
        tags = decodeString(body);
    }

    @Override
    public void encode(ByteBuf body) {
        encodeString(body, userId);
        encodeString(body, alias);
        encodeString(body, tags);
    }

    @Override
    public void decodeJsonBody(Map<String, Object> body) {
        userId = (String) body.get("userId");
        tags = (String) body.get("tags");
    }

    @Override
    public String toString() {
        return "BindUserMessage{" +
                "alias='" + alias + '\'' +
                ", userId='" + userId + '\'' +
                ", tags='" + tags + '\'' +
                ", packet=" + packet +
                '}';
    }
}
