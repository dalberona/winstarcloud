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
package org.winstarcloud.server.dao.timeseries;

import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.kv.DeleteTsKvQuery;
import org.winstarcloud.server.common.data.kv.ReadTsKvQuery;
import org.winstarcloud.server.common.data.kv.ReadTsKvQueryResult;
import org.winstarcloud.server.common.data.kv.TsKvEntry;
import org.winstarcloud.server.common.data.kv.TsKvLatestRemovingResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Andrew Shvayka
 */
public interface TimeseriesService {

    ListenableFuture<List<ReadTsKvQueryResult>> findAllByQueries(TenantId tenantId, EntityId entityId, List<ReadTsKvQuery> queries);

    ListenableFuture<List<TsKvEntry>> findAll(TenantId tenantId, EntityId entityId, List<ReadTsKvQuery> queries);

    ListenableFuture<Optional<TsKvEntry>> findLatest(TenantId tenantId, EntityId entityId, String key);

    ListenableFuture<List<TsKvEntry>> findLatest(TenantId tenantId, EntityId entityId, Collection<String> keys);

    ListenableFuture<List<TsKvEntry>> findAllLatest(TenantId tenantId, EntityId entityId);

    ListenableFuture<Integer> save(TenantId tenantId, EntityId entityId, TsKvEntry tsKvEntry);

    ListenableFuture<Integer> save(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntry, long ttl);

    ListenableFuture<Integer> saveWithoutLatest(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntry, long ttl);

    ListenableFuture<List<Void>> saveLatest(TenantId tenantId, EntityId entityId, List<TsKvEntry> tsKvEntry);

    ListenableFuture<List<TsKvLatestRemovingResult>> remove(TenantId tenantId, EntityId entityId, List<DeleteTsKvQuery> queries);

    ListenableFuture<List<TsKvLatestRemovingResult>> removeLatest(TenantId tenantId, EntityId entityId, Collection<String> keys);

    ListenableFuture<Collection<String>> removeAllLatest(TenantId tenantId, EntityId entityId);

    List<String> findAllKeysByDeviceProfileId(TenantId tenantId, DeviceProfileId deviceProfileId);

    List<String> findAllKeysByEntityIds(TenantId tenantId, List<EntityId> entityIds);

    void cleanup(long systemTtl);
}
