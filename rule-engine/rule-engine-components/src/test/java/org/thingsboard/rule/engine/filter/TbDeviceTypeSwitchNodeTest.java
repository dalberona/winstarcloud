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
package org.winstarcloud.rule.engine.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.rule.engine.api.EmptyNodeConfiguration;
import org.winstarcloud.rule.engine.api.RuleEngineDeviceProfileCache;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbNodeConfiguration;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.msg.TbMsgType;
import org.winstarcloud.server.common.msg.TbMsg;
import org.winstarcloud.server.common.msg.TbMsgMetaData;
import org.winstarcloud.server.common.msg.queue.TbMsgCallback;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TbDeviceTypeSwitchNodeTest {

    private DeviceId deviceId;
    private DeviceId deviceIdDeleted;
    private TbContext ctx;
    private TbDeviceTypeSwitchNode node;
    private TbMsgCallback callback;

    @BeforeEach
    void setUp() throws TbNodeException {
        TenantId tenantId = new TenantId(UUID.randomUUID());
        deviceId = new DeviceId(UUID.randomUUID());
        deviceIdDeleted = new DeviceId(UUID.randomUUID());

        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setTenantId(tenantId);
        deviceProfile.setName("TestDeviceProfile");

        //node
        EmptyNodeConfiguration config = new EmptyNodeConfiguration();
        node = new TbDeviceTypeSwitchNode();
        node.init(ctx, new TbNodeConfiguration(JacksonUtil.valueToTree(config)));

        //init mock
        ctx = mock(TbContext.class);
        RuleEngineDeviceProfileCache deviceProfileCache = mock(RuleEngineDeviceProfileCache.class);
        callback = mock(TbMsgCallback.class);

        when(ctx.getTenantId()).thenReturn(tenantId);
        when(ctx.getDeviceProfileCache()).thenReturn(deviceProfileCache);

        doReturn(deviceProfile).when(deviceProfileCache).get(tenantId, deviceId);
        doReturn(null).when(deviceProfileCache).get(tenantId, deviceIdDeleted);
    }

    @AfterEach
    void tearDown() {
        node.destroy();
    }

    @Test
    void givenMsg_whenOnMsg_then_Fail() {
        CustomerId customerId = new CustomerId(UUID.randomUUID());
        assertThatThrownBy(() -> {
            node.onMsg(ctx, getTbMsg(customerId));
        }).isInstanceOf(TbNodeException.class).hasMessageContaining("Unsupported originator type");
    }

    @Test
    void givenMsg_whenOnMsg_EntityIdDeleted_then_Fail() {
        assertThatThrownBy(() -> {
            node.onMsg(ctx, getTbMsg(deviceIdDeleted));
        }).isInstanceOf(TbNodeException.class).hasMessageContaining("Device profile for entity id");
    }

    @Test
    void givenMsg_whenOnMsg_then_Success() throws TbNodeException {
        TbMsg msg = getTbMsg(deviceId);
        node.onMsg(ctx, msg);

        ArgumentCaptor<TbMsg> newMsgCaptor = ArgumentCaptor.forClass(TbMsg.class);
        verify(ctx, times(1)).tellNext(newMsgCaptor.capture(), eq("TestDeviceProfile"));
        verify(ctx, never()).tellFailure(any(), any());

        TbMsg newMsg = newMsgCaptor.getValue();
        assertThat(newMsg).isNotNull();
        assertThat(newMsg).isSameAs(msg);
    }

    private TbMsg getTbMsg(EntityId entityId) {
        return TbMsg.newMsg(TbMsgType.POST_ATTRIBUTES_REQUEST, entityId, TbMsgMetaData.EMPTY, TbMsg.EMPTY_JSON_OBJECT, callback);
    }
}
