package com.wq.wqchat.core.pub;

import java.util.List;

import com.wq.wqchat.api.spi.common.MQClient;
import com.wq.wqchat.api.spi.common.MQClientFactory;
import com.wq.wqchat.api.spi.pub.MessageModel;
import com.wq.wqchat.api.spi.pub.PubService;
import com.wq.wqchat.api.spi.pub.PubServiceFactory;
import com.wq.wqchat.api.spi.pub.SendStatusModel;
import com.wq.wqchat.store.MessageStore;
import com.wq.wqchat.store.factory.MessageStoreFactory;
import com.wq.wqchat.tools.log.Logs;

public class RedisPubService implements PubService, PubServiceFactory {

	private MQClient mqClient = MQClientFactory.create();

	private MessageStore messageStore = MessageStoreFactory.create();

	private ChatMQMessageReceiver mQMessageReceiver = new ChatMQMessageReceiver(this, MessageStoreFactory.create());

	@Override
	public PubService get() {
		return this;
	}

	@Override
	public void pubMessage(SendStatusModel sendStatusModel, MessageModel messageModel) {
		mqClient.publish(sendStatusModel.getTargetUserId(), messageModel);
		Logs.CACHE.info("pubmessage topic:" + sendStatusModel.getTargetUserId());
	}

	@Override
	public void subMessage(String userId) {
		mqClient.subscribe(userId, mQMessageReceiver);

		// 捞取所有该账号未投递的消息，进行发布出去
		try {
			List<MessageModel> messageModels = messageStore.findNotSendMessageByTargetId(userId);

			for (MessageModel messageModel : messageModels) {
				pubMessage(userId, messageModel);
			}
		} catch (Exception e) {
			Logs.PUSH.error("捞取所有该账号为" + userId + "未投递的消息出错", e);
		}

	}

	@Override
	public void pubMessage(String topic, MessageModel messageModel) {
		mqClient.publish(topic, messageModel);
	}

	@Override
	public void unSubMessage(String userId) {
		mqClient.unSubscribe(userId);
	}

}
