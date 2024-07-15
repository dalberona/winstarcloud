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
package org.winstarcloud.server.service.entitiy.device;

import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.Device;
import org.winstarcloud.server.common.data.Tenant;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.security.DeviceCredentials;
import org.winstarcloud.server.dao.device.claim.ClaimResult;
import org.winstarcloud.server.dao.device.claim.ReclaimResult;

public interface TbDeviceService {

    Device save(Device device, String accessToken, User user) throws Exception;

    Device saveDeviceWithCredentials(Device device, DeviceCredentials deviceCredentials, User user) throws WinstarcloudException;

    void delete(Device device, User user);

    Device assignDeviceToCustomer(TenantId tenantId, DeviceId deviceId, Customer customer, User user) throws WinstarcloudException;

    Device unassignDeviceFromCustomer(Device device, Customer customer, User user) throws WinstarcloudException;

    Device assignDeviceToPublicCustomer(TenantId tenantId, DeviceId deviceId, User user) throws WinstarcloudException;

    DeviceCredentials getDeviceCredentialsByDeviceId(Device device, User user) throws WinstarcloudException;

    DeviceCredentials updateDeviceCredentials(Device device, DeviceCredentials deviceCredentials, User user) throws WinstarcloudException;

    ListenableFuture<ClaimResult> claimDevice(TenantId tenantId, Device device, CustomerId customerId, String secretKey, User user);

    ListenableFuture<ReclaimResult> reclaimDevice(TenantId tenantId, Device device, User user);

    Device assignDeviceToTenant(Device device, Tenant newTenant, User user);

    Device assignDeviceToEdge(TenantId tenantId, DeviceId deviceId, Edge edge, User user) throws WinstarcloudException;

    Device unassignDeviceFromEdge(Device device, Edge edge, User user) throws WinstarcloudException;
}
