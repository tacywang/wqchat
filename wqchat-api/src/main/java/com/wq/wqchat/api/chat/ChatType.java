package com.wq.wqchat.api.chat;

public enum ChatType {

	one2one(0), one2Many(1);
	public final int type;

	private ChatType(int type) {
		this.type = type;
	}

	public static ChatType getChatType(int type) {
		for (ChatType messageType : values()) {
			if (messageType.type == type) {
				return messageType;
			}

		}
		return null;
	}
}
