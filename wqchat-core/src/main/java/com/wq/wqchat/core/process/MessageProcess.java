package com.wq.wqchat.core.process;

import com.wq.wqchat.common.message.PushMessage;

public interface MessageProcess {

	public void process(PushMessage message);
}
