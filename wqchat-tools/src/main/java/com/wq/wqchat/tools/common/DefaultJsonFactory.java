package com.wq.wqchat.tools.common;

import com.wq.wqchat.api.spi.Spi;
import com.wq.wqchat.api.spi.common.Json;
import com.wq.wqchat.api.spi.common.JsonFactory;
import com.wq.wqchat.tools.Jsons;

@Spi
public final class DefaultJsonFactory implements JsonFactory, Json {
    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        return Jsons.fromJson(json, clazz);
    }

    @Override
    public String toJson(Object json) {
        return Jsons.toJson(json);
    }

    @Override
    public Json get() {
        return this;
    }
}