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
package org.winstarcloud.server.dao.model.sql;

import com.fasterxml.jackson.databind.JavaType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.DashboardInfo;
import org.winstarcloud.server.common.data.ShortCustomerInfo;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.id.DashboardId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.dao.model.BaseSqlEntity;
import org.winstarcloud.server.dao.model.ModelConstants;

import java.util.HashSet;
import java.util.UUID;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.DASHBOARD_TABLE_NAME)
public class DashboardInfoEntity extends BaseSqlEntity<DashboardInfo> {

    private static final JavaType assignedCustomersType =
            JacksonUtil.constructCollectionType(HashSet.class, ShortCustomerInfo.class);

    @Column(name = ModelConstants.DASHBOARD_TENANT_ID_PROPERTY)
    private UUID tenantId;

    @Column(name = ModelConstants.DASHBOARD_TITLE_PROPERTY)
    private String title;

    @Column(name = ModelConstants.DASHBOARD_IMAGE_PROPERTY)
    private String image;

    @Column(name = ModelConstants.DASHBOARD_ASSIGNED_CUSTOMERS_PROPERTY)
    private String assignedCustomers;

    @Column(name = ModelConstants.DASHBOARD_MOBILE_HIDE_PROPERTY)
    private boolean mobileHide;

    @Column(name = ModelConstants.DASHBOARD_MOBILE_ORDER_PROPERTY)
    private Integer mobileOrder;

    public DashboardInfoEntity() {
        super();
    }

    public DashboardInfoEntity(DashboardInfo dashboardInfo) {
        if (dashboardInfo.getId() != null) {
            this.setUuid(dashboardInfo.getId().getId());
        }
        this.setCreatedTime(dashboardInfo.getCreatedTime());
        if (dashboardInfo.getTenantId() != null) {
            this.tenantId = dashboardInfo.getTenantId().getId();
        }
        this.title = dashboardInfo.getTitle();
        this.image = dashboardInfo.getImage();
        if (dashboardInfo.getAssignedCustomers() != null) {
            try {
                this.assignedCustomers = JacksonUtil.toString(dashboardInfo.getAssignedCustomers());
            } catch (IllegalArgumentException e) {
                log.error("Unable to serialize assigned customers to string!", e);
            }
        }
        this.mobileHide = dashboardInfo.isMobileHide();
        this.mobileOrder = dashboardInfo.getMobileOrder();
    }

    @Override
    public DashboardInfo toData() {
        DashboardInfo dashboardInfo = new DashboardInfo(new DashboardId(this.getUuid()));
        dashboardInfo.setCreatedTime(createdTime);
        if (tenantId != null) {
            dashboardInfo.setTenantId(TenantId.fromUUID(tenantId));
        }
        dashboardInfo.setTitle(title);
        dashboardInfo.setImage(image);
        if (!StringUtils.isEmpty(assignedCustomers)) {
            try {
                dashboardInfo.setAssignedCustomers(JacksonUtil.fromString(assignedCustomers, assignedCustomersType));
            } catch (IllegalArgumentException e) {
                log.warn("Unable to parse assigned customers!", e);
            }
        }
        dashboardInfo.setMobileHide(mobileHide);
        dashboardInfo.setMobileOrder(mobileOrder);
        return dashboardInfo;
    }

}
