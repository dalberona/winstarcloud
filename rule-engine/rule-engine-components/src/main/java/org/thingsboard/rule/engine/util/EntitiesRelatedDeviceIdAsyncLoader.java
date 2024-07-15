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
package org.winstarcloud.rule.engine.util;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.collections4.CollectionUtils;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.data.DeviceRelationsQuery;
import org.winstarcloud.server.common.data.device.DeviceSearchQuery;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.relation.RelationsSearchParameters;

public class EntitiesRelatedDeviceIdAsyncLoader {

    public static ListenableFuture<DeviceId> findDeviceAsync(
            TbContext ctx,
            EntityId originator,
            DeviceRelationsQuery deviceRelationsQuery
    ) {
        var deviceService = ctx.getDeviceService();
        var query = buildQuery(originator, deviceRelationsQuery);
        var devicesListFuture = deviceService.findDevicesByQuery(ctx.getTenantId(), query);
        return Futures.transformAsync(devicesListFuture,
                deviceList -> CollectionUtils.isNotEmpty(deviceList) ?
                        Futures.immediateFuture(deviceList.get(0).getId())
                        : Futures.immediateFuture(null), ctx.getDbCallbackExecutor());
    }

    private static DeviceSearchQuery buildQuery(EntityId originator, DeviceRelationsQuery deviceRelationsQuery) {
        var query = new DeviceSearchQuery();
        var parameters = new RelationsSearchParameters(
                originator,
                deviceRelationsQuery.getDirection(),
                deviceRelationsQuery.getMaxLevel(),
                deviceRelationsQuery.isFetchLastLevelOnly()
        );
        query.setParameters(parameters);
        query.setRelationType(deviceRelationsQuery.getRelationType());
        query.setDeviceTypes(deviceRelationsQuery.getDeviceTypes());
        return query;
    }

}
