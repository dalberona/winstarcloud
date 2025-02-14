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
package org.winstarcloud.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.DashboardInfo;
import org.winstarcloud.server.common.data.SystemParams;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.mobile.MobileAppSettings;
import org.winstarcloud.server.common.data.mobile.QRCodeConfig;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.data.settings.UserSettings;
import org.winstarcloud.server.common.data.settings.UserSettingsType;
import org.winstarcloud.server.common.data.tenant.profile.DefaultTenantProfileConfiguration;
import org.winstarcloud.server.dao.mobile.MobileAppSettingsService;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.security.model.SecurityUser;
import org.winstarcloud.server.service.security.model.UserPrincipal;
import org.winstarcloud.server.service.sync.vc.EntitiesVersionControlService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Hidden
@RestController
@TbCoreComponent
@RequestMapping("/api")
@Slf4j
public class SystemInfoController extends BaseController {

    @Value("${security.user_token_access_enabled}")
    private boolean userTokenAccessEnabled;

    @Value("${tbel.enabled:true}")
    private boolean tbelEnabled;

    @Value("${state.persistToTelemetry:false}")
    private boolean persistToTelemetry;

    @Value("${ui.dashboard.max_datapoints_limit}")
    private long maxDatapointsLimit;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Autowired
    private EntitiesVersionControlService versionControlService;

    @Autowired
    private MobileAppSettingsService mobileAppSettingsService;

    @PostConstruct
    public void init() {
        JsonNode info = buildInfoObject();
        log.info("System build info: {}", info);
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/system/info", method = RequestMethod.GET)
    @ResponseBody
    public JsonNode getSystemVersionInfo() {
        return buildInfoObject();
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/system/params", method = RequestMethod.GET)
    @ResponseBody
    public SystemParams getSystemParams() throws WinstarcloudException {
        SystemParams systemParams = new SystemParams();
        SecurityUser currentUser = getCurrentUser();
        TenantId tenantId = currentUser.getTenantId();
        CustomerId customerId = currentUser.getCustomerId();
        if (currentUser.isSystemAdmin() || currentUser.isTenantAdmin()) {
            systemParams.setUserTokenAccessEnabled(userTokenAccessEnabled);
        } else {
            systemParams.setUserTokenAccessEnabled(false);
        }
        boolean forceFullscreen = isForceFullscreen(currentUser);
        if (forceFullscreen && (currentUser.isTenantAdmin() || currentUser.isCustomerUser())) {
            PageLink pageLink = new PageLink(100);
            List<DashboardInfo> dashboards;
            if (currentUser.isTenantAdmin()) {
                dashboards = dashboardService.findDashboardsByTenantId(tenantId, pageLink).getData();
            } else {
                dashboards = dashboardService.findDashboardsByTenantIdAndCustomerId(tenantId, customerId, pageLink).getData();
            }
            systemParams.setAllowedDashboardIds(dashboards.stream().map(d -> d.getId().getId().toString()).collect(Collectors.toList()));
        } else {
            systemParams.setAllowedDashboardIds(Collections.emptyList());
        }
        systemParams.setEdgesSupportEnabled(edgesEnabled);
        if (currentUser.isTenantAdmin()) {
            systemParams.setHasRepository(versionControlService.getVersionControlSettings(tenantId) != null);
            systemParams.setTbelEnabled(tbelEnabled);
        } else {
            systemParams.setHasRepository(false);
            systemParams.setTbelEnabled(false);
        }
        if (currentUser.isTenantAdmin() || currentUser.isCustomerUser()) {
            systemParams.setPersistDeviceStateToTelemetry(persistToTelemetry);
        } else {
            systemParams.setPersistDeviceStateToTelemetry(false);
        }
        UserSettings userSettings = userSettingsService.findUserSettings(currentUser.getTenantId(), currentUser.getId(), UserSettingsType.GENERAL);
        ObjectNode userSettingsNode = userSettings == null ? JacksonUtil.newObjectNode() : (ObjectNode) userSettings.getSettings();
        if (!userSettingsNode.has("openedMenuSections")) {
            userSettingsNode.set("openedMenuSections", JacksonUtil.newArrayNode());
        }
        systemParams.setUserSettings(userSettingsNode);
        systemParams.setMaxDatapointsLimit(maxDatapointsLimit);
        if (!currentUser.isSystemAdmin()) {
            DefaultTenantProfileConfiguration tenantProfileConfiguration = tenantProfileCache.get(tenantId).getDefaultProfileConfiguration();
            systemParams.setMaxResourceSize(tenantProfileConfiguration.getMaxResourceSize());
        }
        systemParams.setMobileQrEnabled(Optional.ofNullable(mobileAppSettingsService.getMobileAppSettings(TenantId.SYS_TENANT_ID))
                .map(MobileAppSettings::getQrCodeConfig).map(QRCodeConfig::isShowOnHomePage)
                .orElse(false));
        return systemParams;
    }

    private boolean isForceFullscreen(SecurityUser currentUser) {
        return UserPrincipal.Type.PUBLIC_ID.equals(currentUser.getUserPrincipal().getType()) ||
                (currentUser.getAdditionalInfo() != null &&
                        currentUser.getAdditionalInfo().has("defaultDashboardFullscreen") &&
                        currentUser.getAdditionalInfo().get("defaultDashboardFullscreen").booleanValue());
    }

    private JsonNode buildInfoObject() {
        ObjectNode infoObject = JacksonUtil.newObjectNode();
        if (buildProperties != null) {
            infoObject.put("version", buildProperties.getVersion());
            infoObject.put("artifact", buildProperties.getArtifact());
            infoObject.put("name", buildProperties.getName());
        } else {
            infoObject.put("version", "unknown");
        }
        return infoObject;
    }
}
