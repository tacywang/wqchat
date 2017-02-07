package com.wq.wqchat.core.process;

import com.wq.wqchat.api.spi.external.ExternalService;
import com.wq.wqchat.api.spi.pub.PubService;
import com.wq.wqchat.common.message.PushMessage;
import com.wq.wqchat.store.MessageStore;


public class AsynMessageProcess implements MessageProcess {

	private MessageStore messageStore;

	private ExternalService externalService;

	private PubService pubService;

	public AsynMessageProcess(MessageStore messageStore, ExternalService externalService, PubService pubService) {
		this.messageStore = messageStore;
		this.externalService = externalService;
		this.pubService = pubService;
	}

	@Override
	public void process(PushMessage message) {
		// MessageModel messageModel = new MessageModel();

		// messageStore.save(messageModel);
	}

}
