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
package org.winstarcloud.server.service.edge.rpc;

import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.msg.edge.EdgeSessionMsg;
import org.winstarcloud.server.common.msg.edge.FromEdgeSyncResponse;
import org.winstarcloud.server.common.msg.edge.ToEdgeSyncRequest;

import java.util.function.Consumer;

public interface EdgeRpcService {

    void onToEdgeSessionMsg(TenantId tenantId, EdgeSessionMsg msg);

    void updateEdge(TenantId tenantId, Edge edge);

    void deleteEdge(TenantId tenantId, EdgeId edgeId);

    void processSyncRequest(ToEdgeSyncRequest request, Consumer<FromEdgeSyncResponse> responseConsumer);
}
