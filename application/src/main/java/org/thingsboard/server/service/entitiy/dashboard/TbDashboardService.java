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
package org.winstarcloud.server.service.entitiy.dashboard;

import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.Dashboard;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.DashboardId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.service.entitiy.SimpleTbEntityService;

import java.util.Set;

public interface TbDashboardService extends SimpleTbEntityService<Dashboard> {

    Dashboard assignDashboardToCustomer(Dashboard dashboard, Customer customer, User user) throws WinstarcloudException;

    Dashboard assignDashboardToPublicCustomer(Dashboard dashboard, User user) throws WinstarcloudException;

    Dashboard unassignDashboardFromPublicCustomer(Dashboard dashboard, User user) throws WinstarcloudException;

    Dashboard updateDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws WinstarcloudException;

    Dashboard addDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws WinstarcloudException;

    Dashboard removeDashboardCustomers(Dashboard dashboard, Set<CustomerId> customerIds, User user) throws WinstarcloudException;

    Dashboard asignDashboardToEdge(TenantId tenantId, DashboardId dashboardId, Edge edge, User user) throws WinstarcloudException;

    Dashboard unassignDashboardFromEdge(Dashboard dashboard, Edge edge, User user) throws WinstarcloudException;

    Dashboard unassignDashboardFromCustomer(Dashboard dashboard, Customer customer, User user) throws WinstarcloudException;

}
