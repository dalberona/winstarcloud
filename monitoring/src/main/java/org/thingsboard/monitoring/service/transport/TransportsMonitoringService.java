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
package org.winstarcloud.monitoring.service.transport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.winstarcloud.monitoring.config.transport.TransportMonitoringConfig;
import org.winstarcloud.monitoring.config.transport.TransportMonitoringTarget;
import org.winstarcloud.monitoring.service.BaseHealthChecker;
import org.winstarcloud.monitoring.service.BaseMonitoringService;

@Service
@RequiredArgsConstructor
@Slf4j
public final class TransportsMonitoringService extends BaseMonitoringService<TransportMonitoringConfig, TransportMonitoringTarget> {

    @Override
    protected BaseHealthChecker<?, ?> createHealthChecker(TransportMonitoringConfig config, TransportMonitoringTarget target) {
        return applicationContext.getBean(config.getTransportType().getServiceClass(), config, target);
    }

    @Override
    protected TransportMonitoringTarget createTarget(String baseUrl) {
        TransportMonitoringTarget target = new TransportMonitoringTarget();
        target.setBaseUrl(baseUrl);
        return target;
    }

    @Override
    protected String getName() {
        return "transports check";
    }

}
