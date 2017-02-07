package com.wq.wqchat.api.spi.external;

import java.util.List;

public interface ExternalService {

	boolean userExist(String userId);

	List<String> findUserListByChatRoomId(String targetId);

	boolean isFriends(String userId, String targetId);

}
