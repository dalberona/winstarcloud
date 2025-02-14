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
package org.winstarcloud.server.service.edge.rpc.fetch;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.EdgeUtils;
import org.winstarcloud.server.common.data.asset.AssetProfile;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.edge.EdgeEvent;
import org.winstarcloud.server.common.data.edge.EdgeEventActionType;
import org.winstarcloud.server.common.data.edge.EdgeEventType;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.dao.asset.AssetProfileService;
import org.winstarcloud.server.dao.device.DeviceProfileService;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class DefaultProfilesEdgeEventFetcher implements EdgeEventFetcher {

    private final DeviceProfileService deviceProfileService;
    private final AssetProfileService assetProfileService;

    @Override
    public PageLink getPageLink(int pageSize) {
        return null;
    }

    @Override
    public PageData<EdgeEvent> fetchEdgeEvents(TenantId tenantId, Edge edge, PageLink pageLink) {
        List<EdgeEvent> result = new ArrayList<>();

        DeviceProfile deviceProfile = deviceProfileService.findDefaultDeviceProfile(tenantId);
        result.add(EdgeUtils.constructEdgeEvent(tenantId, edge.getId(), EdgeEventType.DEVICE_PROFILE,
                EdgeEventActionType.ADDED, deviceProfile.getId(), null));

        AssetProfile assetProfile = assetProfileService.findDefaultAssetProfile(tenantId);
        result.add(EdgeUtils.constructEdgeEvent(tenantId, edge.getId(), EdgeEventType.ASSET_PROFILE,
                EdgeEventActionType.ADDED, assetProfile.getId(), null));

        return new PageData<>(result, 1, result.size(), false);
    }

}
