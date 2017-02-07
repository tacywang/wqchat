package com.wq.wqchat.api.service;

public interface Listener {
    void onSuccess(Object... args);

    void onFailure(Throwable cause);
}