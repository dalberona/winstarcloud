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
package org.winstarcloud.server.service.edge.rpc.processor.relation;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.gen.edge.v1.RelationUpdateMsg;
import org.winstarcloud.server.service.edge.rpc.processor.BaseEdgeProcessor;

@Slf4j
public abstract class BaseRelationProcessor extends BaseEdgeProcessor {

    protected ListenableFuture<Void> processRelationMsg(TenantId tenantId, RelationUpdateMsg relationUpdateMsg) {
        log.trace("[{}] processRelationMsg [{}]", tenantId, relationUpdateMsg);
        try {
            EntityRelation entityRelation = constructEntityRelationFromUpdateMsg(relationUpdateMsg);
            if (entityRelation == null) {
                throw new RuntimeException("[{" + tenantId + "}] relationUpdateMsg {" + relationUpdateMsg + "} cannot be converted to entity relation");
            }
            switch (relationUpdateMsg.getMsgType()) {
                case ENTITY_CREATED_RPC_MESSAGE:
                case ENTITY_UPDATED_RPC_MESSAGE:
                    if (isEntityExists(tenantId, entityRelation.getTo())
                            && isEntityExists(tenantId, entityRelation.getFrom())) {
                        relationService.saveRelation(tenantId, entityRelation);
                    } else {
                        log.warn("[{}] Skipping relating update msg because from/to entity doesn't exists on edge, {}", tenantId, relationUpdateMsg);
                    }
                    break;
                case ENTITY_DELETED_RPC_MESSAGE:
                    relationService.deleteRelation(tenantId, entityRelation);
                    break;
                case UNRECOGNIZED:
                default:
                    return handleUnsupportedMsgType(relationUpdateMsg.getMsgType());
            }
        } catch (Exception e) {
            log.error("[{}] Failed to process relation update msg [{}]", tenantId, relationUpdateMsg, e);
            return Futures.immediateFailedFuture(e);
        }
        return Futures.immediateFuture(null);
    }

    protected abstract EntityRelation constructEntityRelationFromUpdateMsg(RelationUpdateMsg relationUpdateMsg);

}
