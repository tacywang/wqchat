package com.wq.wqchat.core.external;

import java.util.List;

import com.wq.wqchat.api.spi.Spi;
import com.wq.wqchat.api.spi.external.ExternalService;
import com.wq.wqchat.api.spi.external.ExternalServiceFactory;

@Spi(order = 1)
public class DefaultExternalService implements ExternalService,ExternalServiceFactory{

	@Override
	public ExternalService get() {
		return this;
	}

	@Override
	public boolean userExist(String userId) {
		return true;
	}

	@Override
	public List<String> findUserListByChatRoomId(String targetId) {
		return null;
	}

	@Override
	public boolean isFriends(String userId, String targetId) {
		return true;
	}

}
