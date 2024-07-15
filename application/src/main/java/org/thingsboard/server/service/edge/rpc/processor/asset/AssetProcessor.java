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
package org.winstarcloud.server.service.edge.rpc.processor.asset;

import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.edge.EdgeEvent;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.gen.edge.v1.AssetUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.DownlinkMsg;
import org.winstarcloud.server.gen.edge.v1.EdgeVersion;
import org.winstarcloud.server.service.edge.rpc.processor.EdgeProcessor;

public interface AssetProcessor extends EdgeProcessor {

    ListenableFuture<Void> processAssetMsgFromEdge(TenantId tenantId, Edge edge, AssetUpdateMsg assetUpdateMsg);

    DownlinkMsg convertAssetEventToDownlink(EdgeEvent edgeEvent, EdgeId edgeId, EdgeVersion edgeVersion);

}
