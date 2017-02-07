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

import static com.wq.wqchat.api.protocol.Command.PUSH;

import java.util.Collections;
import java.util.Map;

import com.wq.wqchat.api.Constants;
import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.JsonPacket;
import com.wq.wqchat.api.protocol.Packet;

import io.netty.channel.ChannelFutureListener;


public final class PushMessage extends BaseMessage {

	public byte[] content;

	public PushMessage(Packet packet, Connection connection) {
		super(packet, connection);
	}

	public static PushMessage build(Connection connection) {
		if (connection.getSessionContext().isSecurity()) {
			return new PushMessage(new Packet(PUSH, genSessionId()), connection);
		} else {
			return new PushMessage(new JsonPacket(PUSH, genSessionId()), connection);
		}
	}

	@Override
	public void decode(byte[] body) {
		content = body;
	}

	@Override
	public byte[] encode() {
		return content;
	}

	@Override
	public void decodeJsonBody(Map<String, Object> body) {
		String content = (String) body.get("content");
		if (content != null) {
			this.content = content.getBytes(Constants.UTF_8);
		}
	}

	@Override
	public Map<String, Object> encodeJsonBody() {
		if (content != null) {
			return Collections.singletonMap("content", new String(content, Constants.UTF_8));
		}
		return null;
	}

	public boolean autoAck() {
		return packet.hasFlag(Packet.FLAG_AUTO_ACK);
	}

	public boolean needAck() {
		return packet.hasFlag(Packet.FLAG_BIZ_ACK) || packet.hasFlag(Packet.FLAG_AUTO_ACK);
	}

	public PushMessage setContent(byte[] content) {
		this.content = content;
		return this;
	}

	@Override
	public void send(ChannelFutureListener listener) {
		super.send(listener);
		this.content = null;// 释放内存
	}

	@Override
	public String toString() {
		return "PushMessage{" + "content='" + (content == null ? 0 : content.length) + '\'' + ", packet=" + packet
				+ '}';
	}
}
