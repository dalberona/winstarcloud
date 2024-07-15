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
package org.winstarcloud.server.service.edge.rpc.fetch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.server.common.data.EdgeUtils;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.edge.EdgeEvent;
import org.winstarcloud.server.common.data.edge.EdgeEventActionType;
import org.winstarcloud.server.common.data.edge.EdgeEventType;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.notification.NotificationType;
import org.winstarcloud.server.common.data.notification.template.NotificationTemplate;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.notification.NotificationTemplateService;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class NotificationTemplateEdgeEventFetcher extends BasePageableEdgeEventFetcher<NotificationTemplate> {

    private NotificationTemplateService notificationTemplateService;

    @Override
    PageData<NotificationTemplate> fetchEntities(TenantId tenantId, Edge edge, PageLink pageLink) {
        return notificationTemplateService.findNotificationTemplatesByTenantIdAndNotificationTypes(tenantId, List.of(NotificationType.values()), pageLink);
    }

    @Override
    EdgeEvent constructEdgeEvent(TenantId tenantId, Edge edge, NotificationTemplate notificationTemplate) {
        return EdgeUtils.constructEdgeEvent(tenantId, edge.getId(), EdgeEventType.NOTIFICATION_TEMPLATE,
                EdgeEventActionType.ADDED, notificationTemplate.getId(), null);
    }

}
