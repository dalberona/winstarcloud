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
package org.winstarcloud.server.service.edge.rpc.processor.settings;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.AdminSettings;
import org.winstarcloud.server.common.data.EdgeUtils;
import org.winstarcloud.server.common.data.edge.EdgeEvent;
import org.winstarcloud.server.gen.edge.v1.AdminSettingsUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.DownlinkMsg;
import org.winstarcloud.server.gen.edge.v1.EdgeVersion;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.edge.rpc.constructor.settings.AdminSettingsMsgConstructor;
import org.winstarcloud.server.service.edge.rpc.processor.BaseEdgeProcessor;

@Slf4j
@Component
@TbCoreComponent
public class AdminSettingsEdgeProcessor extends BaseEdgeProcessor {

    public DownlinkMsg convertAdminSettingsEventToDownlink(EdgeEvent edgeEvent, EdgeVersion edgeVersion) {
        AdminSettings adminSettings = JacksonUtil.convertValue(edgeEvent.getBody(), AdminSettings.class);
        if (adminSettings == null) {
            return null;
        }
        AdminSettingsUpdateMsg adminSettingsUpdateMsg = ((AdminSettingsMsgConstructor)
                adminSettingsMsgConstructorFactory.getMsgConstructorByEdgeVersion(edgeVersion)).constructAdminSettingsUpdateMsg(adminSettings);
        return DownlinkMsg.newBuilder()
                .setDownlinkMsgId(EdgeUtils.nextPositiveInt())
                .addAdminSettingsUpdateMsg(adminSettingsUpdateMsg)
                .build();
    }

}
