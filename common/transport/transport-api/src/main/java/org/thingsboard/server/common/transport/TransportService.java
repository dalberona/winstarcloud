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
package org.winstarcloud.server.common.transport;

import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.DeviceTransportType;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.plugin.ComponentLifecycleEvent;
import org.winstarcloud.server.common.data.rpc.RpcStatus;
import org.winstarcloud.server.common.msg.TbMsgMetaData;
import org.winstarcloud.server.common.transport.auth.GetOrCreateDeviceFromGatewayResponse;
import org.winstarcloud.server.common.transport.auth.ValidateDeviceCredentialsResponse;
import org.winstarcloud.server.common.transport.service.SessionMetaData;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.gen.transport.TransportProtos.ClaimDeviceMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetAttributeRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetDeviceCredentialsRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetDeviceCredentialsResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetDeviceRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetDeviceResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetEntityProfileRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetEntityProfileResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetOrCreateDeviceFromGatewayRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetOtaPackageRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetOtaPackageResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetResourceRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetResourceResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetSnmpDevicesRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.GetSnmpDevicesResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.LwM2MRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.LwM2MResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.PostAttributeMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.PostTelemetryMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ProvisionDeviceRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ProvisionDeviceResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.SessionEventMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.SessionInfoProto;
import org.winstarcloud.server.gen.transport.TransportProtos.SubscribeToAttributeUpdatesMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.SubscribeToRPCMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.SubscriptionInfoProto;
import org.winstarcloud.server.gen.transport.TransportProtos.ToDeviceRpcRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ToDeviceRpcResponseMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ToServerRpcRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.TransportToDeviceActorMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ValidateBasicMqttCredRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ValidateDeviceLwM2MCredentialsRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ValidateDeviceTokenRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ValidateDeviceX509CertRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ValidateOrCreateDeviceX509CertRequestMsg;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ashvayka on 04.10.18.
 */
public interface TransportService {

    GetEntityProfileResponseMsg getEntityProfile(GetEntityProfileRequestMsg msg);

    List<TransportProtos.GetQueueRoutingInfoResponseMsg> getQueueRoutingInfo(TransportProtos.GetAllQueueRoutingInfoRequestMsg msg);

    GetResourceResponseMsg getResource(GetResourceRequestMsg msg);

    GetSnmpDevicesResponseMsg getSnmpDevicesIds(GetSnmpDevicesRequestMsg requestMsg);

    GetDeviceResponseMsg getDevice(GetDeviceRequestMsg requestMsg);

    GetDeviceCredentialsResponseMsg getDeviceCredentials(GetDeviceCredentialsRequestMsg requestMsg);

    void process(DeviceTransportType transportType, ValidateDeviceTokenRequestMsg msg,
                 TransportServiceCallback<ValidateDeviceCredentialsResponse> callback);

    void process(DeviceTransportType transportType, ValidateBasicMqttCredRequestMsg msg,
                 TransportServiceCallback<ValidateDeviceCredentialsResponse> callback);

    void process(DeviceTransportType transportType, ValidateDeviceX509CertRequestMsg msg,
                 TransportServiceCallback<ValidateDeviceCredentialsResponse> callback);

    void process(DeviceTransportType transportType, ValidateOrCreateDeviceX509CertRequestMsg msg,
                 TransportServiceCallback<ValidateDeviceCredentialsResponse> callback);

    void process(ValidateDeviceLwM2MCredentialsRequestMsg msg,
                 TransportServiceCallback<ValidateDeviceCredentialsResponse> callback);

    void process(TenantId tenantId, GetOrCreateDeviceFromGatewayRequestMsg msg,
                 TransportServiceCallback<GetOrCreateDeviceFromGatewayResponse> callback);

    void process(ProvisionDeviceRequestMsg msg,
                 TransportServiceCallback<ProvisionDeviceResponseMsg> callback);

    void onProfileUpdate(DeviceProfile deviceProfile);

    void process(LwM2MRequestMsg msg,
                 TransportServiceCallback<LwM2MResponseMsg> callback);

    void process(SessionInfoProto sessionInfo, SessionEventMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, PostTelemetryMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, PostTelemetryMsg msg, TbMsgMetaData md, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, PostAttributeMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, PostAttributeMsg msg, TbMsgMetaData md, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, GetAttributeRequestMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, SubscribeToAttributeUpdatesMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, SubscribeToRPCMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ToDeviceRpcResponseMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ToServerRpcRequestMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ToDeviceRpcRequestMsg msg, RpcStatus rpcStatus, boolean reportActivity, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ToDeviceRpcRequestMsg msg, RpcStatus rpcStatus, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, SubscriptionInfoProto msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfo, ClaimDeviceMsg msg, TransportServiceCallback<Void> callback);

    void process(TransportToDeviceActorMsg msg, TransportServiceCallback<Void> callback);

    void process(SessionInfoProto sessionInfoProto, GetOtaPackageRequestMsg msg, TransportServiceCallback<GetOtaPackageResponseMsg> callback);

    SessionMetaData registerAsyncSession(SessionInfoProto sessionInfo, SessionMsgListener listener);

    SessionMetaData registerSyncSession(SessionInfoProto sessionInfo, SessionMsgListener listener, long timeout);

    void recordActivity(SessionInfoProto sessionInfo);

    void lifecycleEvent(TenantId tenantId, DeviceId deviceId, ComponentLifecycleEvent eventType, boolean success, Throwable error);

    void errorEvent(TenantId tenantId, DeviceId deviceId, String method, Throwable error);

    void deregisterSession(SessionInfoProto sessionInfo);

    void log(SessionInfoProto sessionInfo, String msg);

    void notifyAboutUplink(SessionInfoProto sessionInfo, TransportProtos.UplinkNotificationMsg build, TransportServiceCallback<Void> empty);

    ExecutorService getCallbackExecutor();

    boolean hasSession(SessionInfoProto sessionInfo);

    void createGaugeStats(String openConnections, AtomicInteger connectionsCounter);
}
