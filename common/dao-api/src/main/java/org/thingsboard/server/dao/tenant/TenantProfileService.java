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
package org.winstarcloud.server.dao.tenant;

import org.winstarcloud.server.common.data.EntityInfo;
import org.winstarcloud.server.common.data.TenantProfile;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.TenantProfileId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.entity.EntityDaoService;

import java.util.List;
import java.util.UUID;

public interface TenantProfileService extends EntityDaoService {

    TenantProfile findTenantProfileById(TenantId tenantId, TenantProfileId tenantProfileId);

    EntityInfo findTenantProfileInfoById(TenantId tenantId, TenantProfileId tenantProfileId);

    TenantProfile saveTenantProfile(TenantId tenantId, TenantProfile tenantProfile);

    void deleteTenantProfile(TenantId tenantId, TenantProfileId tenantProfileId);

    PageData<TenantProfile> findTenantProfiles(TenantId tenantId, PageLink pageLink);

    PageData<EntityInfo> findTenantProfileInfos(TenantId tenantId, PageLink pageLink);

    TenantProfile findOrCreateDefaultTenantProfile(TenantId tenantId);

    TenantProfile findDefaultTenantProfile(TenantId tenantId);

    EntityInfo findDefaultTenantProfileInfo(TenantId tenantId);

    boolean setDefaultTenantProfile(TenantId tenantId, TenantProfileId tenantProfileId);

    void deleteTenantProfiles(TenantId tenantId);

    List<TenantProfile> findTenantProfilesByIds(TenantId tenantId, UUID[] ids);

}
