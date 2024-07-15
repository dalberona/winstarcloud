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
package org.winstarcloud.server.service.edge.rpc.processor.dashboard;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.Dashboard;
import org.winstarcloud.server.common.data.ShortCustomerInfo;
import org.winstarcloud.server.common.data.id.DashboardId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.gen.edge.v1.DashboardUpdateMsg;
import org.winstarcloud.server.queue.util.TbCoreComponent;

import java.util.Set;

@Component
@TbCoreComponent
public class DashboardEdgeProcessorV1 extends DashboardEdgeProcessor {

    @Override
    protected Dashboard constructDashboardFromUpdateMsg(TenantId tenantId, DashboardId dashboardId, DashboardUpdateMsg dashboardUpdateMsg) {
        Dashboard dashboard = new Dashboard();
        dashboard.setTenantId(tenantId);
        dashboard.setCreatedTime(Uuids.unixTimestamp(dashboardId.getId()));
        dashboard.setTitle(dashboardUpdateMsg.getTitle());
        dashboard.setImage(dashboardUpdateMsg.hasImage() ? dashboardUpdateMsg.getImage() : null);
        dashboard.setConfiguration(JacksonUtil.toJsonNode(dashboardUpdateMsg.getConfiguration()));

        Set<ShortCustomerInfo> assignedCustomers;
        if (dashboardUpdateMsg.hasAssignedCustomers()) {
            assignedCustomers = JacksonUtil.fromString(dashboardUpdateMsg.getAssignedCustomers(), new TypeReference<>() {});
            assignedCustomers = filterNonExistingCustomers(tenantId, assignedCustomers);
            dashboard.setAssignedCustomers(assignedCustomers);
        }
        dashboard.setMobileOrder(dashboardUpdateMsg.hasMobileOrder() ? dashboardUpdateMsg.getMobileOrder() : null);
        dashboard.setMobileHide(dashboardUpdateMsg.getMobileHide());
        return dashboard;
    }
}
