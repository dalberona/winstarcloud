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
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.Dashboard;
import org.winstarcloud.server.common.data.ShortCustomerInfo;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.id.DashboardId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.dao.model.BaseSqlEntity;
import org.winstarcloud.server.dao.model.ModelConstants;
import org.winstarcloud.server.dao.util.mapping.JsonConverter;

import java.util.HashSet;
import java.util.UUID;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = ModelConstants.DASHBOARD_TABLE_NAME)
public final class DashboardEntity extends BaseSqlEntity<Dashboard> {

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

    @Convert(converter = JsonConverter.class)
    @Column(name = ModelConstants.DASHBOARD_CONFIGURATION_PROPERTY)
    private JsonNode configuration;

    @Column(name = ModelConstants.EXTERNAL_ID_PROPERTY)
    private UUID externalId;

    public DashboardEntity() {
        super();
    }

    public DashboardEntity(Dashboard dashboard) {
        if (dashboard.getId() != null) {
            this.setUuid(dashboard.getId().getId());
        }
        this.setCreatedTime(dashboard.getCreatedTime());
        if (dashboard.getTenantId() != null) {
            this.tenantId = dashboard.getTenantId().getId();
        }
        this.title = dashboard.getTitle();
        this.image = dashboard.getImage();
        if (dashboard.getAssignedCustomers() != null) {
            try {
                this.assignedCustomers = JacksonUtil.toString(dashboard.getAssignedCustomers());
            } catch (IllegalArgumentException e) {
                log.error("Unable to serialize assigned customers to string!", e);
            }
        }
        this.mobileHide = dashboard.isMobileHide();
        this.mobileOrder = dashboard.getMobileOrder();
        this.configuration = dashboard.getConfiguration();
        if (dashboard.getExternalId() != null) {
            this.externalId = dashboard.getExternalId().getId();
        }
    }

    @Override
    public Dashboard toData() {
        Dashboard dashboard = new Dashboard(new DashboardId(this.getUuid()));
        dashboard.setCreatedTime(this.getCreatedTime());
        if (tenantId != null) {
            dashboard.setTenantId(TenantId.fromUUID(tenantId));
        }
        dashboard.setTitle(title);
        dashboard.setImage(image);
        if (!StringUtils.isEmpty(assignedCustomers)) {
            try {
                dashboard.setAssignedCustomers(JacksonUtil.fromString(assignedCustomers, assignedCustomersType));
            } catch (IllegalArgumentException e) {
                log.warn("Unable to parse assigned customers!", e);
            }
        }
        dashboard.setMobileHide(mobileHide);
        dashboard.setMobileOrder(mobileOrder);
        dashboard.setConfiguration(configuration);
        if (externalId != null) {
            dashboard.setExternalId(new DashboardId(externalId));
        }
        return dashboard;
    }
}
