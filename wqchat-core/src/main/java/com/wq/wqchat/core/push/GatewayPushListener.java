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

package com.wq.wqchat.core.push;

import com.wq.wqchat.api.spi.Spi;
import com.wq.wqchat.api.spi.push.PushListener;
import com.wq.wqchat.api.spi.push.PushListenerFactory;
import com.wq.wqchat.common.message.ErrorMessage;
import com.wq.wqchat.common.message.OkMessage;
import com.wq.wqchat.common.message.gateway.GatewayPushMessage;
import com.wq.wqchat.tools.Jsons;
import com.wq.wqchat.tools.log.Logs;

import java.util.concurrent.ScheduledExecutorService;

import static com.wq.wqchat.common.ErrorCode.*;
import static com.wq.wqchat.common.push.GatewayPushResult.toJson;

@Spi(order = 1)
public final class GatewayPushListener implements PushListener<GatewayPushMessage>, PushListenerFactory<GatewayPushMessage> {

    @Override
    public void onSuccess(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            PushCenter.I.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage
                            .from(message)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client success, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onAckSuccess(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            PushCenter.I.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage
                            .from(message)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });

        } else {
            Logs.PUSH.warn("client ack success, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onBroadcastComplete(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            PushCenter.I.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    OkMessage
                            .from(message)
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("broadcast to client finish, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onFailure(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            PushCenter.I.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage
                            .from(message)
                            .setErrorCode(PUSH_CLIENT_FAILURE)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client failure, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onOffline(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            PushCenter.I.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage
                            .from(message)
                            .setErrorCode(OFFLINE)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client offline, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }

    @Override
    public void onRedirect(GatewayPushMessage message, Object[] timePoints) {
        if (message.getConnection().isConnected()) {
            PushCenter.I.addTask(new PushTask() {
                @Override
                public ScheduledExecutorService getExecutor() {
                    return message.getExecutor();
                }

                @Override
                public void run() {
                    ErrorMessage
                            .from(message)
                            .setErrorCode(ROUTER_CHANGE)
                            .setData(toJson(message, timePoints))
                            .sendRaw();
                }
            });
        } else {
            Logs.PUSH.warn("push message to client redirect, but gateway connection is closed, timePoints={}, message={}"
                    , Jsons.toJson(timePoints), message);
        }
    }


    @Override
    public void onTimeout(GatewayPushMessage message, Object[] timePoints) {
        Logs.PUSH.warn("push message to client timeout, timePoints={}, message={}"
                , Jsons.toJson(timePoints), message);
    }

    @Override
    public PushListener<GatewayPushMessage> get() {
        return this;
    }
}
