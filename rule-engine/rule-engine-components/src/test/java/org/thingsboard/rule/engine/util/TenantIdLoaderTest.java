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
package org.winstarcloud.rule.engine.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.winstarcloud.common.util.AbstractListeningExecutor;
import org.winstarcloud.rule.engine.api.RuleEngineAlarmService;
import org.winstarcloud.rule.engine.api.RuleEngineApiUsageStateService;
import org.winstarcloud.rule.engine.api.RuleEngineAssetProfileCache;
import org.winstarcloud.rule.engine.api.RuleEngineDeviceProfileCache;
import org.winstarcloud.rule.engine.api.RuleEngineRpcService;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.server.common.data.ApiUsageState;
import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.Dashboard;
import org.winstarcloud.server.common.data.Device;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.EntityView;
import org.winstarcloud.server.common.data.OtaPackage;
import org.winstarcloud.server.common.data.TbResource;
import org.winstarcloud.server.common.data.TenantProfile;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.alarm.Alarm;
import org.winstarcloud.server.common.data.asset.Asset;
import org.winstarcloud.server.common.data.asset.AssetProfile;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.id.AssetProfileId;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.EntityIdFactory;
import org.winstarcloud.server.common.data.id.NotificationId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.TenantProfileId;
import org.winstarcloud.server.common.data.notification.NotificationRequest;
import org.winstarcloud.server.common.data.notification.rule.NotificationRule;
import org.winstarcloud.server.common.data.notification.targets.NotificationTarget;
import org.winstarcloud.server.common.data.notification.template.NotificationTemplate;
import org.winstarcloud.server.common.data.queue.Queue;
import org.winstarcloud.server.common.data.queue.QueueStats;
import org.winstarcloud.server.common.data.rpc.Rpc;
import org.winstarcloud.server.common.data.rule.RuleChain;
import org.winstarcloud.server.common.data.rule.RuleNode;
import org.winstarcloud.server.common.data.widget.WidgetType;
import org.winstarcloud.server.common.data.widget.WidgetsBundle;
import org.winstarcloud.server.dao.asset.AssetService;
import org.winstarcloud.server.dao.customer.CustomerService;
import org.winstarcloud.server.dao.dashboard.DashboardService;
import org.winstarcloud.server.dao.device.DeviceService;
import org.winstarcloud.server.dao.edge.EdgeService;
import org.winstarcloud.server.dao.entityview.EntityViewService;
import org.winstarcloud.server.dao.notification.NotificationRequestService;
import org.winstarcloud.server.dao.notification.NotificationRuleService;
import org.winstarcloud.server.dao.notification.NotificationTargetService;
import org.winstarcloud.server.dao.notification.NotificationTemplateService;
import org.winstarcloud.server.dao.ota.OtaPackageService;
import org.winstarcloud.server.dao.queue.QueueService;
import org.winstarcloud.server.dao.queue.QueueStatsService;
import org.winstarcloud.server.dao.resource.ResourceService;
import org.winstarcloud.server.dao.rule.RuleChainService;
import org.winstarcloud.server.dao.user.UserService;
import org.winstarcloud.server.dao.widget.WidgetTypeService;
import org.winstarcloud.server.dao.widget.WidgetsBundleService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TenantIdLoaderTest {

    @Mock
    private TbContext ctx;
    @Mock
    private CustomerService customerService;
    @Mock
    private UserService userService;
    @Mock
    private AssetService assetService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private RuleEngineAlarmService alarmService;
    @Mock
    private RuleChainService ruleChainService;
    @Mock
    private EntityViewService entityViewService;
    @Mock
    private DashboardService dashboardService;
    @Mock
    private EdgeService edgeService;
    @Mock
    private OtaPackageService otaPackageService;
    @Mock
    private RuleEngineAssetProfileCache assetProfileCache;
    @Mock
    private RuleEngineDeviceProfileCache deviceProfileCache;
    @Mock
    private WidgetTypeService widgetTypeService;
    @Mock
    private WidgetsBundleService widgetsBundleService;
    @Mock
    private QueueService queueService;
    @Mock
    private ResourceService resourceService;
    @Mock
    private RuleEngineRpcService rpcService;
    @Mock
    private RuleEngineApiUsageStateService ruleEngineApiUsageStateService;
    @Mock
    private NotificationTargetService notificationTargetService;
    @Mock
    private NotificationTemplateService notificationTemplateService;
    @Mock
    private NotificationRequestService notificationRequestService;
    @Mock
    private NotificationRuleService notificationRuleService;
    @Mock
    private QueueStatsService queueStatsService;

    private TenantId tenantId;
    private TenantProfileId tenantProfileId;
    private NotificationId notificationId;
    private AbstractListeningExecutor dbExecutor;

    @BeforeEach
    public void before() {
        dbExecutor = new AbstractListeningExecutor() {
            @Override
            protected int getThreadPollSize() {
                return 3;
            }
        };
        dbExecutor.init();
        this.tenantId = new TenantId(UUID.randomUUID());
        this.tenantProfileId = new TenantProfileId(UUID.randomUUID());
        this.notificationId = new NotificationId(UUID.randomUUID());

        when(ctx.getTenantId()).thenReturn(tenantId);

        for (EntityType entityType : EntityType.values()) {
            initMocks(entityType, tenantId);
        }
    }

    @AfterEach
    public void after() {
        dbExecutor.destroy();
    }

    private void initMocks(EntityType entityType, TenantId tenantId) {
        switch (entityType) {
            case TENANT:
            case NOTIFICATION:
                break;
            case CUSTOMER:
                Customer customer = new Customer();
                customer.setTenantId(tenantId);

                when(ctx.getCustomerService()).thenReturn(customerService);
                doReturn(customer).when(customerService).findCustomerById(eq(tenantId), any());

                break;
            case USER:
                User user = new User();
                user.setTenantId(tenantId);

                when(ctx.getUserService()).thenReturn(userService);
                doReturn(user).when(userService).findUserById(eq(tenantId), any());

                break;
            case ASSET:
                Asset asset = new Asset();
                asset.setTenantId(tenantId);

                when(ctx.getAssetService()).thenReturn(assetService);
                doReturn(asset).when(assetService).findAssetById(eq(tenantId), any());

                break;
            case DEVICE:
                Device device = new Device();
                device.setTenantId(tenantId);

                when(ctx.getDeviceService()).thenReturn(deviceService);
                doReturn(device).when(deviceService).findDeviceById(eq(tenantId), any());

                break;
            case ALARM:
                Alarm alarm = new Alarm();
                alarm.setTenantId(tenantId);

                when(ctx.getAlarmService()).thenReturn(alarmService);
                doReturn(alarm).when(alarmService).findAlarmById(eq(tenantId), any());

                break;
            case RULE_CHAIN:
                RuleChain ruleChain = new RuleChain();
                ruleChain.setTenantId(tenantId);

                when(ctx.getRuleChainService()).thenReturn(ruleChainService);
                doReturn(ruleChain).when(ruleChainService).findRuleChainById(eq(tenantId), any());

                break;
            case ENTITY_VIEW:
                EntityView entityView = new EntityView();
                entityView.setTenantId(tenantId);

                when(ctx.getEntityViewService()).thenReturn(entityViewService);
                doReturn(entityView).when(entityViewService).findEntityViewById(eq(tenantId), any());

                break;
            case DASHBOARD:
                Dashboard dashboard = new Dashboard();
                dashboard.setTenantId(tenantId);

                when(ctx.getDashboardService()).thenReturn(dashboardService);
                doReturn(dashboard).when(dashboardService).findDashboardById(eq(tenantId), any());

                break;
            case EDGE:
                Edge edge = new Edge();
                edge.setTenantId(tenantId);

                when(ctx.getEdgeService()).thenReturn(edgeService);
                doReturn(edge).when(edgeService).findEdgeById(eq(tenantId), any());

                break;
            case OTA_PACKAGE:
                OtaPackage otaPackage = new OtaPackage();
                otaPackage.setTenantId(tenantId);

                when(ctx.getOtaPackageService()).thenReturn(otaPackageService);
                doReturn(otaPackage).when(otaPackageService).findOtaPackageInfoById(eq(tenantId), any());

                break;
            case ASSET_PROFILE:
                AssetProfile assetProfile = new AssetProfile();
                assetProfile.setTenantId(tenantId);

                when(ctx.getAssetProfileCache()).thenReturn(assetProfileCache);
                doReturn(assetProfile).when(assetProfileCache).get(eq(tenantId), any(AssetProfileId.class));

                break;
            case DEVICE_PROFILE:
                DeviceProfile deviceProfile = new DeviceProfile();
                deviceProfile.setTenantId(tenantId);

                when(ctx.getDeviceProfileCache()).thenReturn(deviceProfileCache);
                doReturn(deviceProfile).when(deviceProfileCache).get(eq(tenantId), any(DeviceProfileId.class));

                break;
            case WIDGET_TYPE:
                WidgetType widgetType = new WidgetType();
                widgetType.setTenantId(tenantId);

                when(ctx.getWidgetTypeService()).thenReturn(widgetTypeService);
                doReturn(widgetType).when(widgetTypeService).findWidgetTypeById(eq(tenantId), any());

                break;
            case WIDGETS_BUNDLE:
                WidgetsBundle widgetsBundle = new WidgetsBundle();
                widgetsBundle.setTenantId(tenantId);

                when(ctx.getWidgetBundleService()).thenReturn(widgetsBundleService);
                doReturn(widgetsBundle).when(widgetsBundleService).findWidgetsBundleById(eq(tenantId), any());

                break;
            case RPC:
                Rpc rpc = new Rpc();
                rpc.setTenantId(tenantId);

                when(ctx.getRpcService()).thenReturn(rpcService);
                doReturn(rpc).when(rpcService).findRpcById(eq(tenantId), any());

                break;
            case QUEUE:
                Queue queue = new Queue();
                queue.setTenantId(tenantId);

                when(ctx.getQueueService()).thenReturn(queueService);
                doReturn(queue).when(queueService).findQueueById(eq(tenantId), any());

                break;
            case API_USAGE_STATE:
                ApiUsageState apiUsageState = new ApiUsageState();
                apiUsageState.setTenantId(tenantId);

                when(ctx.getRuleEngineApiUsageStateService()).thenReturn(ruleEngineApiUsageStateService);
                doReturn(apiUsageState).when(ruleEngineApiUsageStateService).findApiUsageStateById(eq(tenantId), any());

                break;
            case TB_RESOURCE:
                TbResource tbResource = new TbResource();
                tbResource.setTenantId(tenantId);

                when(ctx.getResourceService()).thenReturn(resourceService);
                doReturn(tbResource).when(resourceService).findResourceInfoById(eq(tenantId), any());

                break;
            case RULE_NODE:
                RuleNode ruleNode = new RuleNode();

                when(ctx.getRuleChainService()).thenReturn(ruleChainService);
                doReturn(ruleNode).when(ruleChainService).findRuleNodeById(eq(tenantId), any());

                break;
            case TENANT_PROFILE:
                TenantProfile tenantProfile = new TenantProfile(tenantProfileId);

                when(ctx.getTenantProfile()).thenReturn(tenantProfile);

                break;
            case NOTIFICATION_TARGET:
                NotificationTarget notificationTarget = new NotificationTarget();
                notificationTarget.setTenantId(tenantId);
                when(ctx.getNotificationTargetService()).thenReturn(notificationTargetService);
                doReturn(notificationTarget).when(notificationTargetService).findNotificationTargetById(eq(tenantId), any());
                break;
            case NOTIFICATION_TEMPLATE:
                NotificationTemplate notificationTemplate = new NotificationTemplate();
                notificationTemplate.setTenantId(tenantId);
                when(ctx.getNotificationTemplateService()).thenReturn(notificationTemplateService);
                doReturn(notificationTemplate).when(notificationTemplateService).findNotificationTemplateById(eq(tenantId), any());
                break;
            case NOTIFICATION_REQUEST:
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setTenantId(tenantId);
                when(ctx.getNotificationRequestService()).thenReturn(notificationRequestService);
                doReturn(notificationRequest).when(notificationRequestService).findNotificationRequestById(eq(tenantId), any());
                break;
            case NOTIFICATION_RULE:
                NotificationRule notificationRule = new NotificationRule();
                notificationRule.setTenantId(tenantId);
                when(ctx.getNotificationRuleService()).thenReturn(notificationRuleService);
                doReturn(notificationRule).when(notificationRuleService).findNotificationRuleById(eq(tenantId), any());
                break;
            case QUEUE_STATS:
                QueueStats queueStats = new QueueStats();
                queueStats.setTenantId(tenantId);
                when(ctx.getQueueStatsService()).thenReturn(queueStatsService);
                doReturn(queueStats).when(queueStatsService).findQueueStatsById(eq(tenantId), any());
                break;
            default:
                throw new RuntimeException("Unexpected originator EntityType " + entityType);
        }
    }

    private EntityId getEntityId(EntityType entityType) {
        return EntityIdFactory.getByTypeAndUuid(entityType, UUID.randomUUID());
    }

    private void checkTenant(TenantId checkTenantId, boolean equals) {
        for (EntityType entityType : EntityType.values()) {
            EntityId entityId;
            if (EntityType.TENANT.equals(entityType)) {
                entityId = tenantId;
            } else if (EntityType.TENANT_PROFILE.equals(entityType)) {
                entityId = tenantProfileId;
            } else {
                entityId = getEntityId(entityType);
            }
            TenantId targetTenantId = TenantIdLoader.findTenantId(ctx, entityId);
            String msg = "Check entity type <" + entityType.name() + ">:";
            if (equals) {
                Assertions.assertEquals(targetTenantId, checkTenantId, msg);
            } else {
                Assertions.assertNotEquals(targetTenantId, checkTenantId, msg);
            }
        }
    }

    @Test
    public void test_findEntityIdAsync_current_tenant() {
        checkTenant(tenantId, true);
    }

    @Test
    public void test_findEntityIdAsync_other_tenant() {
        checkTenant(new TenantId(UUID.randomUUID()), false);
    }

}
