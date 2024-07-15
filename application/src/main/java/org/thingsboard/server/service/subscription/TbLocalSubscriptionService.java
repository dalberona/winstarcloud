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
package org.winstarcloud.server.service.subscription;

import org.winstarcloud.server.common.data.alarm.AlarmInfo;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.kv.TsKvEntry;
import org.winstarcloud.server.common.msg.queue.TbCallback;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.queue.discovery.event.ClusterTopologyChangeEvent;
import org.winstarcloud.server.service.ws.notification.sub.NotificationRequestUpdate;
import org.winstarcloud.server.service.ws.notification.sub.NotificationsSubscriptionUpdate;

import java.util.List;

public interface TbLocalSubscriptionService {

    void addSubscription(TbSubscription<?> subscription);

    void onSubEventCallback(TransportProtos.TbEntitySubEventCallbackProto subEventCallback, TbCallback callback);

    void onSubEventCallback(EntityId entityId, int seqNumber, TbEntityUpdatesInfo entityUpdatesInfo, TbCallback empty);

    void cancelSubscription(String sessionId, int subscriptionId);

    void cancelAllSessionSubscriptions(String sessionId);

    void onTimeSeriesUpdate(TransportProtos.TbSubUpdateProto tsUpdate, TbCallback callback);

    void onTimeSeriesUpdate(EntityId entityId, List<TsKvEntry> update, TbCallback callback);

    void onAttributesUpdate(TransportProtos.TbSubUpdateProto attrUpdate, TbCallback callback);

    void onAttributesUpdate(EntityId entityId, String scope, List<TsKvEntry> update, TbCallback callback);

    void onAlarmUpdate(EntityId entityId, AlarmInfo alarm, boolean deleted, TbCallback callback);

    void onAlarmUpdate(TransportProtos.TbAlarmSubUpdateProto update, TbCallback callback);

    void onNotificationUpdate(EntityId entityId, NotificationsSubscriptionUpdate subscriptionUpdate, TbCallback callback);

    void onApplicationEvent(ClusterTopologyChangeEvent event);

    void onCoreStartupMsg(TransportProtos.CoreStartupMsg coreStartupMsg);

    void onNotificationRequestUpdate(TenantId tenantId, NotificationRequestUpdate update, TbCallback callback);

    void onNotificationUpdate(TransportProtos.NotificationsSubUpdateProto notificationsUpdate, TbCallback callback);

}
