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
package org.winstarcloud.server.service.apiusage;

import org.springframework.context.ApplicationListener;
import org.winstarcloud.rule.engine.api.RuleEngineApiUsageStateService;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.TenantProfileId;
import org.winstarcloud.server.common.msg.queue.TbCallback;
import org.winstarcloud.server.common.stats.TbApiUsageStateClient;
import org.winstarcloud.server.gen.transport.TransportProtos.ToUsageStatsServiceMsg;
import org.winstarcloud.server.queue.common.TbProtoQueueMsg;
import org.winstarcloud.server.queue.discovery.event.PartitionChangeEvent;

public interface TbApiUsageStateService extends TbApiUsageStateClient, RuleEngineApiUsageStateService, ApplicationListener<PartitionChangeEvent> {

    void process(TbProtoQueueMsg<ToUsageStatsServiceMsg> msg, TbCallback callback);

    void onTenantProfileUpdate(TenantProfileId tenantProfileId);

    void onTenantUpdate(TenantId tenantId);

    void onTenantDelete(TenantId tenantId);

    void onCustomerDelete(CustomerId customerId);

    void onApiUsageStateUpdate(TenantId tenantId);
}
