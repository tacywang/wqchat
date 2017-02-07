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

package com.wq.wqchat.tools.event;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.wq.wqchat.api.event.Event;
import com.wq.wqchat.tools.thread.pool.ThreadPoolManager;


public class EventBus {
    private final Logger LOGGER = LoggerFactory.getLogger(EventBus.class);
    public static final EventBus I = new EventBus();
    private final com.google.common.eventbus.EventBus eventBus;

    public EventBus() {
        Executor executor = ThreadPoolManager.I.getEventBusExecutor();
        eventBus = new AsyncEventBus(executor, (exception, context)
                -> LOGGER.error("event bus subscriber ex", exception));
    }

    public void post(Event event) {
        eventBus.post(event);
    }

    public void register(Object bean) {
        eventBus.register(bean);
    }

    public void unregister(Object bean) {
        eventBus.unregister(bean);
    }

}
