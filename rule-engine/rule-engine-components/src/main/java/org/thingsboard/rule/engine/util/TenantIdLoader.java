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

import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.HasTenantId;
import org.winstarcloud.server.common.data.id.AlarmId;
import org.winstarcloud.server.common.data.id.ApiUsageStateId;
import org.winstarcloud.server.common.data.id.AssetId;
import org.winstarcloud.server.common.data.id.AssetProfileId;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.DashboardId;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.EntityViewId;
import org.winstarcloud.server.common.data.id.NotificationRequestId;
import org.winstarcloud.server.common.data.id.NotificationRuleId;
import org.winstarcloud.server.common.data.id.NotificationTargetId;
import org.winstarcloud.server.common.data.id.NotificationTemplateId;
import org.winstarcloud.server.common.data.id.OtaPackageId;
import org.winstarcloud.server.common.data.id.QueueId;
import org.winstarcloud.server.common.data.id.QueueStatsId;
import org.winstarcloud.server.common.data.id.RpcId;
import org.winstarcloud.server.common.data.id.RuleChainId;
import org.winstarcloud.server.common.data.id.RuleNodeId;
import org.winstarcloud.server.common.data.id.TbResourceId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.id.WidgetTypeId;
import org.winstarcloud.server.common.data.id.WidgetsBundleId;
import org.winstarcloud.server.common.data.rule.RuleNode;

import java.util.UUID;

public class TenantIdLoader {

    public static TenantId findTenantId(TbContext ctx, EntityId entityId) {
        UUID id = entityId.getId();
        EntityType entityType = entityId.getEntityType();
        TenantId ctxTenantId = ctx.getTenantId();

        HasTenantId tenantEntity;
        switch (entityType) {
            case TENANT:
                return new TenantId(id);
            case CUSTOMER:
                tenantEntity = ctx.getCustomerService().findCustomerById(ctxTenantId, new CustomerId(id));
                break;
            case USER:
                tenantEntity = ctx.getUserService().findUserById(ctxTenantId, new UserId(id));
                break;
            case ASSET:
                tenantEntity = ctx.getAssetService().findAssetById(ctxTenantId, new AssetId(id));
                break;
            case DEVICE:
                tenantEntity = ctx.getDeviceService().findDeviceById(ctxTenantId, new DeviceId(id));
                break;
            case ALARM:
                tenantEntity = ctx.getAlarmService().findAlarmById(ctxTenantId, new AlarmId(id));
                break;
            case RULE_CHAIN:
                tenantEntity = ctx.getRuleChainService().findRuleChainById(ctxTenantId, new RuleChainId(id));
                break;
            case ENTITY_VIEW:
                tenantEntity = ctx.getEntityViewService().findEntityViewById(ctxTenantId, new EntityViewId(id));
                break;
            case DASHBOARD:
                tenantEntity = ctx.getDashboardService().findDashboardById(ctxTenantId, new DashboardId(id));
                break;
            case EDGE:
                tenantEntity = ctx.getEdgeService().findEdgeById(ctxTenantId, new EdgeId(id));
                break;
            case OTA_PACKAGE:
                tenantEntity = ctx.getOtaPackageService().findOtaPackageInfoById(ctxTenantId, new OtaPackageId(id));
                break;
            case ASSET_PROFILE:
                tenantEntity = ctx.getAssetProfileCache().get(ctxTenantId, new AssetProfileId(id));
                break;
            case DEVICE_PROFILE:
                tenantEntity = ctx.getDeviceProfileCache().get(ctxTenantId, new DeviceProfileId(id));
                break;
            case WIDGET_TYPE:
                tenantEntity = ctx.getWidgetTypeService().findWidgetTypeById(ctxTenantId, new WidgetTypeId(id));
                break;
            case WIDGETS_BUNDLE:
                tenantEntity = ctx.getWidgetBundleService().findWidgetsBundleById(ctxTenantId, new WidgetsBundleId(id));
                break;
            case RPC:
                tenantEntity = ctx.getRpcService().findRpcById(ctxTenantId, new RpcId(id));
                break;
            case QUEUE:
                tenantEntity = ctx.getQueueService().findQueueById(ctxTenantId, new QueueId(id));
                break;
            case API_USAGE_STATE:
                tenantEntity = ctx.getRuleEngineApiUsageStateService().findApiUsageStateById(ctxTenantId, new ApiUsageStateId(id));
                break;
            case TB_RESOURCE:
                tenantEntity = ctx.getResourceService().findResourceInfoById(ctxTenantId, new TbResourceId(id));
                break;
            case RULE_NODE:
                RuleNode ruleNode = ctx.getRuleChainService().findRuleNodeById(ctxTenantId, new RuleNodeId(id));
                if (ruleNode != null) {
                    tenantEntity = ctx.getRuleChainService().findRuleChainById(ctxTenantId, ruleNode.getRuleChainId());
                } else {
                    tenantEntity = null;
                }
                break;
            case TENANT_PROFILE:
                if (ctx.getTenantProfile().getId().equals(entityId)) {
                    return ctxTenantId;
                } else {
                    tenantEntity = null;
                }
                break;
            case NOTIFICATION_TARGET:
                tenantEntity = ctx.getNotificationTargetService().findNotificationTargetById(ctxTenantId, new NotificationTargetId(id));
                break;
            case NOTIFICATION_TEMPLATE:
                tenantEntity = ctx.getNotificationTemplateService().findNotificationTemplateById(ctxTenantId, new NotificationTemplateId(id));
                break;
            case NOTIFICATION_REQUEST:
                tenantEntity = ctx.getNotificationRequestService().findNotificationRequestById(ctxTenantId, new NotificationRequestId(id));
                break;
            case NOTIFICATION:
                return ctxTenantId;
            case NOTIFICATION_RULE:
                tenantEntity = ctx.getNotificationRuleService().findNotificationRuleById(ctxTenantId, new NotificationRuleId(id));
                break;
            case QUEUE_STATS:
                tenantEntity = ctx.getQueueStatsService().findQueueStatsById(ctxTenantId, new QueueStatsId(id));
                break;
            default:
                throw new RuntimeException("Unexpected entity type: " + entityId.getEntityType());
        }
        return tenantEntity != null ? tenantEntity.getTenantId() : null;
    }

}
