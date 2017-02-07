package com.wq.wqchat.core.push;

import com.wq.wqchat.api.spi.push.IPushMessage;
import com.wq.wqchat.core.ack.AckCallback;
import com.wq.wqchat.core.ack.AckTask;
import com.wq.wqchat.tools.common.TimeLine;
import com.wq.wqchat.tools.log.Logs;

public final class PushAckCallback implements AckCallback {
    private final IPushMessage message;
    private final TimeLine timeLine;

    public PushAckCallback(IPushMessage message, TimeLine timeLine) {
        this.message = message;
        this.timeLine = timeLine;
    }

    @Override
    public void onSuccess(AckTask task) {
        PushCenter.I.getPushListener().onAckSuccess(message, timeLine.successEnd().getTimePoints());
        Logs.PUSH.info("[SingleUserPush] client ack success, timeLine={}, task={}", timeLine, task);
    }

    @Override
    public void onTimeout(AckTask task) {
        PushCenter.I.getPushListener().onTimeout(message, timeLine.timeoutEnd().getTimePoints());
        Logs.PUSH.warn("[SingleUserPush] client ack timeout, timeLine={}, task={}", timeLine, task);
    }
}