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
package org.winstarcloud.rule.engine.rpc;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.common.util.ListeningExecutor;
import org.winstarcloud.rule.engine.api.RuleEngineRpcService;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbNodeConfiguration;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.server.common.data.DataConstants;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.EntityIdFactory;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.msg.TbMsgType;
import org.winstarcloud.server.common.msg.TbMsg;
import org.winstarcloud.server.common.msg.TbMsgDataType;
import org.winstarcloud.server.common.msg.TbMsgMetaData;
import org.winstarcloud.server.dao.edge.EdgeEventService;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TbSendRPCReplyNodeTest {

    private static final String DUMMY_SERVICE_ID = "testServiceId";
    private static final int DUMMY_REQUEST_ID = 0;
    private static final UUID DUMMY_SESSION_ID = UUID.fromString("4f1d94aa-f6ee-4078-8499-b8e68443f8ad");
    private final String DUMMY_DATA = "{\"key\":\"value\"}";

    private TbSendRPCReplyNode node;
    private TbSendRpcReplyNodeConfiguration config;

    private final TenantId tenantId = TenantId.fromUUID(UUID.fromString("4e2e2336-3376-4238-ba0a-c669b412ca66"));
    private final DeviceId deviceId = new DeviceId(UUID.fromString("af64d1b9-8635-47e1-8738-6389df7fe57e"));

    @Mock
    private TbContext ctx;

    @Mock
    private RuleEngineRpcService rpcService;

    @Mock
    private EdgeEventService edgeEventService;

    @Mock
    private ListeningExecutor listeningExecutor;

    @BeforeEach
    public void setUp() throws TbNodeException {
        node = new TbSendRPCReplyNode();
        config = new TbSendRpcReplyNodeConfiguration().defaultConfiguration();
        node.init(ctx, new TbNodeConfiguration(JacksonUtil.valueToTree(config)));
    }

    @Test
    public void sendReplyToTransport() {
        when(ctx.getRpcService()).thenReturn(rpcService);

        TbMsg msg = TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, deviceId, getDefaultMetadata(),
                TbMsgDataType.JSON, DUMMY_DATA, null, null);

        node.onMsg(ctx, msg);

        verify(rpcService).sendRpcReplyToDevice(DUMMY_SERVICE_ID, DUMMY_SESSION_ID, DUMMY_REQUEST_ID, DUMMY_DATA);
        verify(edgeEventService, never()).saveAsync(any());
    }

    @Test
    public void sendReplyToEdgeQueue() {
        when(ctx.getTenantId()).thenReturn(tenantId);
        when(ctx.getEdgeEventService()).thenReturn(edgeEventService);
        when(edgeEventService.saveAsync(any())).thenReturn(SettableFuture.create());
        when(ctx.getDbCallbackExecutor()).thenReturn(listeningExecutor);

        TbMsgMetaData defaultMetadata = getDefaultMetadata();
        defaultMetadata.putValue(DataConstants.EDGE_ID, UUID.randomUUID().toString());
        defaultMetadata.putValue(DataConstants.DEVICE_ID, UUID.randomUUID().toString());
        TbMsg msg = TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, deviceId, defaultMetadata,
                TbMsgDataType.JSON, DUMMY_DATA, null, null);

        node.onMsg(ctx, msg);

        verify(edgeEventService).saveAsync(any());
        verify(rpcService, never()).sendRpcReplyToDevice(DUMMY_SERVICE_ID, DUMMY_SESSION_ID, DUMMY_REQUEST_ID, DUMMY_DATA);
    }

    @ParameterizedTest
    @EnumSource(EntityType.class)
    public void testOriginatorEntityTypes(EntityType entityType) {
        EntityId entityId = EntityIdFactory.getByTypeAndUuid(entityType, "0f386739-210f-4e23-8739-23f84a172adc");
        TbMsg msg = TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, entityId, TbMsgMetaData.EMPTY, TbMsg.EMPTY_JSON_OBJECT);

        node.onMsg(ctx, msg);

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(ctx).tellFailure(eq(msg), throwableCaptor.capture());
        assertThat(throwableCaptor.getValue()).isInstanceOf(RuntimeException.class)
                .hasMessage(EntityType.DEVICE != entityType ? "Message originator is not a device entity!"
                        : "Request id is not present in the metadata!");
    }

    @ParameterizedTest
    @MethodSource
    public void testForAvailabilityOfMetadataAndDataValues(TbMsgMetaData metaData, String errorMsg) {
        TbMsg msg = TbMsg.newMsg(TbMsgType.POST_TELEMETRY_REQUEST, deviceId, metaData, TbMsg.EMPTY_STRING);

        node.onMsg(ctx, msg);

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(ctx).tellFailure(eq(msg), throwableCaptor.capture());
        assertThat(throwableCaptor.getValue()).isInstanceOf(RuntimeException.class).hasMessage(errorMsg);
    }

    @Test
    public void verifyDefaultConfig() {
        assertThat(config.getServiceIdMetaDataAttribute()).isEqualTo("serviceId");
        assertThat(config.getSessionIdMetaDataAttribute()).isEqualTo("sessionId");
        assertThat(config.getRequestIdMetaDataAttribute()).isEqualTo("requestId");
    }

    private static Stream<Arguments> testForAvailabilityOfMetadataAndDataValues() {
        return Stream.of(
                Arguments.of(TbMsgMetaData.EMPTY, "Request id is not present in the metadata!"),
                Arguments.of(new TbMsgMetaData(Map.of(
                        "requestId", Integer.toString(DUMMY_REQUEST_ID))), "Service id is not present in the metadata!"),
                Arguments.of(new TbMsgMetaData(Map.of(
                        "requestId", Integer.toString(DUMMY_REQUEST_ID),
                        "serviceId", DUMMY_SERVICE_ID)), "Session id is not present in the metadata!"),
                Arguments.of(new TbMsgMetaData(Map.of(
                        "requestId", Integer.toString(DUMMY_REQUEST_ID),
                        "serviceId", DUMMY_SERVICE_ID, "sessionId",
                        DUMMY_SESSION_ID.toString())), "Request body is empty!")
        );
    }

    private TbMsgMetaData getDefaultMetadata() {
        TbSendRpcReplyNodeConfiguration config = new TbSendRpcReplyNodeConfiguration().defaultConfiguration();
        TbMsgMetaData metadata = new TbMsgMetaData();
        metadata.putValue(config.getServiceIdMetaDataAttribute(), DUMMY_SERVICE_ID);
        metadata.putValue(config.getSessionIdMetaDataAttribute(), DUMMY_SESSION_ID.toString());
        metadata.putValue(config.getRequestIdMetaDataAttribute(), Integer.toString(DUMMY_REQUEST_ID));
        return metadata;
    }
}
