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
package org.winstarcloud.server.service.sync.ie.exporting.impl;

import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.NotificationTargetId;
import org.winstarcloud.server.common.data.notification.targets.NotificationTarget;
import org.winstarcloud.server.common.data.notification.targets.NotificationTargetType;
import org.winstarcloud.server.common.data.notification.targets.platform.CustomerUsersFilter;
import org.winstarcloud.server.common.data.notification.targets.platform.PlatformUsersNotificationTargetConfig;
import org.winstarcloud.server.common.data.notification.targets.platform.UsersFilter;
import org.winstarcloud.server.common.data.sync.ie.EntityExportData;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.sync.vc.data.EntitiesExportCtx;

import java.util.Set;

@Service
@TbCoreComponent
public class NotificationTargetExportService extends BaseEntityExportService<NotificationTargetId, NotificationTarget, EntityExportData<NotificationTarget>> {

    @Override
    protected void setRelatedEntities(EntitiesExportCtx<?> ctx, NotificationTarget notificationTarget, EntityExportData<NotificationTarget> exportData) {
        if (notificationTarget.getConfiguration().getType() == NotificationTargetType.PLATFORM_USERS) {
            UsersFilter usersFilter = ((PlatformUsersNotificationTargetConfig) notificationTarget.getConfiguration()).getUsersFilter();
            switch (usersFilter.getType()) {
                case CUSTOMER_USERS:
                    CustomerUsersFilter customerUsersFilter = (CustomerUsersFilter) usersFilter;
                    customerUsersFilter.setCustomerId(getExternalIdOrElseInternal(ctx, new CustomerId(customerUsersFilter.getCustomerId())).getId());
                    break;
                case USER_LIST:
                    // users list stays as is and is replaced with current user id on import (due to user entities not being supported by VC)
                    break;
            }
        }
    }

    @Override
    public Set<EntityType> getSupportedEntityTypes() {
        return Set.of(EntityType.NOTIFICATION_TARGET);
    }

}
