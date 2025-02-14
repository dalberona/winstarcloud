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
package org.winstarcloud.server.dao.resource;

import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.ResourceType;
import org.winstarcloud.server.common.data.TbResource;
import org.winstarcloud.server.common.data.TbResourceInfo;
import org.winstarcloud.server.common.data.TbResourceInfoFilter;
import org.winstarcloud.server.common.data.id.TbResourceId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.entity.EntityDaoService;

import java.util.List;

public interface ResourceService extends EntityDaoService {

    TbResource saveResource(TbResource resource);

    TbResource saveResource(TbResource resource, boolean doValidate);

    TbResource findResourceByTenantIdAndKey(TenantId tenantId, ResourceType resourceType, String resourceKey);

    TbResource findResourceById(TenantId tenantId, TbResourceId resourceId);

    TbResourceInfo findResourceInfoById(TenantId tenantId, TbResourceId resourceId);

    TbResourceInfo findResourceInfoByTenantIdAndKey(TenantId tenantId, ResourceType resourceType, String resourceKey);

    PageData<TbResource> findAllTenantResources(TenantId tenantId, PageLink pageLink);

    ListenableFuture<TbResourceInfo> findResourceInfoByIdAsync(TenantId tenantId, TbResourceId resourceId);

    PageData<TbResourceInfo> findAllTenantResourcesByTenantId(TbResourceInfoFilter filter, PageLink pageLink);

    PageData<TbResourceInfo> findTenantResourcesByTenantId(TbResourceInfoFilter filter, PageLink pageLink);

    List<TbResource> findTenantResourcesByResourceTypeAndObjectIds(TenantId tenantId, ResourceType lwm2mModel, String[] objectIds);

    PageData<TbResource> findTenantResourcesByResourceTypeAndPageLink(TenantId tenantId, ResourceType lwm2mModel, PageLink pageLink);

    void deleteResource(TenantId tenantId, TbResourceId resourceId);

    void deleteResource(TenantId tenantId, TbResourceId resourceId, boolean force);

    void deleteResourcesByTenantId(TenantId tenantId);

    long sumDataSizeByTenantId(TenantId tenantId);

}
