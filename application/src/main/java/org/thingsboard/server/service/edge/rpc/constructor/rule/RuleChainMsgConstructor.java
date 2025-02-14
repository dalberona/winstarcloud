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
package org.winstarcloud.server.service.edge.rpc.constructor.rule;

import org.winstarcloud.server.common.data.id.RuleChainId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.rule.RuleChain;
import org.winstarcloud.server.common.data.rule.RuleChainMetaData;
import org.winstarcloud.server.gen.edge.v1.EdgeVersion;
import org.winstarcloud.server.gen.edge.v1.RuleChainMetadataUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.RuleChainUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.UpdateMsgType;
import org.winstarcloud.server.service.edge.rpc.constructor.MsgConstructor;

public interface RuleChainMsgConstructor extends MsgConstructor {

    RuleChainUpdateMsg constructRuleChainUpdatedMsg(UpdateMsgType msgType, RuleChain ruleChain, boolean isRoot);

    RuleChainUpdateMsg constructRuleChainDeleteMsg(RuleChainId ruleChainId);

    RuleChainMetadataUpdateMsg constructRuleChainMetadataUpdatedMsg(TenantId tenantId,
                                                                    UpdateMsgType msgType,
                                                                    RuleChainMetaData ruleChainMetaData,
                                                                    EdgeVersion edgeVersion);
}
