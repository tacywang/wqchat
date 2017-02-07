package com.wq.wqchat.api.spi.external;

import com.wq.wqchat.api.spi.Factory;
import com.wq.wqchat.api.spi.SpiLoader;

public interface ExternalServiceFactory extends Factory<ExternalService>  {
	static ExternalService create() {
        return SpiLoader.load(ExternalServiceFactory.class).get();
    }
}
