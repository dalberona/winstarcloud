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
package org.winstarcloud.server.actors.stats;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.actors.ActorSystemContext;
import org.winstarcloud.server.actors.TbActor;
import org.winstarcloud.server.actors.TbActorId;
import org.winstarcloud.server.actors.TbStringActorId;
import org.winstarcloud.server.actors.service.ContextAwareActor;
import org.winstarcloud.server.actors.service.ContextBasedCreator;
import org.winstarcloud.server.common.data.event.StatisticsEvent;
import org.winstarcloud.server.common.msg.MsgType;
import org.winstarcloud.server.common.msg.TbActorMsg;

@Slf4j
public class StatsActor extends ContextAwareActor {

    public StatsActor(ActorSystemContext context) {
        super(context);
    }

    @Override
    protected boolean doProcess(TbActorMsg msg) {
        log.debug("Received message: {}", msg);
        if (msg.getMsgType().equals(MsgType.STATS_PERSIST_MSG)) {
            onStatsPersistMsg((StatsPersistMsg) msg);
            return true;
        } else {
            return false;
        }
    }

    public void onStatsPersistMsg(StatsPersistMsg msg) {
        if (msg.isEmpty()) {
            return;
        }
        systemContext.getEventService().saveAsync(StatisticsEvent.builder()
                .tenantId(msg.getTenantId())
                .entityId(msg.getEntityId().getId())
                .serviceId(systemContext.getServiceInfoProvider().getServiceId())
                .messagesProcessed(msg.getMessagesProcessed())
                .errorsOccurred(msg.getErrorsOccurred())
                .build()
        );
    }

    private JsonNode toBodyJson(String serviceId, long messagesProcessed, long errorsOccurred) {
        return JacksonUtil.newObjectNode().put("server", serviceId).put("messagesProcessed", messagesProcessed).put("errorsOccurred", errorsOccurred);
    }

    public static class ActorCreator extends ContextBasedCreator {
        private final String actorId;

        public ActorCreator(ActorSystemContext context, String actorId) {
            super(context);
            this.actorId = actorId;
        }

        @Override
        public TbActorId createActorId() {
            return new TbStringActorId(actorId);
        }

        @Override
        public TbActor createActor() {
            return new StatsActor(context);
        }
    }
}
