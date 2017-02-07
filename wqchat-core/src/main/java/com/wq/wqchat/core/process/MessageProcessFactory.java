package com.wq.wqchat.core.process;

import com.wq.wqchat.api.spi.external.ExternalServiceFactory;
import com.wq.wqchat.api.spi.pub.PubServiceFactory;
import com.wq.wqchat.store.factory.MessageStoreFactory;
import com.wq.wqchat.tools.config.CC;

public class MessageProcessFactory {

	private static MessageProcess I;

	public static MessageProcess getMessageProcess() {
		if (I != null) {
			return I;
		}
		if (CC.mp.message_process_asyn) {
			I = new AsynMessageProcess(MessageStoreFactory.create(),ExternalServiceFactory.create(),PubServiceFactory.create());
		} else {
			I = new SyncMessageProcess(MessageStoreFactory.create(),ExternalServiceFactory.create(),PubServiceFactory.create());
		}
		return I;

	}

}
