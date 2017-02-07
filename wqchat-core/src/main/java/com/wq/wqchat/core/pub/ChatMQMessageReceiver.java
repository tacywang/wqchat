package com.wq.wqchat.core.pub;

import java.util.Map;

import com.wq.wqchat.api.Constants;
import com.wq.wqchat.api.chat.SendMessageStatus;
import com.wq.wqchat.api.connection.Connection;
import com.wq.wqchat.api.protocol.Command;
import com.wq.wqchat.api.protocol.Packet;
import com.wq.wqchat.api.spi.common.MQMessageReceiver;
import com.wq.wqchat.api.spi.pub.MessageModel;
import com.wq.wqchat.api.spi.pub.PubService;
import com.wq.wqchat.common.message.PushMessage;
import com.wq.wqchat.core.router.LocalRouter;
import com.wq.wqchat.core.router.LocalRouterManager;
import com.wq.wqchat.core.router.RouterCenter;
import com.wq.wqchat.store.MessageStore;
import com.wq.wqchat.tools.Jsons;
import com.wq.wqchat.tools.log.Logs;

public class ChatMQMessageReceiver implements MQMessageReceiver {

	private LocalRouterManager localRouterManager = RouterCenter.I.getLocalRouterManager();

	private PubService pubService;

	private MessageStore messageStore;

	public ChatMQMessageReceiver(PubService pubService, MessageStore messageStore) {
		this.pubService = pubService;
		this.messageStore = messageStore;
	}

	@Override
	public void receive(String topic, Object message) {
		Map<Integer, LocalRouter> clientMap = localRouterManager.routers().get(topic);
		MessageModel msg = Jsons.fromJson(message.toString(), MessageModel.class);
		if (clientMap != null && clientMap.size() > 0) {

			if (msg != null) {

				for (Integer clientType : clientMap.keySet()) {

					LocalRouter localRouter = clientMap.get(clientType);
					Connection connection = localRouter.getRouteValue();

					PushMessage pushMessage = PushMessage.build(connection);
//					Packet packet = new Packet(Command.PUSH);
					pushMessage.content = (message.toString().getBytes(Constants.UTF_8));
//					packet.setBody(message.toString().getBytes(Constants.UTF_8));
//					connection.send(packet);
					pushMessage.send();
				}
				messageStore.updateMessageStatusByTargetId(msg, topic, SendMessageStatus.sendSuccess);
				Logs.PUSH.info("chat message send success .targetId=" + topic);

			} else {
				Logs.CONN.warn("receive an error kick message={}", message);

				pubService.pubMessage(topic, msg);
			}
		} else {
			Logs.CONN.warn("receive an error redis channel={}", topic);
			pubService.pubMessage(topic, msg);
		}
	}

}
