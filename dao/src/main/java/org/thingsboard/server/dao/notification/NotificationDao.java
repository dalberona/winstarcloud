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
package org.winstarcloud.server.dao.notification;

import org.winstarcloud.server.common.data.id.NotificationId;
import org.winstarcloud.server.common.data.id.NotificationRequestId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.notification.Notification;
import org.winstarcloud.server.common.data.notification.NotificationDeliveryMethod;
import org.winstarcloud.server.common.data.notification.NotificationStatus;
import org.winstarcloud.server.common.data.notification.NotificationType;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.Dao;

import java.util.Set;

public interface NotificationDao extends Dao<Notification> {

    PageData<Notification> findUnreadByDeliveryMethodAndRecipientIdAndPageLink(TenantId tenantId, NotificationDeliveryMethod deliveryMethod, UserId recipientId, PageLink pageLink);

    PageData<Notification> findUnreadByDeliveryMethodAndRecipientIdAndNotificationTypesAndPageLink(TenantId tenantId, NotificationDeliveryMethod deliveryMethod, UserId recipientId, Set<NotificationType> types, PageLink pageLink);

    PageData<Notification> findByDeliveryMethodAndRecipientIdAndPageLink(TenantId tenantId, NotificationDeliveryMethod deliveryMethod, UserId recipientId, PageLink pageLink);

    boolean updateStatusByIdAndRecipientId(TenantId tenantId, UserId recipientId, NotificationId notificationId, NotificationStatus status);

    int countUnreadByDeliveryMethodAndRecipientId(TenantId tenantId, NotificationDeliveryMethod deliveryMethod, UserId recipientId);

    boolean deleteByIdAndRecipientId(TenantId tenantId, UserId recipientId, NotificationId notificationId);

    int updateStatusByDeliveryMethodAndRecipientId(TenantId tenantId, NotificationDeliveryMethod deliveryMethod, UserId recipientId, NotificationStatus status);

    void deleteByRequestId(TenantId tenantId, NotificationRequestId requestId);

    void deleteByRecipientId(TenantId tenantId, UserId recipientId);

}
