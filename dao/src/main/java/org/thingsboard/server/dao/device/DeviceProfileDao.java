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
package org.winstarcloud.server.dao.device;

import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.DeviceProfileInfo;
import org.winstarcloud.server.common.data.EntityInfo;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.Dao;
import org.winstarcloud.server.dao.ExportableEntityDao;
import org.winstarcloud.server.dao.ImageContainerDao;

import java.util.List;
import java.util.UUID;

public interface DeviceProfileDao extends Dao<DeviceProfile>, ExportableEntityDao<DeviceProfileId, DeviceProfile>, ImageContainerDao<DeviceProfileInfo> {

    DeviceProfileInfo findDeviceProfileInfoById(TenantId tenantId, UUID deviceProfileId);

    DeviceProfile save(TenantId tenantId, DeviceProfile deviceProfile);

    DeviceProfile saveAndFlush(TenantId tenantId, DeviceProfile deviceProfile);

    PageData<DeviceProfile> findDeviceProfiles(TenantId tenantId, PageLink pageLink);

    PageData<DeviceProfileInfo> findDeviceProfileInfos(TenantId tenantId, PageLink pageLink, String transportType);

    DeviceProfile findDefaultDeviceProfile(TenantId tenantId);

    DeviceProfileInfo findDefaultDeviceProfileInfo(TenantId tenantId);

    DeviceProfile findByProvisionDeviceKey(String provisionDeviceKey);

    DeviceProfile findByName(TenantId tenantId, String profileName);

    PageData<DeviceProfile> findAllWithImages(PageLink pageLink);

    List<EntityInfo> findTenantDeviceProfileNames(UUID tenantId, boolean activeOnly);

}
