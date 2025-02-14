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
package org.winstarcloud.server.common.data.notification.info;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.limit.LimitedApi;

import java.util.Map;

import static org.winstarcloud.server.common.data.util.CollectionsUtil.mapOf;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitsNotificationInfo implements RuleOriginatedNotificationInfo {

    private TenantId tenantId;
    private String tenantName;
    private LimitedApi api;
    private EntityId limitLevel;
    private String limitLevelEntityName;

    @Override
    public Map<String, String> getTemplateData() {
        return mapOf(
                "api", api.getLabel(),
                "limitLevelEntityType", limitLevel != null ? limitLevel.getEntityType().getNormalName() : null,
                "limitLevelEntityId", limitLevel != null ? limitLevel.getId().toString() : null,
                "limitLevelEntityName", limitLevelEntityName,
                "tenantName", tenantName,
                "tenantId", tenantId.toString()
        );
    }

    @Override
    public TenantId getAffectedTenantId() {
        return tenantId;
    }

}
