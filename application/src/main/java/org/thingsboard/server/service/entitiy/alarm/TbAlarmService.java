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
package org.winstarcloud.server.service.entitiy.alarm;

import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.alarm.Alarm;
import org.winstarcloud.server.common.data.alarm.AlarmInfo;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.AlarmId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;

import java.util.List;

public interface TbAlarmService {

    Alarm save(Alarm entity, User user) throws WinstarcloudException;

    AlarmInfo ack(Alarm alarm, User user) throws WinstarcloudException;

    AlarmInfo ack(Alarm alarm, long ackTs, User user) throws WinstarcloudException;

    AlarmInfo clear(Alarm alarm, User user) throws WinstarcloudException;

    AlarmInfo clear(Alarm alarm, long clearTs, User user) throws WinstarcloudException;

    AlarmInfo assign(Alarm alarm, UserId assigneeId, long assignTs, User user) throws WinstarcloudException;

    AlarmInfo unassign(Alarm alarm, long unassignTs, User user) throws WinstarcloudException;

    List<AlarmId> unassignDeletedUserAlarms(TenantId tenantId, UserId userId, String userTitle, long unassignTs);

    Boolean delete(Alarm alarm, User user);
}
