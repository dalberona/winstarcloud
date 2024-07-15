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
package org.winstarcloud.server.service.notification.channels;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.winstarcloud.rule.engine.api.notification.SlackService;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.notification.NotificationDeliveryMethod;
import org.winstarcloud.server.common.data.notification.settings.NotificationSettings;
import org.winstarcloud.server.common.data.notification.settings.SlackNotificationDeliveryMethodConfig;
import org.winstarcloud.server.common.data.notification.targets.slack.SlackConversation;
import org.winstarcloud.server.common.data.notification.template.SlackDeliveryMethodNotificationTemplate;
import org.winstarcloud.server.dao.notification.NotificationSettingsService;
import org.winstarcloud.server.service.notification.NotificationProcessingContext;

@Component
@RequiredArgsConstructor
public class SlackNotificationChannel implements NotificationChannel<SlackConversation, SlackDeliveryMethodNotificationTemplate> {

    private final SlackService slackService;
    private final NotificationSettingsService notificationSettingsService;

    @Override
    public void sendNotification(SlackConversation conversation, SlackDeliveryMethodNotificationTemplate processedTemplate, NotificationProcessingContext ctx) throws Exception {
        SlackNotificationDeliveryMethodConfig config = ctx.getDeliveryMethodConfig(NotificationDeliveryMethod.SLACK);
        slackService.sendMessage(ctx.getTenantId(), config.getBotToken(), conversation.getId(), processedTemplate.getBody());
    }

    @Override
    public void check(TenantId tenantId) throws Exception {
        NotificationSettings notificationSettings = notificationSettingsService.findNotificationSettings(tenantId);
        if (!notificationSettings.getDeliveryMethodsConfigs().containsKey(NotificationDeliveryMethod.SLACK)) {
            throw new RuntimeException("Slack API token is not configured");
        }
    }

    @Override
    public NotificationDeliveryMethod getDeliveryMethod() {
        return NotificationDeliveryMethod.SLACK;
    }

}
