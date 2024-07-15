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
package org.winstarcloud.server.service.state;

import org.springframework.context.ApplicationListener;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.msg.queue.TbCallback;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.queue.discovery.event.PartitionChangeEvent;

/**
 * Created by ashvayka on 01.05.18.
 */
public interface DeviceStateService extends ApplicationListener<PartitionChangeEvent> {

    void onDeviceConnect(TenantId tenantId, DeviceId deviceId, long lastConnectTime);

    default void onDeviceConnect(TenantId tenantId, DeviceId deviceId) {
        onDeviceConnect(tenantId, deviceId, System.currentTimeMillis());
    }

    void onDeviceActivity(TenantId tenantId, DeviceId deviceId, long lastReportedActivityTime);

    void onDeviceDisconnect(TenantId tenantId, DeviceId deviceId, long lastDisconnectTime);

    default void onDeviceDisconnect(TenantId tenantId, DeviceId deviceId) {
        onDeviceDisconnect(tenantId, deviceId, System.currentTimeMillis());
    }

    void onDeviceInactivity(TenantId tenantId, DeviceId deviceId, long lastInactivityTime);

    void onDeviceInactivityTimeoutUpdate(TenantId tenantId, DeviceId deviceId, long inactivityTimeout);

    void onQueueMsg(TransportProtos.DeviceStateServiceMsgProto proto, TbCallback bytes);

}
