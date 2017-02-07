package com.wq.wqchat.api.chat;

public enum MessageType {

	text(0), picture(1), expression(2), language(3);
	public int type;
	private MessageType(int type) {
		this.type = type;
	}
	
	public static MessageType getMessageType(int type){
		for(MessageType messageType:values()){
			if(messageType.type==type){
				return messageType;
			}
			
		}
		return null;
	}
}
