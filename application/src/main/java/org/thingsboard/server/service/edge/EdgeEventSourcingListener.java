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
package org.winstarcloud.server.service.edge;

import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.cluster.TbClusterService;
import org.winstarcloud.server.common.data.EdgeUtils;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.OtaPackageInfo;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.alarm.Alarm;
import org.winstarcloud.server.common.data.alarm.AlarmApiCallResult;
import org.winstarcloud.server.common.data.alarm.AlarmComment;
import org.winstarcloud.server.common.data.audit.ActionType;
import org.winstarcloud.server.common.data.edge.EdgeEventActionType;
import org.winstarcloud.server.common.data.edge.EdgeEventType;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.oauth2.OAuth2Info;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.common.data.relation.RelationTypeGroup;
import org.winstarcloud.server.common.data.rule.RuleChain;
import org.winstarcloud.server.common.data.rule.RuleChainType;
import org.winstarcloud.server.common.data.security.Authority;
import org.winstarcloud.server.dao.edge.EdgeSynchronizationManager;
import org.winstarcloud.server.dao.eventsourcing.ActionEntityEvent;
import org.winstarcloud.server.dao.eventsourcing.DeleteEntityEvent;
import org.winstarcloud.server.dao.eventsourcing.RelationActionEvent;
import org.winstarcloud.server.dao.eventsourcing.SaveEntityEvent;
import org.winstarcloud.server.dao.tenant.TenantService;
import org.winstarcloud.server.dao.user.UserServiceImpl;

import javax.annotation.PostConstruct;

/**
 * This event listener does not support async event processing because relay on ThreadLocal
 * Another possible approach is to implement a special annotation and a bunch of classes similar to TransactionalApplicationListener
 * This class is the simplest approach to maintain edge synchronization within the single class.
 * <p>
 * For async event publishers, you have to decide whether publish event on creating async task in the same thread where dao method called
 * @Autowired
 * EdgeEventSynchronizationManager edgeSynchronizationManager
 * ...
 *   //some async write action make future
 *   if (!edgeSynchronizationManager.isSync()) {
 *     future.addCallback(eventPublisher.publishEvent(...))
 *   }
 * */
@Component
@RequiredArgsConstructor
@Slf4j
public class EdgeEventSourcingListener {

    private final TbClusterService tbClusterService;
    private final EdgeSynchronizationManager edgeSynchronizationManager;
    private final TenantService tenantService;

