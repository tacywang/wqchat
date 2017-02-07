package com.wq.wqchat.store.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.alibaba.fastjson.JSON;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.wq.wqchat.api.chat.SendMessageStatus;
import com.wq.wqchat.api.spi.Spi;
import com.wq.wqchat.api.spi.pub.MessageModel;
import com.wq.wqchat.api.spi.pub.SendStatusModel;
import com.wq.wqchat.store.MessageStore;
import com.wq.wqchat.store.factory.MessageStoreFactory;
import com.wq.wqchat.tools.config.CC;
import com.wq.wqchat.tools.log.Logs;

@Spi(order = 1)
public class MessageStoreMongodb implements MessageStore, MessageStoreFactory {

	private static MongoClient mongoClient;

	private static final String MESSAGE_TABLE_NAME = "message";

	private static final String MESSAGE_STATUS_TABLE_NAME = "message_status";

	private static String databaseName;

	@Override
	public MessageStore get() {
		return this;
	}

	// 初始化客户端
	public static MongoClient getClient() {
		try {
			if (null != mongoClient) {
				return mongoClient;
			}

			String domains = CC.mp.mongo.domains;
			String user = CC.mp.mongo.userName;
			String password = CC.mp.mongo.password;
			String database = CC.mp.mongo.dbName;
			databaseName = database;
			// 组装mongo服务端地址
			final List<ServerAddress> addressLists = new ArrayList<>();
			for (String domain : domains.split(";")) {
				String[] hostAndPort = domain.split(":");
				String host = hostAndPort[0];
				String port = hostAndPort[1];
				ServerAddress serverAddress = new ServerAddress(host, Integer.parseInt(port));
				addressLists.add(serverAddress);
			}

			// 组装mongo各项参数 默认值
			// maxConnectionsPerHost = 100;
			// threadsAllowedToBlockForConnectionMultiplier = 5;
			// serverSelectionTimeout = 1000 * 30;
			// maxWaitTime = 1000 * 60 * 2;
			// maxConnectionIdleTime;
			// maxConnectionLifeTime;
			// connectTimeout = 1000 * 10;
			// socketTimeout = 0;
			// socketKeepAlive = false;
			// sslEnabled = false;
			// sslInvalidHostNameAllowed = false;
			// alwaysUseMBeans = false;
			//
			// heartbeatFrequency = 10000;
			// minHeartbeatFrequency = 500;
			// heartbeatConnectTimeout = 20000;
			// heartbeatSocketTimeout = 20000;
			// localThreshold = 15;
			MongoClientOptions.Builder buide = new MongoClientOptions.Builder();
			buide.connectionsPerHost(CC.mp.mongo.maxConnectionsPerHost);// 与目标数据库可以建立的最大链接数
			buide.connectTimeout(CC.mp.mongo.connectTimeout);// 与数据库建立链接的超时时间
			buide.maxWaitTime(CC.mp.mongo.maxWaitTime);// 一个线程成功获取到一个可用数据库之前的最大等待时间
			buide.threadsAllowedToBlockForConnectionMultiplier(
					CC.mp.mongo.threadsAllowedToBlockForConnectionMultiplier);
			buide.maxConnectionIdleTime(CC.mp.mongo.maxConnectionIdleTime);
			buide.maxConnectionLifeTime(CC.mp.mongo.maxConnectionLifeTime);
			buide.socketTimeout(CC.mp.mongo.socketTimeout);
			buide.socketKeepAlive(CC.mp.mongo.socketKeepAlive);
			MongoClientOptions myOptions = buide.build();

			// 组装权限对象
			final List<MongoCredential> credentialsLists = new ArrayList<>();
			MongoCredential credential = MongoCredential.createCredential(user, database, password.toCharArray());
			credentialsLists.add(credential);

			// 创建客户端
			mongoClient = new MongoClient(addressLists, credentialsLists, myOptions);
		} catch (Exception e) {
			Logs.STORE.error("MongoDB init error", e);
		}
		return mongoClient;
	}

	@Override
	public void saveMessage(MessageModel messageModel, List<SendStatusModel> sendStatusModels) {
		if (messageModel != null) {
			Document document = getDocumentByMessage(messageModel);
			MongoCollection<Document> collection = getClient().getDatabase(databaseName)
					.getCollection(MESSAGE_TABLE_NAME);
			collection.insertOne(document);

			MongoCollection<Document> statusCollection = getClient().getDatabase(databaseName)
					.getCollection(MESSAGE_STATUS_TABLE_NAME);
			statusCollection.insertMany(getDocumentByMessageStatus(sendStatusModels));
		}
	}

	@Override
	public void delete(MessageModel messageModel) {
		if (messageModel != null) {
			Document document = getDocumentByMessage(messageModel);
			MongoCollection<Document> collection = getClient().getDatabase(databaseName)
					.getCollection(MESSAGE_TABLE_NAME);
			collection.deleteOne(document);
		}
	}

	@Override
	public void deleteById(String id) {

	}

	@Override
	public MessageModel findMessageById(String id) {
		return null;
	}

	private Document getDocumentByMessage(MessageModel messageModel) {

		String json = com.alibaba.fastjson.JSON.toJSONString(messageModel);
		Document document = Document.parse(json);
		return document;

	}

	private List<Document> getDocumentByMessageStatus(List<SendStatusModel> sendStatusModels) {

		List<Document> documents = new ArrayList<>();
		for (SendStatusModel model : sendStatusModels) {
			String json = com.alibaba.fastjson.JSON.toJSONString(model);

			Document document = Document.parse(json);
			documents.add(document);
		}
		return documents;

	}

	@Override
	public void updateMessageStatusByTargetId(MessageModel msg, String topic, SendMessageStatus sendsuccess) {

		getClient().getDatabase(databaseName).getCollection(MESSAGE_STATUS_TABLE_NAME).updateOne(
				Filters.and(Filters.eq("sendId", msg.getId()), Filters.eq("targetUserId", topic)),
				new Document("$set", new Document("status", sendsuccess.status)));

	}

	@Override
	public List<MessageModel> findNotSendMessageByTargetId(String targetId) {

		List<MessageModel> list = new ArrayList<MessageModel>();

		List<String> messageIds = new ArrayList<String>();

		MongoDatabase database = getClient().getDatabase(databaseName);
		FindIterable<Document> findIterable = database.getCollection(MESSAGE_STATUS_TABLE_NAME).find(
				Filters.and(Filters.eq("status", SendMessageStatus.notSend.status), Filters.eq("targetUserId", targetId)));

		findIterable.forEach(new Block<Document>() {

			@Override
			public void apply(Document arg0) {
				SendStatusModel model = JSON.parseObject(arg0.toJson(), SendStatusModel.class);
				if (model != null) {
					messageIds.add(model.getSendId());
				}
			}

		});

		FindIterable<Document> iterable = database.getCollection(MESSAGE_TABLE_NAME)
				.find(Filters.in("id", messageIds.toArray()));
		iterable.forEach(new Block<Document>() {

			@Override
			public void apply(Document arg0) {
				MessageModel model = JSON.parseObject(arg0.toJson(), MessageModel.class);
				if (model != null) {
					list.add(model);
				}
			}

		});

		return list;
	}

}
