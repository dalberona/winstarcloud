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
package org.winstarcloud.server.service.edge.rpc.constructor.tenant;

import org.springframework.stereotype.Component;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.Tenant;
import org.winstarcloud.server.common.data.TenantProfile;
import org.winstarcloud.server.gen.edge.v1.EdgeVersion;
import org.winstarcloud.server.gen.edge.v1.TenantProfileUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.TenantUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.UpdateMsgType;
import org.winstarcloud.server.queue.util.TbCoreComponent;

@Component
@TbCoreComponent
public class TenantMsgConstructorV2 implements TenantMsgConstructor {

    @Override
    public TenantUpdateMsg constructTenantUpdateMsg(UpdateMsgType msgType, Tenant tenant) {
        return TenantUpdateMsg.newBuilder().setMsgType(msgType).setEntity(JacksonUtil.toString(tenant)).build();
    }

    @Override
    public TenantProfileUpdateMsg constructTenantProfileUpdateMsg(UpdateMsgType msgType, TenantProfile tenantProfile, EdgeVersion edgeVersion) {
        return TenantProfileUpdateMsg.newBuilder().setMsgType(msgType).setEntity(JacksonUtil.toString(tenantProfile)).build();
    }

}
