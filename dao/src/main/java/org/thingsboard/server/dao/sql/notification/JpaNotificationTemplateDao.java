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
package org.winstarcloud.server.dao.sql.notification;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.id.NotificationTemplateId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.notification.NotificationType;
import org.winstarcloud.server.common.data.notification.template.NotificationTemplate;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.DaoUtil;
import org.winstarcloud.server.dao.model.sql.NotificationTemplateEntity;
import org.winstarcloud.server.dao.notification.NotificationTemplateDao;
import org.winstarcloud.server.dao.sql.JpaAbstractDao;
import org.winstarcloud.server.dao.util.SqlDao;

import java.util.List;
import java.util.UUID;

@Component
@SqlDao
@RequiredArgsConstructor
public class JpaNotificationTemplateDao extends JpaAbstractDao<NotificationTemplateEntity, NotificationTemplate> implements NotificationTemplateDao {

    private final NotificationTemplateRepository notificationTemplateRepository;

    @Override
    protected Class<NotificationTemplateEntity> getEntityClass() {
        return NotificationTemplateEntity.class;
    }

    @Override
    public PageData<NotificationTemplate> findByTenantIdAndNotificationTypesAndPageLink(TenantId tenantId, List<NotificationType> notificationTypes, PageLink pageLink) {
        return DaoUtil.toPageData(notificationTemplateRepository.findByTenantIdAndNotificationTypesAndSearchText(tenantId.getId(),
                notificationTypes, pageLink.getTextSearch(), DaoUtil.toPageable(pageLink)));
    }

    @Override
    public int countByTenantIdAndNotificationTypes(TenantId tenantId, List<NotificationType> notificationTypes) {
        return notificationTemplateRepository.countByTenantIdAndNotificationTypes(tenantId.getId(), notificationTypes);
    }

    @Override
    public void removeByTenantId(TenantId tenantId) {
        notificationTemplateRepository.deleteByTenantId(tenantId.getId());
    }

    @Override
    public NotificationTemplate findByTenantIdAndExternalId(UUID tenantId, UUID externalId) {
        return DaoUtil.getData(notificationTemplateRepository.findByTenantIdAndExternalId(tenantId, externalId));
    }

    @Override
    public NotificationTemplate findByTenantIdAndName(UUID tenantId, String name) {
        return DaoUtil.getData(notificationTemplateRepository.findByTenantIdAndName(tenantId, name));
    }

    @Override
    public PageData<NotificationTemplate> findByTenantId(UUID tenantId, PageLink pageLink) {
        return DaoUtil.toPageData(notificationTemplateRepository.findByTenantId(tenantId, DaoUtil.toPageable(pageLink)));
    }

    @Override
    public NotificationTemplateId getExternalIdByInternal(NotificationTemplateId internalId) {
        return DaoUtil.toEntityId(notificationTemplateRepository.getExternalIdByInternal(internalId.getId()), NotificationTemplateId::new);
    }

    @Override
    protected JpaRepository<NotificationTemplateEntity, UUID> getRepository() {
        return notificationTemplateRepository;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.NOTIFICATION_TEMPLATE;
    }

}
