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
package org.winstarcloud.server.actors.shared;

import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.server.actors.ActorSystemContext;
import org.winstarcloud.server.actors.TbActor;
import org.winstarcloud.server.actors.TbActorId;
import org.winstarcloud.server.actors.TbStringActorId;
import org.winstarcloud.server.actors.service.ContextAwareActor;
import org.winstarcloud.server.actors.service.ContextBasedCreator;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.msg.TbActorMsg;
import org.winstarcloud.server.common.msg.aware.RuleChainAwareMsg;
import org.winstarcloud.server.common.msg.queue.RuleEngineException;

import java.util.UUID;

@Slf4j
public class RuleChainErrorActor extends ContextAwareActor {

    private final TenantId tenantId;
    private final RuleEngineException error;

    private RuleChainErrorActor(ActorSystemContext systemContext, TenantId tenantId, RuleEngineException error) {
        super(systemContext);
        this.tenantId = tenantId;
        this.error = error;
    }

    @Override
    protected boolean doProcess(TbActorMsg msg) {
        if (msg instanceof RuleChainAwareMsg) {
            log.debug("[{}] Reply with {} for message {}", tenantId, error.getMessage(), msg);
            var rcMsg = (RuleChainAwareMsg) msg;
            rcMsg.getMsg().getCallback().onFailure(error);
            return true;
        } else {
            return false;
        }
    }

    public static class ActorCreator extends ContextBasedCreator {

        private final TenantId tenantId;
        private final RuleEngineException error;

        public ActorCreator(ActorSystemContext context, TenantId tenantId, RuleEngineException error) {
            super(context);
            this.tenantId = tenantId;
            this.error = error;
        }

        @Override
        public TbActorId createActorId() {
            return new TbStringActorId(UUID.randomUUID().toString());
        }

        @Override
        public TbActor createActor() {
            return new RuleChainErrorActor(context, tenantId, error);
        }
    }

}
