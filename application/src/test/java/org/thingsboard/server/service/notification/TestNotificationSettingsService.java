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
package org.winstarcloud.server.service.notification;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.dao.notification.DefaultNotificationSettingsService;
import org.winstarcloud.server.dao.notification.NotificationTargetService;
import org.winstarcloud.server.dao.notification.NotificationTemplateService;
import org.winstarcloud.server.dao.settings.AdminSettingsService;
import org.winstarcloud.server.dao.user.UserSettingsService;

@Service
@Primary
public class TestNotificationSettingsService extends DefaultNotificationSettingsService {

    public TestNotificationSettingsService(AdminSettingsService adminSettingsService,
                                           NotificationTargetService notificationTargetService,
                                           NotificationTemplateService notificationTemplateService,
                                           UserSettingsService userSettingsService) {
        super(adminSettingsService, notificationTargetService, notificationTemplateService, null, userSettingsService);
    }

    @Override
    public void createDefaultNotificationConfigs(TenantId tenantId) {
        // do nothing
    }

}
