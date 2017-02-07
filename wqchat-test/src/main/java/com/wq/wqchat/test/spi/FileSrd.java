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

package com.wq.wqchat.test.spi;

import com.google.common.collect.Lists;
import com.wq.wqchat.api.service.BaseService;
import com.wq.wqchat.api.srd.*;
import com.wq.wqchat.tools.Jsons;

import java.util.List;

/**
 * Created by ohun on 2016/12/28.
 *
 * @author qiong.wang (夜色)
 */
public final class FileSrd extends BaseService implements ServiceRegistry, ServiceDiscovery {

    public static final FileSrd I = new FileSrd();

    @Override
    public void init() {

    }

    @Override
    public void register(ServiceNode node) {
        FileCacheManger.I.hset(node.serviceName(), node.nodeId(), Jsons.toJson(node));
    }

    @Override
    public void deregister(ServiceNode node) {
        FileCacheManger.I.hdel(node.serviceName(), node.nodeId());
    }

    @Override
    public List<ServiceNode> lookup(String path) {
        return Lists.newArrayList(FileCacheManger.I.hgetAll(path, CommonServiceNode.class).values());
    }

    @Override
    public void subscribe(String path, ServiceListener listener) {

    }

    @Override
    public void unsubscribe(String path, ServiceListener listener) {

    }
}
