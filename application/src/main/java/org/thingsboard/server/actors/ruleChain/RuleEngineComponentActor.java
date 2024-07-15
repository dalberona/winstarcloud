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
package org.winstarcloud.server.actors.ruleChain;

import org.winstarcloud.server.actors.ActorSystemContext;
import org.winstarcloud.server.actors.TbRuleNodeUpdateException;
import org.winstarcloud.server.actors.service.ComponentActor;
import org.winstarcloud.server.actors.shared.ComponentMsgProcessor;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.RuleChainId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.notification.rule.trigger.RuleEngineComponentLifecycleEventTrigger;
import org.winstarcloud.server.common.data.plugin.ComponentLifecycleEvent;
import org.winstarcloud.server.common.msg.TbActorStopReason;

public abstract class RuleEngineComponentActor<T extends EntityId, P extends ComponentMsgProcessor<T>> extends ComponentActor<T, P> {

    public RuleEngineComponentActor(ActorSystemContext systemContext, TenantId tenantId, T id) {
        super(systemContext, tenantId, id);
    }

    @Override
    protected void logLifecycleEvent(ComponentLifecycleEvent event, Exception e) {
        super.logLifecycleEvent(event, e);
        if (e instanceof TbRuleNodeUpdateException || (event == ComponentLifecycleEvent.STARTED && e != null)) {
            return;
        }
        processNotificationRule(event, e);
    }

    @Override
    public void destroy(TbActorStopReason stopReason, Throwable cause) {
        super.destroy(stopReason, cause);
        if (stopReason == TbActorStopReason.INIT_FAILED && cause != null) {
            processNotificationRule(ComponentLifecycleEvent.STARTED, cause);
        }
    }

    private void processNotificationRule(ComponentLifecycleEvent event, Throwable e) {
        systemContext.getNotificationRuleProcessor().process(RuleEngineComponentLifecycleEventTrigger.builder()
                .tenantId(tenantId)
                .ruleChainId(getRuleChainId())
                .ruleChainName(getRuleChainName())
                .componentId(id)
                .componentName(processor.getComponentName())
                .eventType(event)
                .error(e)
                .build());
    }

    protected abstract RuleChainId getRuleChainId();

    protected abstract String getRuleChainName();

}
