package com.wq.wqchat.api.spi.pub;

import java.io.Serializable;

public class SendStatusModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8147200276224176455L;

	private String sendId;
	
	private String targetUserId;
	
	private int status;
	
	public String getSendId() {
		return sendId;
	}

	public void setSendId(String sendId) {
		this.sendId = sendId;
	}

	public String getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(String targetUserId) {
		this.targetUserId = targetUserId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	
}
