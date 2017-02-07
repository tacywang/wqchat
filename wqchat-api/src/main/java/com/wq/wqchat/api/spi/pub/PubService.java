package com.wq.wqchat.api.spi.pub;

public interface PubService {

	public void pubMessage(SendStatusModel sendStatusModel, MessageModel messageModel);
	
	public void subMessage(String userId);

	public void pubMessage(String topic, MessageModel msg);
	
	public void unSubMessage(String userId);
	
}
