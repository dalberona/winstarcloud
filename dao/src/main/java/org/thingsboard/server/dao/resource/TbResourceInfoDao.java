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

import org.winstarcloud.server.common.data.ResourceType;
import org.winstarcloud.server.common.data.TbResourceInfo;
import org.winstarcloud.server.common.data.TbResourceInfoFilter;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.Dao;

import java.util.List;
import java.util.Set;

public interface TbResourceInfoDao extends Dao<TbResourceInfo> {

    PageData<TbResourceInfo> findAllTenantResourcesByTenantId(TbResourceInfoFilter filter, PageLink pageLink);

    PageData<TbResourceInfo> findTenantResourcesByTenantId(TbResourceInfoFilter filter, PageLink pageLink);

    TbResourceInfo findByTenantIdAndKey(TenantId tenantId, ResourceType resourceType, String resourceKey);

    boolean existsByTenantIdAndResourceTypeAndResourceKey(TenantId tenantId, ResourceType resourceType, String resourceKey);

    Set<String> findKeysByTenantIdAndResourceTypeAndResourceKeyPrefix(TenantId tenantId, ResourceType resourceType, String prefix);

    List<TbResourceInfo> findByTenantIdAndEtagAndKeyStartingWith(TenantId tenantId, String etag, String query);

    TbResourceInfo findSystemOrTenantImageByEtag(TenantId tenantId, ResourceType resourceType, String etag);

    boolean existsByPublicResourceKey(ResourceType resourceType, String publicResourceKey);

    TbResourceInfo findPublicResourceByKey(ResourceType resourceType, String publicResourceKey);

}
