package com.wq.wqchat.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.wq.wqchat.api.Constants;
import com.wq.wqchat.api.chat.ChatMessage;
import com.wq.wqchat.api.chat.ChatType;
import com.wq.wqchat.api.chat.MessageType;
import com.wq.wqchat.api.chat.SendMessageStatus;
import com.wq.wqchat.api.protocol.JsonPacket;
import com.wq.wqchat.api.spi.external.ExternalService;
import com.wq.wqchat.api.spi.pub.MessageModel;
import com.wq.wqchat.api.spi.pub.PubService;
import com.wq.wqchat.api.spi.pub.SendStatusModel;
import com.wq.wqchat.common.exception.ChatException;
import com.wq.wqchat.common.message.PushMessage;
import com.wq.wqchat.common.router.RemoteRouter;
import com.wq.wqchat.common.router.RemoteRouterManager;
import com.wq.wqchat.core.router.RouterCenter;
import com.wq.wqchat.store.MessageStore;
import com.wq.wqchat.tools.Jsons;
import com.wq.wqchat.tools.log.Logs;

/**
 * 同步进行消息处理
 * 
 * @author qiong.wang
 *
 */
public class SyncMessageProcess implements MessageProcess {

	private MessageStore messageStore;

	private ExternalService externalService;

	private PubService pubService;

	private RemoteRouterManager remoteRouterManager = RouterCenter.I.getRemoteRouterManager();

	public SyncMessageProcess(MessageStore messageStore, ExternalService externalService, PubService pubService) {
		this.messageStore = messageStore;
		this.externalService = externalService;
		this.pubService = pubService;
	}

	@Override
	public void process(PushMessage message) {
		String content = null;
		if (JsonPacket.class.isInstance(message.getPacket())) {
			JsonPacket jsonPacket = (JsonPacket) message.getPacket();
			content = Jsons.toJson(jsonPacket.getBody());
		} else {
			content = new String(message.content, Constants.UTF_8);
		}
		if (StringUtils.isNotBlank(content)) {
			ChatMessage chatMessage = Jsons.fromJson(content, ChatMessage.class);
			checkChatMessage(chatMessage);
			String userId = message.getConnection().getSessionContext().userId;
			MessageModel messageModel = new MessageModel();
			messageModel.setClientTime(chatMessage.getClientTime());
			messageModel.setChatType(chatMessage.getChatType());
			messageModel.setContent(chatMessage.getMessage());
			messageModel.setId(chatMessage.getId());
			messageModel.setMessageType(chatMessage.getMessageType());
			messageModel.setTargetId(chatMessage.getTargetId());
			messageModel.setSendUserId(userId);
			messageModel.setName(chatMessage.getSendUserName());
			List<SendStatusModel> list = new ArrayList<SendStatusModel>();

			if (chatMessage.getChatType() == ChatType.one2one.type) {

				if (!externalService.isFriends(userId, chatMessage.getTargetId())) {
					throw new ChatException("目标用户不存在:" + chatMessage.getTargetId());
				}

				SendStatusModel statusModel = new SendStatusModel();
				statusModel.setSendId(chatMessage.getId());
				statusModel.setStatus(SendMessageStatus.notSend.status);
				statusModel.setTargetUserId(chatMessage.getTargetId());

				list.add(statusModel);
			} else if (chatMessage.getChatType() == ChatType.one2Many.type) {
				List<String> userIds = externalService.findUserListByChatRoomId(chatMessage.getTargetId());
				if (userIds == null || userIds.size() == 0) {
					throw new ChatException("目标聊天室不存在:" + chatMessage.getTargetId());
				}

				for (String targetUserId : userIds) {
					SendStatusModel statusModel = new SendStatusModel();
					statusModel.setSendId(chatMessage.getId());
					statusModel.setStatus(SendMessageStatus.notSend.status);
					statusModel.setTargetUserId(targetUserId);

					list.add(statusModel);
				}
			}

			messageStore.saveMessage(messageModel, list);

			pubMessage(messageModel, list);

		}
	}

	/**
	 * 发布订阅消息
	 * 
	 * @param messageModel
	 */
	private void pubMessage(MessageModel messageModel, List<SendStatusModel> sendStatusModels) {

		for (SendStatusModel sendStatusModel : sendStatusModels) {
			Set<RemoteRouter> remoteRouters = remoteRouterManager.lookupAll(sendStatusModel.getTargetUserId());
			if (remoteRouters != null && remoteRouters.size() > 0) {
				boolean isPub = false;
				for (RemoteRouter remoteRouter : remoteRouters) {
					if (remoteRouter.isOnline()) {
						isPub = true;
						break;
					}
				}
				if (isPub) {
					pubService.pubMessage(sendStatusModel, messageModel);
				} else {
					Logs.PROCESS.info("not pubmesssage userId=" + sendStatusModel.getTargetUserId()
							+ " is offline, messageId=" + messageModel.getId());
				}
				isPub = false;
			}

		}

	}

	/**
	 * 检查聊天消息是否正确
	 * 
	 * @param chatMessage
	 */
	private void checkChatMessage(ChatMessage chatMessage) {
		if (chatMessage == null) {
			Logs.PROCESS.error("聊天消息为空");
			throw new ChatException("聊天消息为空");
		}

		if (ChatType.getChatType(chatMessage.getChatType()) == null) {
			Logs.PROCESS.error("聊天消息的chattype不正确:" + chatMessage.getChatType());
			throw new ChatException("聊天消息的chattype不正确:" + chatMessage.getChatType());
		}

		if (MessageType.getMessageType(chatMessage.getMessageType()) == null) {
			Logs.PROCESS.error("聊天消息的messagetype不正确:" + chatMessage.getMessageType());
			throw new ChatException("聊天消息的messagetype不正确:" + chatMessage.getMessageType());
		}
	}

}
