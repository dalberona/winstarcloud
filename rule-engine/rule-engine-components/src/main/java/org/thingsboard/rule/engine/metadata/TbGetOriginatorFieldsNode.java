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
package org.winstarcloud.rule.engine.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.rule.engine.api.RuleNode;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbNodeConfiguration;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.rule.engine.api.util.TbNodeUtils;
import org.winstarcloud.rule.engine.util.TbMsgSource;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.plugin.ComponentType;
import org.winstarcloud.server.common.data.util.TbPair;
import org.winstarcloud.server.common.msg.TbMsg;

import java.util.concurrent.ExecutionException;

/**
 * Created by ashvayka on 19.01.18.
 */
@Slf4j
@RuleNode(type = ComponentType.ENRICHMENT,
        name = "originator fields",
        configClazz = TbGetOriginatorFieldsConfiguration.class,
        version = 1,
        nodeDescription = "Adds message originator fields values into message or message metadata",
        nodeDetails = "Fetches fields values specified in the mapping. If specified field is not part of originator fields it will be ignored. " +
                "Useful when you need to retrieve originator fields and use them for further message processing.<br><br>" +
                "Output connections: <code>Success</code>, <code>Failure</code>.",
        uiResources = {"static/rulenode/rulenode-core-config.js"},
        configDirective = "tbEnrichmentNodeOriginatorFieldsConfig")
public class TbGetOriginatorFieldsNode extends TbAbstractGetMappedDataNode<EntityId, TbGetOriginatorFieldsConfiguration> {

    protected final static String DATA_MAPPING_PROPERTY_NAME = "dataMapping";
    protected static final String OLD_DATA_MAPPING_PROPERTY_NAME = "fieldsMapping";

    @Override
    protected TbGetOriginatorFieldsConfiguration loadNodeConfiguration(TbNodeConfiguration configuration) throws TbNodeException {
        var config = TbNodeUtils.convert(configuration, TbGetOriginatorFieldsConfiguration.class);
        checkIfMappingIsNotEmptyOrElseThrow(config.getDataMapping());
        return config;
    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) throws ExecutionException, InterruptedException, TbNodeException {
        var msgDataAsJsonNode = TbMsgSource.DATA.equals(fetchTo) ? getMsgDataAsObjectNode(msg) : null;
        processFieldsData(ctx, msg, msg.getOriginator(), msgDataAsJsonNode, config.isIgnoreNullStrings());
    }

    @Override
    public TbPair<Boolean, JsonNode> upgrade(int fromVersion, JsonNode oldConfiguration) throws TbNodeException {
        if (fromVersion == 0) {
            var newConfigObjectNode = (ObjectNode) oldConfiguration;
            if (!newConfigObjectNode.has(OLD_DATA_MAPPING_PROPERTY_NAME)) {
                throw new TbNodeException("property to update: '" + OLD_DATA_MAPPING_PROPERTY_NAME + "' doesn't exists in configuration!");
            }
            newConfigObjectNode.set(DATA_MAPPING_PROPERTY_NAME, newConfigObjectNode.get(OLD_DATA_MAPPING_PROPERTY_NAME));
            newConfigObjectNode.remove(OLD_DATA_MAPPING_PROPERTY_NAME);
            newConfigObjectNode.put(FETCH_TO_PROPERTY_NAME, TbMsgSource.METADATA.name());
            return new TbPair<>(true, newConfigObjectNode);
        }
        return new TbPair<>(false, oldConfiguration);
    }

}
