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
package org.winstarcloud.server.dao.alarm;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.EntitySubtype;
import org.winstarcloud.server.common.data.alarm.Alarm;
import org.winstarcloud.server.common.data.alarm.AlarmApiCallResult;
import org.winstarcloud.server.common.data.alarm.AlarmCreateOrUpdateActiveRequest;
import org.winstarcloud.server.common.data.alarm.AlarmInfo;
import org.winstarcloud.server.common.data.alarm.AlarmQuery;
import org.winstarcloud.server.common.data.alarm.AlarmQueryV2;
import org.winstarcloud.server.common.data.alarm.AlarmSeverity;
import org.winstarcloud.server.common.data.alarm.AlarmStatusFilter;
import org.winstarcloud.server.common.data.alarm.AlarmUpdateRequest;
import org.winstarcloud.server.common.data.alarm.EntityAlarm;
import org.winstarcloud.server.common.data.id.AlarmId;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.data.query.AlarmCountQuery;
import org.winstarcloud.server.common.data.query.AlarmData;
import org.winstarcloud.server.common.data.query.AlarmDataQuery;
import org.winstarcloud.server.common.data.util.TbPair;
import org.winstarcloud.server.dao.Dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ashvayka on 11.05.17.
 */
public interface AlarmDao extends Dao<Alarm> {

    Alarm findLatestByOriginatorAndType(TenantId tenantId, EntityId originator, String type);

    Alarm findLatestActiveByOriginatorAndType(TenantId tenantId, EntityId originator, String type);

    ListenableFuture<Alarm> findLatestByOriginatorAndTypeAsync(TenantId tenantId, EntityId originator, String type);

    Alarm findAlarmById(TenantId tenantId, UUID key);

    ListenableFuture<Alarm> findAlarmByIdAsync(TenantId tenantId, UUID key);

    AlarmInfo findAlarmInfoById(TenantId tenantId, UUID key);

    Alarm save(TenantId tenantId, Alarm alarm);

    PageData<AlarmInfo> findAlarms(TenantId tenantId, AlarmQuery query);

    PageData<AlarmInfo> findCustomerAlarms(TenantId tenantId, CustomerId customerId, AlarmQuery query);

    PageData<AlarmInfo> findAlarmsV2(TenantId tenantId, AlarmQueryV2 query);

    PageData<AlarmInfo> findCustomerAlarmsV2(TenantId tenantId, CustomerId customerId, AlarmQueryV2 query);

    PageData<AlarmData> findAlarmDataByQueryForEntities(TenantId tenantId, AlarmDataQuery query, Collection<EntityId> orderedEntityIds);

    Set<AlarmSeverity> findAlarmSeverities(TenantId tenantId, EntityId entityId, AlarmStatusFilter asf, String assigneeId);

    PageData<AlarmId> findAlarmsIdsByEndTsBeforeAndTenantId(Long time, TenantId tenantId, PageLink pageLink);

    PageData<AlarmId> findAlarmIdsByAssigneeId(TenantId tenantId, UUID userId, PageLink pageLink);

    PageData<TbPair<UUID, Long>> findAlarmIdsByOriginatorId(TenantId tenantId, EntityId originatorId, long createdTimeOffset, AlarmId idOffset, int limit);

    void createEntityAlarmRecord(EntityAlarm entityAlarm);

    List<EntityAlarm> findEntityAlarmRecords(TenantId tenantId, AlarmId id);

    List<EntityAlarm> findEntityAlarmRecordsByEntityId(TenantId tenantId, EntityId entityId);

    int deleteEntityAlarmRecords(TenantId tenantId, EntityId entityId);

    void deleteEntityAlarmRecordsByTenantId(TenantId tenantId);

    AlarmApiCallResult createOrUpdateActiveAlarm(AlarmCreateOrUpdateActiveRequest request, boolean alarmCreationEnabled);

    AlarmApiCallResult updateAlarm(AlarmUpdateRequest request);

    AlarmApiCallResult acknowledgeAlarm(TenantId tenantId, AlarmId id, long ackTs);

    AlarmApiCallResult clearAlarm(TenantId tenantId, AlarmId alarmId, long clearTs, JsonNode details);

    AlarmApiCallResult assignAlarm(TenantId tenantId, AlarmId alarmId, UserId assigneeId, long assignTime);

    AlarmApiCallResult unassignAlarm(TenantId tenantId, AlarmId alarmId, long unassignTime);

    long countAlarmsByQuery(TenantId tenantId, CustomerId customerId, AlarmCountQuery query);

    PageData<EntitySubtype> findTenantAlarmTypes(UUID tenantId, PageLink pageLink);

    boolean removeAlarmTypesIfNoAlarmsPresent(UUID tenantId, Set<String> types);
}
