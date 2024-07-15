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
package org.winstarcloud.server.service.entitiy.entityview;

import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.EntityView;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.EntityViewId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.msg.plugin.ComponentLifecycleListener;

import java.util.List;

public interface TbEntityViewService extends ComponentLifecycleListener {

    EntityView save(EntityView entityView, EntityView existingEntityView, User user) throws Exception;

    void updateEntityViewAttributes(TenantId tenantId, EntityView savedEntityView, EntityView oldEntityView, User user) throws WinstarcloudException;

    void delete(EntityView entity, User user) throws WinstarcloudException;

    EntityView assignEntityViewToCustomer(TenantId tenantId, EntityViewId entityViewId, Customer customer, User user) throws WinstarcloudException;

    EntityView assignEntityViewToPublicCustomer(TenantId tenantId, EntityViewId entityViewId, User user) throws WinstarcloudException;

    EntityView assignEntityViewToEdge(TenantId tenantId, CustomerId customerId, EntityViewId entityViewId, Edge edge, User user) throws WinstarcloudException;

    EntityView unassignEntityViewFromEdge(TenantId tenantId, CustomerId customerId, EntityView entityView, Edge edge, User user) throws WinstarcloudException;

    EntityView unassignEntityViewFromCustomer(TenantId tenantId, EntityViewId entityViewId, Customer customer, User user) throws WinstarcloudException;

    ListenableFuture<List<EntityView>> findEntityViewsByTenantIdAndEntityIdAsync(TenantId tenantId, EntityId entityId);
}
