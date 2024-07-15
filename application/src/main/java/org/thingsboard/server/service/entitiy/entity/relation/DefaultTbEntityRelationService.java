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
package org.winstarcloud.server.service.entitiy.entity.relation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.audit.ActionType;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.dao.relation.RelationService;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.entitiy.AbstractTbEntityService;

@Service
@TbCoreComponent
@AllArgsConstructor
@Slf4j
public class DefaultTbEntityRelationService extends AbstractTbEntityService implements TbEntityRelationService {

    private final RelationService relationService;

    @Override
    public void save(TenantId tenantId, CustomerId customerId, EntityRelation relation, User user) throws WinstarcloudException {
        ActionType actionType = ActionType.RELATION_ADD_OR_UPDATE;
        try {
            relationService.saveRelation(tenantId, relation);
            logEntityActionService.logEntityRelationAction(tenantId, customerId,
                    relation, user, actionType, null, relation);
        } catch (Exception e) {
            logEntityActionService.logEntityRelationAction(tenantId, customerId,
                    relation, user, actionType, e, relation);
            throw e;
        }
    }

    @Override
    public void delete(TenantId tenantId, CustomerId customerId, EntityRelation relation, User user) throws WinstarcloudException {
        ActionType actionType = ActionType.RELATION_DELETED;
        try {
            boolean found = relationService.deleteRelation(tenantId, relation.getFrom(), relation.getTo(), relation.getType(), relation.getTypeGroup());
            if (!found) {
                throw new WinstarcloudException("Requested item wasn't found!", WinstarcloudErrorCode.ITEM_NOT_FOUND);
            }
            logEntityActionService.logEntityRelationAction(tenantId, customerId, relation, user, actionType, null, relation);
        } catch (Exception e) {
            logEntityActionService.logEntityRelationAction(tenantId, customerId,
                    relation, user, actionType, e, relation);
            throw e;
        }
    }

    @Override
    public void deleteCommonRelations(TenantId tenantId, CustomerId customerId, EntityId entityId, User user) throws WinstarcloudException {
        try {
            relationService.deleteEntityCommonRelations(tenantId, entityId);
            logEntityActionService.logEntityAction(tenantId, entityId, null, customerId, ActionType.RELATIONS_DELETED, user);
        } catch (Exception e) {
            logEntityActionService.logEntityAction(tenantId, entityId, null, customerId,
                    ActionType.RELATIONS_DELETED, user, e);
            throw e;
        }
    }
}
