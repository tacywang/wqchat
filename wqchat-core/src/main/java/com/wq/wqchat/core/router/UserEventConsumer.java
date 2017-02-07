/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   qiong.wang (夜色)
 */

package com.wq.wqchat.core.router;

import com.google.common.eventbus.Subscribe;
import com.wq.wqchat.api.event.UserOfflineEvent;
import com.wq.wqchat.api.event.UserOnlineEvent;
import com.wq.wqchat.api.spi.common.MQClient;
import com.wq.wqchat.api.spi.common.MQClientFactory;
import com.wq.wqchat.common.user.UserManager;
import com.wq.wqchat.tools.event.EventConsumer;

import static com.wq.wqchat.api.event.Topics.OFFLINE_CHANNEL;
import static com.wq.wqchat.api.event.Topics.ONLINE_CHANNEL;


/* package */ final class UserEventConsumer extends EventConsumer {

	private final MQClient mqClient = MQClientFactory.create();

	@Subscribe
	void on(UserOnlineEvent event) {
		UserManager.I.addToOnlineList(event.getUserId());
		mqClient.publish(ONLINE_CHANNEL, event.getUserId());
	}

	@Subscribe
	void on(UserOfflineEvent event) {
		UserManager.I.remFormOnlineList(event.getUserId());
		mqClient.publish(OFFLINE_CHANNEL, event.getUserId());
		LocalRouterManager localRouterManager = RouterCenter.I.getLocalRouterManager();

		// 如果该服务端没有该用户客户端连接了需要接触订阅
		if (localRouterManager.lookupAll(event.getUserId()) == null
				|| localRouterManager.lookupAll(event.getUserId()).size() == 0) {
			mqClient.unSubscribe(event.getUserId());
		}
	}
}
