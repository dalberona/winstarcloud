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
package org.winstarcloud.rule.engine.api;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.AttributeScope;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.kv.AttributeKvEntry;
import org.winstarcloud.server.common.data.kv.DeleteTsKvQuery;
import org.winstarcloud.server.common.data.kv.TsKvEntry;

import java.util.Collection;
import java.util.List;

/**
 * Created by ashvayka on 02.04.18.
 */
public interface RuleEngineTelemetryService {

    ListenableFuture<Void> saveAndNotify(TenantId tenantId, EntityId entityId, TsKvEntry ts);

    void saveAndNotify(TenantId tenantId, EntityId entityId, List<TsKvEntry> ts, FutureCallback<Void> callback);

    void saveAndNotify(TenantId tenantId, CustomerId id, EntityId entityId, List<TsKvEntry> ts, long ttl, FutureCallback<Void> callback);

    void saveWithoutLatestAndNotify(TenantId tenantId, CustomerId id, EntityId entityId, List<TsKvEntry> ts, long ttl, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    void saveAndNotify(TenantId tenantId, EntityId entityId, String scope, List<AttributeKvEntry> attributes, FutureCallback<Void> callback);

    void saveAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, List<AttributeKvEntry> attributes, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    void saveAndNotify(TenantId tenantId, EntityId entityId, String scope, List<AttributeKvEntry> attributes, boolean notifyDevice, FutureCallback<Void> callback);

    void saveAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, List<AttributeKvEntry> attributes, boolean notifyDevice, FutureCallback<Void> callback);

    void saveLatestAndNotify(TenantId tenantId, EntityId entityId, List<TsKvEntry> ts, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, long value);

    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, long value);

    @Deprecated(since = "3.7.0")
    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, String value);

    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, String value);

    @Deprecated(since = "3.7.0")
    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, double value);

    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, double value);

    @Deprecated(since = "3.7.0")
    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, boolean value);

    ListenableFuture<Void> saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, boolean value);

    @Deprecated(since = "3.7.0")
    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, long value, FutureCallback<Void> callback);

    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, long value, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, String value, FutureCallback<Void> callback);

    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, String value, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, double value, FutureCallback<Void> callback);

    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, double value, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, String scope, String key, boolean value, FutureCallback<Void> callback);

    void saveAttrAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, String key, boolean value, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    void deleteAndNotify(TenantId tenantId, EntityId entityId, String scope, List<String> keys, FutureCallback<Void> callback);

    void deleteAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, List<String> keys, FutureCallback<Void> callback);

    @Deprecated(since = "3.7.0")
    void deleteAndNotify(TenantId tenantId, EntityId entityId, String scope, List<String> keys, boolean notifyDevice, FutureCallback<Void> callback);

    void deleteAndNotify(TenantId tenantId, EntityId entityId, AttributeScope scope, List<String> keys, boolean notifyDevice, FutureCallback<Void> callback);

    void deleteLatest(TenantId tenantId, EntityId entityId, List<String> keys, FutureCallback<Void> callback);

    void deleteAllLatest(TenantId tenantId, EntityId entityId, FutureCallback<Collection<String>> callback);

    void deleteTimeseriesAndNotify(TenantId tenantId, EntityId entityId, List<String> keys, List<DeleteTsKvQuery> deleteTsKvQueries, FutureCallback<Void> callback);
}
