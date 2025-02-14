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
package org.winstarcloud.rule.engine.edge;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.common.util.ListeningExecutor;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbNodeConfiguration;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.server.common.data.DataConstants;
import org.winstarcloud.server.common.data.edge.EdgeEvent;
import org.winstarcloud.server.common.data.edge.EdgeEventActionType;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.msg.TbMsgType;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.msg.TbMsg;
import org.winstarcloud.server.common.msg.TbMsgDataType;
import org.winstarcloud.server.common.msg.TbMsgMetaData;
import org.winstarcloud.server.dao.edge.EdgeEventService;
import org.winstarcloud.server.dao.edge.EdgeService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TbMsgPushToEdgeNodeTest {

    private static final List<TbMsgType> MISC_EVENTS = List.of(TbMsgType.CONNECT_EVENT, TbMsgType.DISCONNECT_EVENT,
            TbMsgType.ACTIVITY_EVENT, TbMsgType.INACTIVITY_EVENT);

    TbMsgPushToEdgeNode node;

    private final TenantId tenantId = TenantId.fromUUID(UUID.randomUUID());
    private final DeviceId deviceId = new DeviceId(UUID.randomUUID());

    @Mock
    private TbContext ctx;

    @Mock
    private EdgeService edgeService;
    @Mock
    private EdgeEventService edgeEventService;
    @Mock
    private ListeningExecutor dbCallbackExecutor;

    @BeforeEach
    public void setUp() throws TbNodeException {
        node = new TbMsgPushToEdgeNode();
        TbMsgPushToEdgeNodeConfiguration config = new TbMsgPushToEdgeNodeConfiguration().defaultConfiguration();
        node.init(ctx, new TbNodeConfiguration(JacksonUtil.valueToTree(config)));
    }

    @Test
    public void ackMsgInCaseNoEdgeRelated() {
        Mockito.when(ctx.getTenantId()).thenReturn(tenantId);
        Mockito.when(ctx.getEdgeService()).thenReturn(edgeService);
        Mockito.when(edgeService.findRelatedEdgeIdsByEntityId(tenantId, deviceId, new PageLink(TbMsgPushToEdgeNode.DEFAULT_PAGE_SIZE))).thenReturn(new PageData<>());

        TbMsg msg = TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, deviceId, TbMsgMetaData.EMPTY,
                TbMsgDataType.JSON, TbMsg.EMPTY_JSON_OBJECT, null, null);

        node.onMsg(ctx, msg);

        verify(ctx).ack(msg);
    }

    @Test
    public void testAttributeUpdateMsg_userEntity() {
        Mockito.when(ctx.getTenantId()).thenReturn(tenantId);
        Mockito.when(ctx.getEdgeService()).thenReturn(edgeService);
        Mockito.when(ctx.getEdgeEventService()).thenReturn(edgeEventService);
        Mockito.when(ctx.getDbCallbackExecutor()).thenReturn(dbCallbackExecutor);
        Mockito.when(edgeEventService.saveAsync(any())).thenReturn(SettableFuture.create());

        UserId userId = new UserId(UUID.randomUUID());
        EdgeId edgeId = new EdgeId(UUID.randomUUID());
        PageData<EdgeId> edgePageData = new PageData<>(List.of(edgeId), 1, 1, false);
        Mockito.when(edgeService.findRelatedEdgeIdsByEntityId(tenantId, userId, new PageLink(TbMsgPushToEdgeNode.DEFAULT_PAGE_SIZE))).thenReturn(edgePageData);

        TbMsg msg = TbMsg.newMsg(TbMsgType.ATTRIBUTES_UPDATED, userId, TbMsgMetaData.EMPTY,
                TbMsgDataType.JSON, TbMsg.EMPTY_JSON_OBJECT, null, null);

        node.onMsg(ctx, msg);

        verify(edgeEventService).saveAsync(any());
    }

    @Test
    public void testMiscEventsProcessedAsAttributesUpdated() {
        for (var event : MISC_EVENTS) {
            TbMsgMetaData metaData = new TbMsgMetaData();
            metaData.putValue(DataConstants.SCOPE, DataConstants.SERVER_SCOPE);
            testEvent(event, metaData, EdgeEventActionType.ATTRIBUTES_UPDATED, "kv");
        }
    }

    @Test
    public void testMiscEventsProcessedAsTimeseriesUpdated() {
        for (var event : MISC_EVENTS) {
            testEvent(event, TbMsgMetaData.EMPTY, EdgeEventActionType.TIMESERIES_UPDATED, "data");
        }
    }

    private void testEvent(TbMsgType event, TbMsgMetaData metaData, EdgeEventActionType expectedType, String dataKey) {
        Mockito.when(ctx.getTenantId()).thenReturn(tenantId);
        Mockito.when(ctx.getEdgeService()).thenReturn(edgeService);
        Mockito.when(ctx.getEdgeEventService()).thenReturn(edgeEventService);
        Mockito.when(ctx.getDbCallbackExecutor()).thenReturn(dbCallbackExecutor);
        Mockito.when(edgeEventService.saveAsync(any())).thenReturn(SettableFuture.create());

        TbMsg msg = TbMsg.newMsg(event, new EdgeId(UUID.randomUUID()), metaData,
                TbMsgDataType.JSON, "{\"lastConnectTs\":1}", null, null);

        node.onMsg(ctx, msg);

        ArgumentMatcher<EdgeEvent> eventArgumentMatcher = edgeEvent ->
                edgeEvent.getAction().equals(expectedType)
                        && edgeEvent.getBody().get(dataKey).get("lastConnectTs").asInt() == 1;
        verify(edgeEventService).saveAsync(Mockito.argThat(eventArgumentMatcher));

        Mockito.reset(ctx, edgeEventService);
    }
}
