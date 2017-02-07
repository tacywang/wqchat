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

package com.wq.wqchat.bootstrap.job;

import com.wq.wqchat.api.spi.common.CacheManagerFactory;
import com.wq.wqchat.common.user.UserManager;


public final class CacheManagerBoot extends BootJob {

    @Override
    protected void start() {
        CacheManagerFactory.create().init();
        UserManager.I.clearUserOnlineData();
        startNext();
    }

    @Override
    protected void stop() {
        stopNext();
        CacheManagerFactory.create().destroy();
        UserManager.I.clearUserOnlineData();
    }
}
