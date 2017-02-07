package com.wq.wqchat.common.exception;

public class ChatException extends RuntimeException {
	public ChatException(String message) {
		super(message);
	}

	public ChatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChatException(Throwable cause) {
		super(cause);
	}
}
