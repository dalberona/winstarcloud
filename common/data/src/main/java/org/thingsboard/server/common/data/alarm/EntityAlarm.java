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
package org.winstarcloud.server.common.data.alarm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.winstarcloud.server.common.data.HasTenantId;
import org.winstarcloud.server.common.data.id.AlarmId;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityAlarm implements HasTenantId {

    private TenantId tenantId;
    private EntityId entityId;
    private long createdTime;
    private String alarmType;

    private CustomerId customerId;
    private UserId assigneeId;
    private AlarmId alarmId;

}
