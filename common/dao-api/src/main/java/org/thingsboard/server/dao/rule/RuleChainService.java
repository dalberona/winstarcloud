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
package org.winstarcloud.server.dao.rule;

import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.RuleChainId;
import org.winstarcloud.server.common.data.id.RuleNodeId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.common.data.rule.RuleChain;
import org.winstarcloud.server.common.data.rule.RuleChainData;
import org.winstarcloud.server.common.data.rule.RuleChainImportResult;
import org.winstarcloud.server.common.data.rule.RuleChainMetaData;
import org.winstarcloud.server.common.data.rule.RuleChainType;
import org.winstarcloud.server.common.data.rule.RuleChainUpdateResult;
import org.winstarcloud.server.common.data.rule.RuleNode;
import org.winstarcloud.server.dao.entity.EntityDaoService;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Created by igor on 3/12/18.
 */
public interface RuleChainService extends EntityDaoService {

    RuleChain saveRuleChain(RuleChain ruleChain);

    RuleChain saveRuleChain(RuleChain ruleChain, boolean publishSaveEvent);

    boolean setRootRuleChain(TenantId tenantId, RuleChainId ruleChainId);

    RuleChainUpdateResult saveRuleChainMetaData(TenantId tenantId, RuleChainMetaData ruleChainMetaData, Function<RuleNode, RuleNode> ruleNodeUpdater);

    RuleChainUpdateResult saveRuleChainMetaData(TenantId tenantId, RuleChainMetaData ruleChainMetaData, Function<RuleNode, RuleNode> ruleNodeUpdater, boolean publishSaveEvent);

    RuleChainMetaData loadRuleChainMetaData(TenantId tenantId, RuleChainId ruleChainId);

    RuleChain findRuleChainById(TenantId tenantId, RuleChainId ruleChainId);

    RuleNode findRuleNodeById(TenantId tenantId, RuleNodeId ruleNodeId);

    ListenableFuture<RuleChain> findRuleChainByIdAsync(TenantId tenantId, RuleChainId ruleChainId);

    ListenableFuture<RuleNode> findRuleNodeByIdAsync(TenantId tenantId, RuleNodeId ruleNodeId);

    RuleChain getRootTenantRuleChain(TenantId tenantId);

    List<RuleNode> getRuleChainNodes(TenantId tenantId, RuleChainId ruleChainId);

    List<RuleNode> getReferencingRuleChainNodes(TenantId tenantId, RuleChainId ruleChainId);

    List<EntityRelation> getRuleNodeRelations(TenantId tenantId, RuleNodeId ruleNodeId);

    PageData<RuleChain> findTenantRuleChainsByType(TenantId tenantId, RuleChainType type, PageLink pageLink);

    Collection<RuleChain> findTenantRuleChainsByTypeAndName(TenantId tenantId, RuleChainType type, String name);

    void deleteRuleChainById(TenantId tenantId, RuleChainId ruleChainId);

    void deleteRuleChainsByTenantId(TenantId tenantId);

    RuleChainData exportTenantRuleChains(TenantId tenantId, PageLink pageLink) throws WinstarcloudException;

    List<RuleChainImportResult> importTenantRuleChains(TenantId tenantId, RuleChainData ruleChainData, boolean overwrite, Function<RuleNode, RuleNode> ruleNodeUpdater);

    RuleChain assignRuleChainToEdge(TenantId tenantId, RuleChainId ruleChainId, EdgeId edgeId);

    RuleChain unassignRuleChainFromEdge(TenantId tenantId, RuleChainId ruleChainId, EdgeId edgeId, boolean remove);

    PageData<RuleChain> findRuleChainsByTenantIdAndEdgeId(TenantId tenantId, EdgeId edgeId, PageLink pageLink);

    RuleChain getEdgeTemplateRootRuleChain(TenantId tenantId);

    boolean setEdgeTemplateRootRuleChain(TenantId tenantId, RuleChainId ruleChainId);

    boolean setAutoAssignToEdgeRuleChain(TenantId tenantId, RuleChainId ruleChainId);

    boolean unsetAutoAssignToEdgeRuleChain(TenantId tenantId, RuleChainId ruleChainId);

    PageData<RuleChain> findAutoAssignToEdgeRuleChainsByTenantId(TenantId tenantId, PageLink pageLink);

    List<RuleNode> findRuleNodesByTenantIdAndType(TenantId tenantId, String name, String toString);

    List<RuleNode> findRuleNodesByTenantIdAndType(TenantId tenantId, String type);

    PageData<RuleNode> findAllRuleNodesByType(String type, PageLink pageLink);

    @Deprecated(forRemoval = true, since = "3.6.3")
    PageData<RuleNode> findAllRuleNodesByTypeAndVersionLessThan(String type, int version, PageLink pageLink);

    PageData<RuleNodeId> findAllRuleNodeIdsByTypeAndVersionLessThan(String type, int version, PageLink pageLink);

    List<RuleNode> findAllRuleNodesByIds(List<RuleNodeId> ruleNodeIds);

    RuleNode saveRuleNode(TenantId tenantId, RuleNode ruleNode);

    void deleteRuleNodes(TenantId tenantId, RuleChainId ruleChainId);

}
