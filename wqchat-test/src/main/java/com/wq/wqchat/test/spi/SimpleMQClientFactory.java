/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     qiong.wang (夜色)
 */

package com.wq.wqchat.test.spi;

import com.wq.wqchat.api.spi.Spi;
import com.wq.wqchat.api.spi.common.MQClient;
import com.wq.wqchat.api.spi.common.MQMessageReceiver;

/**
 * Created by ohun on 2016/12/28.
 *
 * @author qiong.wang (夜色)
 */
@Spi(order = 2)
public final class SimpleMQClientFactory implements com.wq.wqchat.api.spi.common.MQClientFactory {
    private MQClient mqClient = new MQClient() {
        @Override
        public void subscribe(String topic, MQMessageReceiver receiver) {
            System.err.println("subscribe " + topic);
        }

        @Override
        public void publish(String topic, Object message) {
            System.err.println("publish " + topic + " " + message);
        }

		@Override
		public void unSubscribe(String... channel) {
            System.err.println("unSubscribe " + channel );
			
		}
    };

    @Override
    public MQClient get() {
        return mqClient;
    }
}
