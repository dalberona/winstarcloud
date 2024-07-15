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
package org.winstarcloud.server.service.notification.rule.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.notification.info.ApiUsageLimitNotificationInfo;
import org.winstarcloud.server.common.data.notification.info.RuleOriginatedNotificationInfo;
import org.winstarcloud.server.common.data.notification.rule.trigger.config.ApiUsageLimitNotificationRuleTriggerConfig;
import org.winstarcloud.server.common.data.notification.rule.trigger.config.NotificationRuleTriggerType;
import org.winstarcloud.server.common.data.notification.rule.trigger.ApiUsageLimitTrigger;
import org.winstarcloud.server.dao.tenant.TenantService;

import static org.winstarcloud.server.common.data.util.CollectionsUtil.emptyOrContains;

@Service
@RequiredArgsConstructor
public class ApiUsageLimitTriggerProcessor implements NotificationRuleTriggerProcessor<ApiUsageLimitTrigger, ApiUsageLimitNotificationRuleTriggerConfig> {

    private final TenantService tenantService;

    @Override
    public boolean matchesFilter(ApiUsageLimitTrigger trigger, ApiUsageLimitNotificationRuleTriggerConfig triggerConfig) {
        return emptyOrContains(triggerConfig.getApiFeatures(), trigger.getState().getApiFeature()) &&
                emptyOrContains(triggerConfig.getNotifyOn(), trigger.getStatus());
    }

    @Override
    public RuleOriginatedNotificationInfo constructNotificationInfo(ApiUsageLimitTrigger trigger) {
        return ApiUsageLimitNotificationInfo.builder()
                .feature(trigger.getState().getApiFeature())
                .recordKey(trigger.getState().getKey())
                .status(trigger.getStatus())
                .limit(trigger.getState().getThresholdAsString())
                .currentValue(trigger.getState().getValueAsString())
                .tenantId(trigger.getTenantId())
                .tenantName(tenantService.findTenantById(trigger.getTenantId()).getName())
                .build();
    }

    @Override
    public NotificationRuleTriggerType getTriggerType() {
        return NotificationRuleTriggerType.API_USAGE_LIMIT;
    }

}
