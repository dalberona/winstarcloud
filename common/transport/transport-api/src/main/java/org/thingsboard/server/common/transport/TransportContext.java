/**
 * Copyright © 2016-2024 The Winstarcloud Authors
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
 */
package org.winstarcloud.server.common.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.winstarcloud.common.util.WinstarCloudExecutors;
import org.winstarcloud.server.cache.ota.OtaPackageDataCache;
import org.winstarcloud.server.common.transport.limits.TransportRateLimitService;
import org.winstarcloud.server.queue.discovery.TbServiceInfoProvider;
import org.winstarcloud.server.queue.scheduler.SchedulerComponent;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

/**
 * Created by ashvayka on 15.10.18.
 */
@Slf4j
@Data
public abstract class TransportContext {

    protected final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected TransportService transportService;

    @Autowired
    private TbServiceInfoProvider serviceInfoProvider;

    @Autowired
    private SchedulerComponent scheduler;

    @Getter
    private ExecutorService executor;

    @Getter
    @Autowired
    private OtaPackageDataCache otaPackageDataCache;

    @Autowired
    private TransportResourceCache transportResourceCache;

    @Autowired
    protected TransportRateLimitService rateLimitService;

    @PostConstruct
    public void init() {
        executor = WinstarCloudExecutors.newWorkStealingPool(50, getClass());
    }

    @PreDestroy
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public String getNodeId() {
        return serviceInfoProvider.getServiceId();
    }



}
