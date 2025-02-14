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
package org.winstarcloud.server.service.edge.rpc.processor.widget;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.common.data.EdgeUtils;
import org.winstarcloud.server.common.data.edge.EdgeEvent;
import org.winstarcloud.server.common.data.id.WidgetsBundleId;
import org.winstarcloud.server.common.data.widget.WidgetsBundle;
import org.winstarcloud.server.gen.edge.v1.DownlinkMsg;
import org.winstarcloud.server.gen.edge.v1.EdgeVersion;
import org.winstarcloud.server.gen.edge.v1.UpdateMsgType;
import org.winstarcloud.server.gen.edge.v1.WidgetsBundleUpdateMsg;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.edge.rpc.constructor.widget.WidgetMsgConstructor;
import org.winstarcloud.server.service.edge.rpc.processor.BaseEdgeProcessor;

import java.util.List;

@Component
@Slf4j
@TbCoreComponent
public class WidgetBundleEdgeProcessor extends BaseEdgeProcessor {

    public DownlinkMsg convertWidgetsBundleEventToDownlink(EdgeEvent edgeEvent, EdgeVersion edgeVersion) {
        WidgetsBundleId widgetsBundleId = new WidgetsBundleId(edgeEvent.getEntityId());
        DownlinkMsg downlinkMsg = null;
        switch (edgeEvent.getAction()) {
            case ADDED, UPDATED -> {
                WidgetsBundle widgetsBundle = widgetsBundleService.findWidgetsBundleById(edgeEvent.getTenantId(), widgetsBundleId);
                if (widgetsBundle != null) {
                    List<String> widgets = widgetTypeService.findWidgetFqnsByWidgetsBundleId(edgeEvent.getTenantId(), widgetsBundleId);
                    UpdateMsgType msgType = getUpdateMsgType(edgeEvent.getAction());
                    WidgetsBundleUpdateMsg widgetsBundleUpdateMsg =
                            ((WidgetMsgConstructor) widgetMsgConstructorFactory.getMsgConstructorByEdgeVersion(edgeVersion)).constructWidgetsBundleUpdateMsg(msgType, widgetsBundle, widgets);
                    downlinkMsg = DownlinkMsg.newBuilder()
                            .setDownlinkMsgId(EdgeUtils.nextPositiveInt())
                            .addWidgetsBundleUpdateMsg(widgetsBundleUpdateMsg)
                            .build();
                }
            }
            case DELETED -> {
                WidgetsBundleUpdateMsg widgetsBundleUpdateMsg =
                        ((WidgetMsgConstructor) widgetMsgConstructorFactory.getMsgConstructorByEdgeVersion(edgeVersion)).constructWidgetsBundleDeleteMsg(widgetsBundleId);
                downlinkMsg = DownlinkMsg.newBuilder()
                        .setDownlinkMsgId(EdgeUtils.nextPositiveInt())
                        .addWidgetsBundleUpdateMsg(widgetsBundleUpdateMsg)
                        .build();
            }
        }
        return downlinkMsg;
    }

}