    @PostConstruct
    public void init() {
        log.info("EdgeEventSourcingListener initiated");
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void handleEvent(SaveEntityEvent<?> event) {
        try {
            if (!isValidSaveEntityEventForEdgeProcessing(event)) {
                return;
            }
            log.trace("[{}] SaveEntityEvent called: {}", event.getTenantId(), event);
            boolean isCreated = Boolean.TRUE.equals(event.getCreated());
            String body = getBodyMsgForEntityEvent(event.getEntity());
            EdgeEventType type = getEdgeEventTypeForEntityEvent(event.getEntity());
            EdgeEventActionType action = getActionForEntityEvent(event.getEntity(), isCreated);
            tbClusterService.sendNotificationMsgToEdge(event.getTenantId(), null, event.getEntityId(),
                    body, type, action, edgeSynchronizationManager.getEdgeId().get());
        } catch (Exception e) {
            log.error("[{}] failed to process SaveEntityEvent: {}", event.getTenantId(), event, e);
        }
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void handleEvent(DeleteEntityEvent<?> event) {
        TenantId tenantId = event.getTenantId();
        EntityType entityType = event.getEntityId().getEntityType();
        if (!tenantId.isSysTenantId() && !tenantService.tenantExists(tenantId)) {
            log.debug("[{}] Ignoring DeleteEntityEvent because tenant does not exist: {}", tenantId, event);
            return;
        }
        try {
            if (EntityType.EDGE.equals(entityType) || EntityType.TENANT.equals(entityType)) {
                return;
            }
            log.trace("[{}] DeleteEntityEvent called: {}", tenantId, event);
            EdgeEventType type = getEdgeEventTypeForEntityEvent(event.getEntity());
            EdgeEventActionType actionType = getEdgeEventActionTypeForEntityEvent(event.getEntity());
            tbClusterService.sendNotificationMsgToEdge(tenantId, null, event.getEntityId(),
                    JacksonUtil.toString(event.getEntity()), type, actionType,
                    edgeSynchronizationManager.getEdgeId().get());
        } catch (Exception e) {
            log.error("[{}] failed to process DeleteEntityEvent: {}", tenantId, event, e);
        }
    }

    private EdgeEventActionType getEdgeEventActionTypeForEntityEvent(Object entity) {
        if (entity instanceof AlarmComment) {
            return EdgeEventActionType.DELETED_COMMENT;
        } else if (entity instanceof Alarm) {
            return EdgeEventActionType.ALARM_DELETE;
        }
        return EdgeEventActionType.DELETED;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void handleEvent(ActionEntityEvent<?> event) {
        if (EntityType.DEVICE.equals(event.getEntityId().getEntityType())
                && ActionType.ASSIGNED_TO_TENANT.equals(event.getActionType())) {
            return;
        }
        try {
            log.trace("[{}] ActionEntityEvent called: {}", event.getTenantId(), event);
            tbClusterService.sendNotificationMsgToEdge(event.getTenantId(), event.getEdgeId(), event.getEntityId(),
                    event.getBody(), null, EdgeUtils.getEdgeEventActionTypeByActionType(event.getActionType()),
                    edgeSynchronizationManager.getEdgeId().get());
        } catch (Exception e) {
            log.error("[{}] failed to process ActionEntityEvent: {}", event.getTenantId(), event, e);
        }
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void handleEvent(RelationActionEvent event) {
        try {
            EntityRelation relation = event.getRelation();
            if (relation == null) {
                log.trace("[{}] skipping RelationActionEvent event in case relation is null: {}", event.getTenantId(), event);
                return;
            }
            if (!RelationTypeGroup.COMMON.equals(relation.getTypeGroup())) {
                log.trace("[{}] skipping RelationActionEvent event in case NOT COMMON relation type group: {}", event.getTenantId(), event);
                return;
            }
            log.trace("[{}] RelationActionEvent called: {}", event.getTenantId(), event);
            tbClusterService.sendNotificationMsgToEdge(event.getTenantId(), null, null,
                    JacksonUtil.toString(relation), EdgeEventType.RELATION, EdgeUtils.getEdgeEventActionTypeByActionType(event.getActionType()),
                    edgeSynchronizationManager.getEdgeId().get());
        } catch (Exception e) {
            log.error("[{}] failed to process RelationActionEvent: {}", event.getTenantId(), event, e);
        }
    }

    private boolean isValidSaveEntityEventForEdgeProcessing(SaveEntityEvent<?> event) {
        Object entity = event.getEntity();
        Object oldEntity = event.getOldEntity();
        if (event.getEntityId() != null) {
            switch (event.getEntityId().getEntityType()) {
                case RULE_CHAIN:
                    if (entity instanceof RuleChain ruleChain) {
                        return RuleChainType.EDGE.equals(ruleChain.getType());
                    }
                    break;
                case USER:
                    if (entity instanceof User user) {
                        if (Authority.SYS_ADMIN.equals(user.getAuthority())) {
                            return false;
                        }
                        if (oldEntity != null) {
                            User oldUser = (User) oldEntity;
                            cleanUpUserAdditionalInfo(oldUser);
                            cleanUpUserAdditionalInfo(user);
                            return !user.equals(oldUser);
                        }
                    }
                    break;
                case OTA_PACKAGE:
                    if (entity instanceof OtaPackageInfo otaPackageInfo) {
                        return otaPackageInfo.hasUrl() || otaPackageInfo.isHasData();
                    }
                    break;
                case ALARM:
                    if (entity instanceof AlarmApiCallResult || entity instanceof Alarm) {
                        return false;
                    }
                    break;
                case TENANT:
                    return !event.getCreated();
                case API_USAGE_STATE, EDGE:
                    return false;
            }
        }
        if (entity instanceof OAuth2Info oAuth2Info) {
            return oAuth2Info.isEdgeEnabled();
        }
        // Default: If the entity doesn't match any of the conditions, consider it as valid.
        return true;
    }

    private void cleanUpUserAdditionalInfo(User user) {
        // reset FAILED_LOGIN_ATTEMPTS and LAST_LOGIN_TS - edge is not interested in this information
        if (user.getAdditionalInfo() instanceof NullNode) {
            user.setAdditionalInfo(null);
        }
        if (user.getAdditionalInfo() instanceof ObjectNode additionalInfo) {
            additionalInfo.remove(UserServiceImpl.FAILED_LOGIN_ATTEMPTS);
            additionalInfo.remove(UserServiceImpl.LAST_LOGIN_TS);
            if (additionalInfo.isEmpty()) {
                user.setAdditionalInfo(null);
            } else {
                user.setAdditionalInfo(additionalInfo);
            }
        }
    }

    private EdgeEventType getEdgeEventTypeForEntityEvent(Object entity) {
        if (entity instanceof AlarmComment) {
            return EdgeEventType.ALARM_COMMENT;
        } else if (entity instanceof OAuth2Info) {
            return EdgeEventType.OAUTH2;
        }
        return null;
    }

    private String getBodyMsgForEntityEvent(Object entity) {
        if (entity instanceof AlarmComment) {
            return JacksonUtil.toString(entity);
        } else if (entity instanceof OAuth2Info) {
            return JacksonUtil.toString(entity);
        }
        return null;
    }

    private EdgeEventActionType getActionForEntityEvent(Object entity, boolean isCreated) {
        if (entity instanceof AlarmComment) {
            return isCreated ? EdgeEventActionType.ADDED_COMMENT : EdgeEventActionType.UPDATED_COMMENT;
        }
        return isCreated ? EdgeEventActionType.ADDED : EdgeEventActionType.UPDATED;
    }

}
