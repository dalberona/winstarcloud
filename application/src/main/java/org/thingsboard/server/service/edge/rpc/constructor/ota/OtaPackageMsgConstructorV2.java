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
package org.winstarcloud.server.service.edge.rpc.constructor.ota;

import org.springframework.stereotype.Component;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.OtaPackage;
import org.winstarcloud.server.gen.edge.v1.OtaPackageUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.UpdateMsgType;
import org.winstarcloud.server.queue.util.TbCoreComponent;

@Component
@TbCoreComponent
public class OtaPackageMsgConstructorV2 extends BaseOtaPackageMsgConstructor {

    @Override
    public OtaPackageUpdateMsg constructOtaPackageUpdatedMsg(UpdateMsgType msgType, OtaPackage otaPackage) {
        return OtaPackageUpdateMsg.newBuilder().setMsgType(msgType).setEntity(JacksonUtil.toString(otaPackage))
                .setIdMSB(otaPackage.getId().getId().getMostSignificantBits())
                .setIdLSB(otaPackage.getId().getId().getLeastSignificantBits()).build();
    }
}
