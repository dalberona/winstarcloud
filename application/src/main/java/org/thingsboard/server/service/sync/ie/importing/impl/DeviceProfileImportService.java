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
package org.winstarcloud.server.service.sync.ie.importing.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.audit.ActionType;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.sync.ie.EntityExportData;
import org.winstarcloud.server.dao.device.DeviceProfileService;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.sync.vc.data.EntitiesImportCtx;

@Service
@TbCoreComponent
@RequiredArgsConstructor
public class DeviceProfileImportService extends BaseEntityImportService<DeviceProfileId, DeviceProfile, EntityExportData<DeviceProfile>> {

    private final DeviceProfileService deviceProfileService;

    @Override
    protected void setOwner(TenantId tenantId, DeviceProfile deviceProfile, IdProvider idProvider) {
        deviceProfile.setTenantId(tenantId);
    }

    @Override
    protected DeviceProfile prepare(EntitiesImportCtx ctx, DeviceProfile deviceProfile, DeviceProfile old, EntityExportData<DeviceProfile> exportData, IdProvider idProvider) {
        deviceProfile.setDefaultRuleChainId(idProvider.getInternalId(deviceProfile.getDefaultRuleChainId()));
        deviceProfile.setDefaultEdgeRuleChainId(idProvider.getInternalId(deviceProfile.getDefaultEdgeRuleChainId()));
        deviceProfile.setDefaultDashboardId(idProvider.getInternalId(deviceProfile.getDefaultDashboardId()));
        deviceProfile.setFirmwareId(getOldEntityField(old, DeviceProfile::getFirmwareId));
        deviceProfile.setSoftwareId(getOldEntityField(old, DeviceProfile::getSoftwareId));
        return deviceProfile;
    }

    @Override
    protected DeviceProfile saveOrUpdate(EntitiesImportCtx ctx, DeviceProfile deviceProfile, EntityExportData<DeviceProfile> exportData, IdProvider idProvider) {
        return deviceProfileService.saveDeviceProfile(deviceProfile);
    }

    @Override
    protected void onEntitySaved(User user, DeviceProfile savedDeviceProfile, DeviceProfile oldDeviceProfile) {
        logEntityActionService.logEntityAction(savedDeviceProfile.getTenantId(), savedDeviceProfile.getId(), savedDeviceProfile,
                null, oldDeviceProfile == null ? ActionType.ADDED : ActionType.UPDATED, user);
    }

    @Override
    protected DeviceProfile deepCopy(DeviceProfile deviceProfile) {
        return new DeviceProfile(deviceProfile);
    }

    @Override
    protected void cleanupForComparison(DeviceProfile deviceProfile) {
        super.cleanupForComparison(deviceProfile);
        deviceProfile.setFirmwareId(null);
        deviceProfile.setSoftwareId(null);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DEVICE_PROFILE;
    }

}
