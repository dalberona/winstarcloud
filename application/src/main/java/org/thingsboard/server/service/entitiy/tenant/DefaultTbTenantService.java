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
package org.winstarcloud.server.service.entitiy.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.winstarcloud.server.common.data.Tenant;
import org.winstarcloud.server.common.data.TenantProfile;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.dao.tenant.TbTenantProfileCache;
import org.winstarcloud.server.dao.tenant.TenantProfileService;
import org.winstarcloud.server.dao.tenant.TenantService;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.entitiy.AbstractTbEntityService;
import org.winstarcloud.server.service.entitiy.queue.TbQueueService;
import org.winstarcloud.server.service.install.InstallScripts;
import org.winstarcloud.server.service.sync.vc.EntitiesVersionControlService;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@TbCoreComponent
@RequiredArgsConstructor
public class DefaultTbTenantService extends AbstractTbEntityService implements TbTenantService {

    private final TenantService tenantService;
    private final TbTenantProfileCache tenantProfileCache;
    private final InstallScripts installScripts;
    private final TbQueueService tbQueueService;
    private final TenantProfileService tenantProfileService;
    private final EntitiesVersionControlService versionControlService;

    @Override
    public Tenant save(Tenant tenant) throws Exception {
        boolean created = tenant.getId() == null;
        Tenant oldTenant = !created ? tenantService.findTenantById(tenant.getId()) : null;

        Tenant savedTenant = tenantService.saveTenant(tenant, tenantId -> {
            installScripts.createDefaultRuleChains(tenantId);
            installScripts.createDefaultEdgeRuleChains(tenantId);
            if (!isTestProfile()) {
                installScripts.createDefaultTenantDashboards(tenantId, null);
            }
        });
        tenantProfileCache.evict(savedTenant.getId());

        TenantProfile oldTenantProfile = oldTenant != null ? tenantProfileService.findTenantProfileById(TenantId.SYS_TENANT_ID, oldTenant.getTenantProfileId()) : null;
        TenantProfile newTenantProfile = tenantProfileService.findTenantProfileById(TenantId.SYS_TENANT_ID, savedTenant.getTenantProfileId());
        tbQueueService.updateQueuesByTenants(Collections.singletonList(savedTenant.getTenantId()), newTenantProfile, oldTenantProfile);
        return savedTenant;
    }

    @Override
    public void delete(Tenant tenant) throws Exception {
        TenantId tenantId = tenant.getId();
        tenantService.deleteTenant(tenantId);
        tenantProfileCache.evict(tenantId);
        versionControlService.deleteVersionControlSettings(tenantId).get(1, TimeUnit.MINUTES);
    }
}
