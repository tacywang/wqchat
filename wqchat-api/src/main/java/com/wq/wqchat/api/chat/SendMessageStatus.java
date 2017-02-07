package com.wq.wqchat.api.chat;

public enum SendMessageStatus {
	notSend(0), sendSuccess(1), sendError(1);
	public final int status;

	private SendMessageStatus(int status) {
		this.status = status;
	}

	public SendMessageStatus getSendMessageStatus(int status) {
		for (SendMessageStatus messageStatus : values()) {
			if (messageStatus.status == status) {
				return messageStatus;
			}

		}
		return null;
	}
}
