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

package com.wq.wqchat.core.session;

import com.wq.wqchat.api.connection.SessionContext;
import com.wq.wqchat.api.spi.common.CacheManager;
import com.wq.wqchat.api.spi.common.CacheManagerFactory;
import com.wq.wqchat.common.CacheKeys;
import com.wq.wqchat.tools.common.Strings;
import com.wq.wqchat.tools.config.CC;
import com.wq.wqchat.tools.crypto.MD5Utils;


public final class ReusableSessionManager {
    public static final ReusableSessionManager I = new ReusableSessionManager();
    private final int expiredTime = CC.mp.core.session_expired_time;
    private final CacheManager cacheManager = CacheManagerFactory.create();

    public boolean cacheSession(ReusableSession session) {
        String key = CacheKeys.getSessionKey(session.sessionId);
        cacheManager.set(key, ReusableSession.encode(session.context), expiredTime);
        return true;
    }

    public ReusableSession querySession(String sessionId) {
        String key = CacheKeys.getSessionKey(sessionId);
        String value = cacheManager.get(key, String.class);
        if (Strings.isBlank(value)) return null;
        return ReusableSession.decode(value);
    }

    public ReusableSession genSession(SessionContext context) {
        long now = System.currentTimeMillis();
        ReusableSession session = new ReusableSession();
        session.context = context;
        session.sessionId = MD5Utils.encrypt(context.deviceId + now);
        session.expireTime = now + expiredTime * 1000;
        return session;
    }
}
