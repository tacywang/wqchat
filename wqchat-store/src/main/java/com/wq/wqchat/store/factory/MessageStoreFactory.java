package com.wq.wqchat.store.factory;

import com.wq.wqchat.api.spi.Factory;
import com.wq.wqchat.api.spi.SpiLoader;
import com.wq.wqchat.store.MessageStore;

public interface MessageStoreFactory extends Factory<MessageStore>  {
	static MessageStore create() {
        return SpiLoader.load(MessageStoreFactory.class).get();
    }
}
