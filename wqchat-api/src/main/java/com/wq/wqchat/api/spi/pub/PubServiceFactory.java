package com.wq.wqchat.api.spi.pub;

import com.wq.wqchat.api.spi.Factory;
import com.wq.wqchat.api.spi.SpiLoader;

public interface PubServiceFactory extends Factory<PubService>  {
	static PubService create() {
        return SpiLoader.load(PubServiceFactory.class).get();
    }
}
