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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.housekeeper.HousekeeperTask;
import org.winstarcloud.server.common.data.notification.info.TaskProcessingFailureNotificationInfo;
import org.winstarcloud.server.common.data.notification.rule.trigger.TaskProcessingFailureTrigger;
import org.winstarcloud.server.common.data.notification.rule.trigger.config.NotificationRuleTriggerType;
import org.winstarcloud.server.common.data.notification.rule.trigger.config.TaskProcessingFailureNotificationRuleTriggerConfig;

@Service
public class TaskProcessingFailureTriggerProcessor implements NotificationRuleTriggerProcessor<TaskProcessingFailureTrigger, TaskProcessingFailureNotificationRuleTriggerConfig> {

    @Override
    public boolean matchesFilter(TaskProcessingFailureTrigger trigger, TaskProcessingFailureNotificationRuleTriggerConfig triggerConfig) {
        return true;
    }

    @Override
    public TaskProcessingFailureNotificationInfo constructNotificationInfo(TaskProcessingFailureTrigger trigger) {
        HousekeeperTask task = trigger.getTask();
        return TaskProcessingFailureNotificationInfo.builder()
                .tenantId(task.getTenantId())
                .entityId(task.getEntityId())
                .taskType(task.getTaskType())
                .taskDescription(task.getDescription())
                .error(StringUtils.truncate(ExceptionUtils.getStackTrace(trigger.getError()), 1024))
                .attempt(trigger.getAttempt())
                .build();
    }

    @Override
    public NotificationRuleTriggerType getTriggerType() {
        return NotificationRuleTriggerType.TASK_PROCESSING_FAILURE;
    }

}
