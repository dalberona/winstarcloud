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
package org.winstarcloud.server.dao.service;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.DeviceProfileType;
import org.winstarcloud.server.common.data.DeviceTransportType;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.OtaPackage;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.Tenant;
import org.winstarcloud.server.common.data.asset.AssetProfile;
import org.winstarcloud.server.common.data.device.profile.DefaultDeviceProfileConfiguration;
import org.winstarcloud.server.common.data.device.profile.DefaultDeviceProfileTransportConfiguration;
import org.winstarcloud.server.common.data.device.profile.DeviceProfileData;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.event.RuleNodeDebugEvent;
import org.winstarcloud.server.common.data.housekeeper.TenantEntitiesDeletionHousekeeperTask;
import org.winstarcloud.server.common.data.housekeeper.HousekeeperTaskType;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.HasId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.ota.ChecksumAlgorithm;
import org.winstarcloud.server.common.data.ota.OtaPackageType;
import org.winstarcloud.server.dao.audit.AuditLogLevelFilter;
import org.winstarcloud.server.dao.audit.AuditLogLevelMask;
import org.winstarcloud.server.dao.audit.AuditLogLevelProperties;
import org.winstarcloud.server.dao.entity.EntityDaoService;
import org.winstarcloud.server.dao.entity.EntityServiceRegistry;
import org.winstarcloud.server.common.msg.housekeeper.HousekeeperClient;
import org.winstarcloud.server.dao.tenant.TenantService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AbstractServiceTest.class, loader = AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Configuration
@ComponentScan("org.winstarcloud.server")
public abstract class AbstractServiceTest {

    public static final TenantId SYSTEM_TENANT_ID = TenantId.SYS_TENANT_ID;

    @Autowired
    protected TenantService tenantService;

    @Autowired
    protected EntityServiceRegistry entityServiceRegistry;

    protected TenantId tenantId;

    @Before
    public void beforeAbstractService() {
        tenantId = createTenant().getId();
    }

    @After
    public void afterAbstractService() {
        tenantService.deleteTenants();
    }

    public class IdComparator<D extends HasId> implements Comparator<D> {
        @Override
        public int compare(D o1, D o2) {
            return o1.getId().getId().compareTo(o2.getId().getId());
        }
    }


    protected RuleNodeDebugEvent generateEvent(TenantId tenantId, EntityId entityId) throws IOException {
        return generateEvent(tenantId, entityId, null);
    }

    protected RuleNodeDebugEvent generateEvent(TenantId tenantId, EntityId entityId, String eventType) throws IOException {
        if (tenantId == null) {
            tenantId = TenantId.fromUUID(Uuids.timeBased());
        }
        return RuleNodeDebugEvent.builder()
                .tenantId(tenantId)
                .entityId(entityId.getId())
                .serviceId("server A")
                .eventType(eventType)
                .data(JacksonUtil.toString(readFromResource("TestJsonData.json")))
                .build();
    }

    public JsonNode readFromResource(String resourceName) throws IOException {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourceName)){
            return JacksonUtil.fromBytes(Objects.requireNonNull(is).readAllBytes());
        }
    }

    @Bean
    public AuditLogLevelFilter auditLogLevelFilter() {
        Map<String, String> mask = new HashMap<>();
        for (EntityType entityType : EntityType.values()) {
            mask.put(entityType.name().toLowerCase(), AuditLogLevelMask.RW.name());
        }
        var props = new AuditLogLevelProperties();
        props.setMask(mask);
        return new AuditLogLevelFilter(props);
    }

    @Bean
    public HousekeeperClient housekeeperClient() {
        return task -> {
            if (task.getTaskType() == HousekeeperTaskType.DELETE_TENANT_ENTITIES) {
                EntityDaoService entityService = entityServiceRegistry.getServiceByEntityType(((TenantEntitiesDeletionHousekeeperTask) task).getEntityType());
                entityService.deleteByTenantId(task.getTenantId());
            }
        };
    }

    protected DeviceProfile createDeviceProfile(TenantId tenantId, String name) {
        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setTenantId(tenantId);
        deviceProfile.setName(name);
        deviceProfile.setType(DeviceProfileType.DEFAULT);
        deviceProfile.setTransportType(DeviceTransportType.DEFAULT);
        deviceProfile.setDescription(name + " Test");
        DeviceProfileData deviceProfileData = new DeviceProfileData();
        DefaultDeviceProfileConfiguration configuration = new DefaultDeviceProfileConfiguration();
        DefaultDeviceProfileTransportConfiguration transportConfiguration = new DefaultDeviceProfileTransportConfiguration();
        deviceProfileData.setConfiguration(configuration);
        deviceProfileData.setTransportConfiguration(transportConfiguration);
        deviceProfile.setProfileData(deviceProfileData);
        deviceProfile.setDefault(false);
        deviceProfile.setDefaultRuleChainId(null);
        return deviceProfile;
    }

    protected AssetProfile createAssetProfile(TenantId tenantId, String name) {
        AssetProfile assetProfile = new AssetProfile();
        assetProfile.setTenantId(tenantId);
        assetProfile.setName(name);
        assetProfile.setDescription(name + " Test");
        assetProfile.setDefault(false);
        assetProfile.setDefaultRuleChainId(null);
        return assetProfile;
    }

    public Tenant createTenant() {
        Tenant tenant = new Tenant();
        tenant.setTitle("My tenant " + UUID.randomUUID());
        Tenant savedTenant = tenantService.saveTenant(tenant);
        assertNotNull(savedTenant);
        return savedTenant;
    }

    protected Edge constructEdge(TenantId tenantId, String name, String type) {
        Edge edge = new Edge();
        edge.setTenantId(tenantId);
        edge.setName(name);
        edge.setType(type);
        edge.setSecret(StringUtils.randomAlphanumeric(20));
        edge.setRoutingKey(StringUtils.randomAlphanumeric(20));
        return edge;
    }

    protected OtaPackage constructDefaultOtaPackage(TenantId tenantId, DeviceProfileId deviceProfileId) {
        OtaPackage firmware = new OtaPackage();
        firmware.setTenantId(tenantId);
        firmware.setDeviceProfileId(deviceProfileId);
        firmware.setType(OtaPackageType.FIRMWARE);
        firmware.setTitle("My firmware");
        firmware.setVersion("3.3.3");
        firmware.setFileName("filename.txt");
        firmware.setContentType("text/plain");
        firmware.setChecksumAlgorithm(ChecksumAlgorithm.SHA256);
        firmware.setChecksum("4bf5122f344554c53bde2ebb8cd2b7e3d1600ad631c385a5d7cce23c7785459a");
        firmware.setData(ByteBuffer.wrap(new byte[]{1}));
        firmware.setDataSize(1L);
        return firmware;
    }

}
