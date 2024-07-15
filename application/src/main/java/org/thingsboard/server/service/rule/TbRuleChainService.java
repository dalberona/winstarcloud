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
package org.winstarcloud.server.service.rule;

import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.RuleChainId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.rule.DefaultRuleChainCreateRequest;
import org.winstarcloud.server.common.data.rule.RuleChain;
import org.winstarcloud.server.common.data.rule.RuleChainMetaData;
import org.winstarcloud.server.common.data.rule.RuleChainOutputLabelsUsage;
import org.winstarcloud.server.common.data.rule.RuleChainUpdateResult;
import org.winstarcloud.server.common.data.rule.RuleNode;
import org.winstarcloud.server.service.entitiy.SimpleTbEntityService;

import java.util.List;
import java.util.Set;

public interface TbRuleChainService extends SimpleTbEntityService<RuleChain> {

    Set<String> getRuleChainOutputLabels(TenantId tenantId, RuleChainId ruleChainId);

    List<RuleChainOutputLabelsUsage> getOutputLabelUsage(TenantId tenantId, RuleChainId ruleChainId);

    List<RuleChain> updateRelatedRuleChains(TenantId tenantId, RuleChainId ruleChainId, RuleChainUpdateResult result);

    RuleChain saveDefaultByName(TenantId tenantId, DefaultRuleChainCreateRequest request, User user) throws Exception;

    RuleChain setRootRuleChain(TenantId tenantId, RuleChain ruleChain, User user) throws WinstarcloudException;

    RuleChainMetaData saveRuleChainMetaData(TenantId tenantId, RuleChain ruleChain, RuleChainMetaData ruleChainMetaData,
                                            boolean updateRelated, User user) throws Exception;

    RuleChain assignRuleChainToEdge(TenantId tenantId, RuleChain ruleChain, Edge edge, User user) throws WinstarcloudException;

    RuleChain unassignRuleChainFromEdge(TenantId tenantId, RuleChain ruleChain, Edge edge, User user) throws WinstarcloudException;

    RuleChain setEdgeTemplateRootRuleChain(TenantId tenantId, RuleChain ruleChain, User user) throws WinstarcloudException;

    RuleChain setAutoAssignToEdgeRuleChain(TenantId tenantId, RuleChain ruleChain, User user) throws WinstarcloudException;

    RuleChain unsetAutoAssignToEdgeRuleChain(TenantId tenantId, RuleChain ruleChain, User user) throws WinstarcloudException;

    RuleNode updateRuleNodeConfiguration(RuleNode ruleNode);
}
