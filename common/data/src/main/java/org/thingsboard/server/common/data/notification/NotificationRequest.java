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
package org.winstarcloud.server.common.data.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.winstarcloud.server.common.data.BaseData;
import org.winstarcloud.server.common.data.HasName;
import org.winstarcloud.server.common.data.HasTenantId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.NotificationRequestId;
import org.winstarcloud.server.common.data.id.NotificationRuleId;
import org.winstarcloud.server.common.data.id.NotificationTemplateId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.notification.info.NotificationInfo;
import org.winstarcloud.server.common.data.notification.template.NotificationTemplate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest extends BaseData<NotificationRequestId> implements HasTenantId, HasName {

    private TenantId tenantId;
    @NotEmpty
    private List<UUID> targets;

    private NotificationTemplateId templateId;
    @Valid
    private NotificationTemplate template;
    @Valid
    private NotificationInfo info;
    @Valid
    private NotificationRequestConfig additionalConfig;

    private EntityId originatorEntityId;
    private NotificationRuleId ruleId;

    private NotificationRequestStatus status;

    private NotificationRequestStats stats;

    public NotificationRequest(NotificationRequest other) {
        super(other);
        this.tenantId = other.tenantId;
        this.targets = other.targets;
        this.templateId = other.templateId;
        this.template = other.template;
        this.info = other.info;
        this.additionalConfig = other.additionalConfig;
        this.originatorEntityId = other.originatorEntityId;
        this.ruleId = other.ruleId;
        this.status = other.status;
        this.stats = other.stats;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return "To targets " + targets;
    }

    @JsonIgnore
    public UserId getSenderId() {
        return originatorEntityId instanceof UserId ? (UserId) originatorEntityId : null;
    }

    @JsonIgnore
    public boolean isSent() {
        return status == NotificationRequestStatus.SENT;
    }

    @JsonIgnore
    public boolean isScheduled() {
        return status == NotificationRequestStatus.SCHEDULED;
    }

}
