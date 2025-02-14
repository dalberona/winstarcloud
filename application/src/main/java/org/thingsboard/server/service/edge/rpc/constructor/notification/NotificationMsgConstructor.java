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
package org.winstarcloud.server.service.edge.rpc.constructor.notification;

import org.winstarcloud.server.common.data.id.NotificationRuleId;
import org.winstarcloud.server.common.data.id.NotificationTargetId;
import org.winstarcloud.server.common.data.id.NotificationTemplateId;
import org.winstarcloud.server.common.data.notification.rule.NotificationRule;
import org.winstarcloud.server.common.data.notification.targets.NotificationTarget;
import org.winstarcloud.server.common.data.notification.template.NotificationTemplate;
import org.winstarcloud.server.gen.edge.v1.NotificationRuleUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.NotificationTargetUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.NotificationTemplateUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.UpdateMsgType;

public interface NotificationMsgConstructor {

    NotificationRuleUpdateMsg constructNotificationRuleUpdateMsg(UpdateMsgType msgType, NotificationRule notificationRule);

    NotificationRuleUpdateMsg constructNotificationRuleDeleteMsg(NotificationRuleId notificationRuleId);

    NotificationTargetUpdateMsg constructNotificationTargetUpdateMsg(UpdateMsgType msgType, NotificationTarget notificationTarget);

    NotificationTargetUpdateMsg constructNotificationTargetDeleteMsg(NotificationTargetId notificationTargetId);

    NotificationTemplateUpdateMsg constructNotificationTemplateUpdateMsg(UpdateMsgType msgType, NotificationTemplate notificationTemplate);

    NotificationTemplateUpdateMsg constructNotificationTemplateDeleteMsg(NotificationTemplateId notificationTemplateId);

}
