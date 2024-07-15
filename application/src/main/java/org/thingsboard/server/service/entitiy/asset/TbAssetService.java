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
package org.winstarcloud.server.service.entitiy.asset;

import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.asset.Asset;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.AssetId;
import org.winstarcloud.server.common.data.id.TenantId;

public interface TbAssetService {

    Asset save(Asset asset, User user) throws Exception;

    void delete(Asset asset, User user);

    Asset assignAssetToCustomer(TenantId tenantId, AssetId assetId, Customer customer, User user) throws WinstarcloudException;

    Asset unassignAssetToCustomer(TenantId tenantId, AssetId assetId, Customer customer, User user) throws WinstarcloudException;

    Asset assignAssetToPublicCustomer(TenantId tenantId, AssetId assetId, User user) throws WinstarcloudException;

    Asset assignAssetToEdge(TenantId tenantId, AssetId assetId, Edge edge, User user) throws WinstarcloudException;

    Asset unassignAssetFromEdge(TenantId tenantId, Asset asset, Edge edge, User user) throws WinstarcloudException;

}
