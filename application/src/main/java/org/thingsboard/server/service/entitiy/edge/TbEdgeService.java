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
package org.winstarcloud.server.service.entitiy.edge;

import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.RuleChainId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.rule.RuleChain;

public interface TbEdgeService {
    Edge save(Edge edge, RuleChain edgeTemplateRootRuleChain, User user) throws Exception;

    void delete(Edge edge, User user);

    Edge assignEdgeToCustomer(TenantId tenantId, EdgeId edgeId, Customer customer, User user) throws WinstarcloudException;

    Edge unassignEdgeFromCustomer(Edge edge, Customer customer, User user) throws WinstarcloudException;

    Edge assignEdgeToPublicCustomer(TenantId tenantId, EdgeId edgeId, User user) throws WinstarcloudException;

    Edge setEdgeRootRuleChain(Edge edge, RuleChainId ruleChainId, User user) throws Exception;
}
