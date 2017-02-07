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

package com.wq.wqchat.core.ack;

import java.util.concurrent.Future;


public final class AckTask implements Runnable {
    final int ackMessageId;

    private AckCallback callback;
    private Future<?> timeoutFuture;

    public AckTask(int ackMessageId) {
        this.ackMessageId = ackMessageId;
    }

    public static AckTask from(int ackMessageId) {
        return new AckTask(ackMessageId);
    }

    public void setFuture(Future<?> future) {
        this.timeoutFuture = future;
    }

    public AckTask setCallback(AckCallback callback) {
        this.callback = callback;
        return this;
    }

    private boolean tryDone() {
        return timeoutFuture.cancel(true);
    }

    public void onResponse() {
        if (tryDone()) {
            callback.onSuccess(this);
            callback = null;
        }
    }

    public void onTimeout() {
        AckTask context = AckTaskQueue.I.getAndRemove(ackMessageId);
        if (context != null && tryDone()) {
            callback.onTimeout(this);
            callback = null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                ", ackMessageId=" + ackMessageId +
                '}';
    }

    @Override
    public void run() {
        onTimeout();
    }
}
