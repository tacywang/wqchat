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

import com.wq.wqchat.api.MessageHandler;
import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Packet;
import com.wq.wqchat.api.spi.Spi;
import com.wq.wqchat.api.spi.handler.FileUploadHandlerFactory;
import com.wq.wqchat.common.handler.BaseMessageHandler;
import com.wq.wqchat.common.message.AckMessage;
import com.wq.wqchat.common.message.ErrorMessage;
import com.wq.wqchat.common.message.FileUploadMessage;
import com.wq.wqchat.core.process.MessageProcess;
import com.wq.wqchat.core.process.MessageProcessFactory;
import com.wq.wqchat.tools.log.Logs;


@Spi(order = 1)
public final class MongoFileUploadHandler extends BaseMessageHandler<FileUploadMessage> implements FileUploadHandlerFactory {

	@Override
	public FileUploadMessage decode(Packet packet, Connection connection) {
		return new FileUploadMessage(packet, connection);
	}

	@Override
	public void handle(FileUploadMessage message) {
		Logs.PUSH.info("receive client push message={}", message);

		MessageProcess messageProcess = MessageProcessFactory.getMessageProcess();

		try {
//			messageProcess.process(message);
		} catch (Exception e) {
			// 发送失败
			Logs.PUSH.error("消息处理过程出错", e);
			ErrorMessage.from(message).setReason("消息处理过程出错").sendRaw();
			return;
		}
		// 发送成功
		if (message.autoAck()) {
			AckMessage.from(message).sendRaw();
			Logs.PUSH.info("send ack for push message={}", message);
		}

	}

	@Override
	public MessageHandler get() {
		return this;
	}
}
