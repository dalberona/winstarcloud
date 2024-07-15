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
package org.winstarcloud.server.service.edge.rpc.constructor.widget;

import org.winstarcloud.server.common.data.id.WidgetTypeId;
import org.winstarcloud.server.common.data.id.WidgetsBundleId;
import org.winstarcloud.server.common.data.widget.WidgetTypeDetails;
import org.winstarcloud.server.common.data.widget.WidgetsBundle;
import org.winstarcloud.server.gen.edge.v1.EdgeVersion;
import org.winstarcloud.server.gen.edge.v1.UpdateMsgType;
import org.winstarcloud.server.gen.edge.v1.WidgetTypeUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.WidgetsBundleUpdateMsg;
import org.winstarcloud.server.service.edge.rpc.constructor.MsgConstructor;

import java.util.List;

public interface WidgetMsgConstructor extends MsgConstructor {

    WidgetsBundleUpdateMsg constructWidgetsBundleUpdateMsg(UpdateMsgType msgType, WidgetsBundle widgetsBundle, List<String> widgets);

    WidgetsBundleUpdateMsg constructWidgetsBundleDeleteMsg(WidgetsBundleId widgetsBundleId);

    WidgetTypeUpdateMsg constructWidgetTypeUpdateMsg(UpdateMsgType msgType, WidgetTypeDetails widgetTypeDetails, EdgeVersion edgeVersion);

    WidgetTypeUpdateMsg constructWidgetTypeDeleteMsg(WidgetTypeId widgetTypeId);
}
