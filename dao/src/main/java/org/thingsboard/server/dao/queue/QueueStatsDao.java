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
package org.winstarcloud.server.dao.queue;

import org.winstarcloud.server.common.data.id.QueueStatsId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.data.queue.QueueStats;
import org.winstarcloud.server.dao.Dao;

import java.util.List;

public interface QueueStatsDao extends Dao<QueueStats> {

    QueueStats findByTenantIdQueueNameAndServiceId(TenantId tenantId, String queueName, String serviceId);

    PageData<QueueStats> findByTenantId(TenantId tenantId, PageLink pageLink);

    void deleteByTenantId(TenantId tenantId);

    List<QueueStats> findByIds(TenantId tenantId, List<QueueStatsId> queueStatsIds);

}