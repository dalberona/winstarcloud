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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbNode;
import org.winstarcloud.rule.engine.api.TbNodeConfiguration;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.rule.engine.api.util.TbNodeUtils;
import org.winstarcloud.server.common.data.DataConstants;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.alarm.Alarm;
import org.winstarcloud.server.common.data.edge.EdgeEventActionType;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.msg.TbMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.winstarcloud.server.common.data.msg.TbMsgType.ACTIVITY_EVENT;
import static org.winstarcloud.server.common.data.msg.TbMsgType.ALARM;
import static org.winstarcloud.server.common.data.msg.TbMsgType.ATTRIBUTES_DELETED;
import static org.winstarcloud.server.common.data.msg.TbMsgType.ATTRIBUTES_UPDATED;
import static org.winstarcloud.server.common.data.msg.TbMsgType.CONNECT_EVENT;
import static org.winstarcloud.server.common.data.msg.TbMsgType.DISCONNECT_EVENT;
import static org.winstarcloud.server.common.data.msg.TbMsgType.INACTIVITY_EVENT;
import static org.winstarcloud.server.common.data.msg.TbMsgType.POST_ATTRIBUTES_REQUEST;
import static org.winstarcloud.server.common.data.msg.TbMsgType.POST_TELEMETRY_REQUEST;
import static org.winstarcloud.server.common.data.msg.TbMsgType.TIMESERIES_UPDATED;
import static org.winstarcloud.server.common.data.msg.TbMsgType.TO_SERVER_RPC_REQUEST;

@Slf4j
public abstract class AbstractTbMsgPushNode<T extends BaseTbMsgPushNodeConfiguration, S, U> implements TbNode {

    protected T config;

    private static final String SCOPE = "scope";

    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, getConfigClazz());
    }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) {
        if (getIgnoredMessageSource().equalsIgnoreCase(msg.getMetaData().getValue(DataConstants.MSG_SOURCE_KEY))) {
            log.debug("Ignoring msg from the {}, msg [{}]", getIgnoredMessageSource(), msg);
            ctx.ack(msg);
            return;
        }
        if (isSupportedMsgType(msg)) {
            processMsg(ctx, msg);
        } else {
            String errMsg = String.format("Unsupported msg type %s", msg.getType());
            log.debug(errMsg);
            ctx.tellFailure(msg, new RuntimeException(errMsg));
        }
    }

    protected S buildEvent(TbMsg msg, TbContext ctx) {
        if (msg.isTypeOf(ALARM)) {
            EdgeEventActionType actionType = getAlarmActionType(msg);
            return buildEvent(ctx.getTenantId(), actionType, getUUIDFromMsgData(msg), getAlarmEventType(), null);
        } else {
            Map<String, String> metadata = msg.getMetaData().getData();
            EdgeEventActionType actionType = getEdgeEventActionTypeByMsgType(msg);
            Map<String, Object> entityBody = new HashMap<>();
            JsonNode dataJson = JacksonUtil.toJsonNode(msg.getData());
            switch (actionType) {
                case ATTRIBUTES_UPDATED, POST_ATTRIBUTES -> {
                    entityBody.put("kv", dataJson);
                    entityBody.put(SCOPE, getScope(metadata));
                    if (EdgeEventActionType.POST_ATTRIBUTES.equals(actionType)) {
                        entityBody.put("isPostAttributes", true);
                    }
                }
                case ATTRIBUTES_DELETED -> {
                    List<String> keys = JacksonUtil.convertValue(dataJson.get("attributes"), new TypeReference<>() {
                    });
                    entityBody.put("keys", keys);
                    entityBody.put(SCOPE, getScope(metadata));
                }
                case TIMESERIES_UPDATED -> {
                    entityBody.put("data", dataJson);
                    entityBody.put("ts", msg.getMetaDataTs());
                }
            }
            return buildEvent(ctx.getTenantId(),
                    actionType,
                    msg.getOriginator().getId(),
                    getEventTypeByEntityType(msg.getOriginator().getEntityType()),
                    JacksonUtil.valueToTree(entityBody));
        }
    }

    private static EdgeEventActionType getAlarmActionType(TbMsg msg) {
        boolean isNewAlarm = Boolean.parseBoolean(msg.getMetaData().getValue(DataConstants.IS_NEW_ALARM));
        boolean isClearedAlarm = Boolean.parseBoolean(msg.getMetaData().getValue(DataConstants.IS_CLEARED_ALARM));
        EdgeEventActionType eventAction;
        if (isNewAlarm) {
            eventAction = EdgeEventActionType.ADDED;
        } else if (isClearedAlarm) {
            eventAction = EdgeEventActionType.ALARM_CLEAR;
        } else {
            eventAction = EdgeEventActionType.UPDATED;
        }
        return eventAction;
    }

    abstract S buildEvent(TenantId tenantId, EdgeEventActionType eventAction, UUID entityId, U eventType, JsonNode entityBody);

    abstract U getEventTypeByEntityType(EntityType entityType);

    abstract U getAlarmEventType();

    abstract String getIgnoredMessageSource();

    abstract protected Class<T> getConfigClazz();

    abstract void processMsg(TbContext ctx, TbMsg msg);

    protected UUID getUUIDFromMsgData(TbMsg msg) {
        Alarm alarm = JacksonUtil.fromString(msg.getData(), Alarm.class);
        return alarm != null ? alarm.getUuidId() : null;
    }

    protected String getScope(Map<String, String> metadata) {
        String scope = metadata.get(SCOPE);
        if (StringUtils.isEmpty(scope)) {
            scope = config.getScope();
        }
        return scope;
    }

    protected EdgeEventActionType getEdgeEventActionTypeByMsgType(TbMsg msg) {
        EdgeEventActionType actionType;
        if (msg.isTypeOneOf(POST_TELEMETRY_REQUEST, TIMESERIES_UPDATED)) {
            actionType = EdgeEventActionType.TIMESERIES_UPDATED;
        } else if (msg.isTypeOf(ATTRIBUTES_UPDATED)) {
            actionType = EdgeEventActionType.ATTRIBUTES_UPDATED;
        } else if (msg.isTypeOf(POST_ATTRIBUTES_REQUEST)) {
            actionType = EdgeEventActionType.POST_ATTRIBUTES;
        } else if (msg.isTypeOf(ATTRIBUTES_DELETED)) {
            actionType = EdgeEventActionType.ATTRIBUTES_DELETED;
        } else if (msg.isTypeOneOf(CONNECT_EVENT, DISCONNECT_EVENT, ACTIVITY_EVENT, INACTIVITY_EVENT)) {
            String scope = msg.getMetaData().getValue(SCOPE);
            actionType = StringUtils.isEmpty(scope) ?
                    EdgeEventActionType.TIMESERIES_UPDATED : EdgeEventActionType.ATTRIBUTES_UPDATED;
        } else {
            String type = msg.getType();
            log.warn("Unsupported msg type [{}]", type);
            throw new IllegalArgumentException("Unsupported msg type: " + type);
        }
        return actionType;
    }

    protected boolean isSupportedMsgType(TbMsg msg) {
        return msg.isTypeOneOf(POST_TELEMETRY_REQUEST, POST_ATTRIBUTES_REQUEST, ATTRIBUTES_UPDATED, ATTRIBUTES_DELETED, TIMESERIES_UPDATED,
                ALARM, CONNECT_EVENT, DISCONNECT_EVENT, ACTIVITY_EVENT, INACTIVITY_EVENT, TO_SERVER_RPC_REQUEST);
    }

}
