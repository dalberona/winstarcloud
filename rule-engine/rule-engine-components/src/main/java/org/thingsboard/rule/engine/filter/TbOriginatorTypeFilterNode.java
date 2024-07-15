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
package org.winstarcloud.rule.engine.filter;

import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.rule.engine.api.RuleNode;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbNode;
import org.winstarcloud.rule.engine.api.TbNodeConfiguration;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.server.common.data.msg.TbNodeConnectionType;
import org.winstarcloud.rule.engine.api.util.TbNodeUtils;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.plugin.ComponentType;
import org.winstarcloud.server.common.msg.TbMsg;

@Slf4j
@RuleNode(
        type = ComponentType.FILTER,
        name = "entity type filter",
        configClazz = TbOriginatorTypeFilterNodeConfiguration.class,
        relationTypes = {TbNodeConnectionType.TRUE, TbNodeConnectionType.FALSE},
        nodeDescription = "Filter incoming messages by the type of message originator entity",
        nodeDetails = "Checks that the entity type of the incoming message originator matches one of the values specified in the filter.<br><br>" +
                "Output connections: <code>True</code>, <code>False</code>, <code>Failure</code>",
        uiResources = {"static/rulenode/rulenode-core-config.js"},
        configDirective = "tbFilterNodeOriginatorTypeConfig")
public class TbOriginatorTypeFilterNode implements TbNode {

    TbOriginatorTypeFilterNodeConfiguration config;

    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbOriginatorTypeFilterNodeConfiguration.class);
    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) {
        EntityType originatorType = msg.getOriginator().getEntityType();
        ctx.tellNext(msg, config.getOriginatorTypes().contains(originatorType) ? TbNodeConnectionType.TRUE : TbNodeConnectionType.FALSE);
    }

}
