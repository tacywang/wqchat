package com.wq.wqchat.store;

import java.util.List;

import com.wq.wqchat.api.chat.SendMessageStatus;
import com.wq.wqchat.api.spi.pub.MessageModel;
import com.wq.wqchat.api.spi.pub.SendStatusModel;

public interface MessageStore {

	public void saveMessage(MessageModel messageModel, List<SendStatusModel> sendStatusModels);

	public void delete(MessageModel messageModel);

	public void deleteById(String id);

	public MessageModel findMessageById(String id);

	public void updateMessageStatusByTargetId(MessageModel msg, String topic, SendMessageStatus sendsuccess);

	public List<MessageModel> findNotSendMessageByTargetId(String targetId);
}
