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
package org.winstarcloud.server.transport.coap.client;

import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.adaptor.AdaptorException;
import org.winstarcloud.server.common.transport.auth.ValidateDeviceCredentialsResponse;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.transport.coap.CoapSessionMsgType;

import java.util.concurrent.atomic.AtomicInteger;

public interface CoapClientContext {

    boolean registerAttributeObservation(TbCoapClientState clientState, String token, CoapExchange exchange);

    boolean registerRpcObservation(TbCoapClientState clientState, String token, CoapExchange exchange);

    AtomicInteger getNotificationCounterByToken(String token);

    TbCoapClientState getOrCreateClient(CoapSessionMsgType type, ValidateDeviceCredentialsResponse deviceCredentials, DeviceProfile deviceProfile) throws AdaptorException;

    TransportProtos.SessionInfoProto getNewSyncSession(TbCoapClientState clientState);

    void deregisterAttributeObservation(TbCoapClientState clientState, String token, CoapExchange exchange);

    void deregisterRpcObservation(TbCoapClientState clientState, String token, CoapExchange exchange);

    void reportActivity();

    void registerObserveRelation(String token, ObserveRelation relation);

    void deregisterObserveRelation(String token);

    boolean awake(TbCoapClientState client);
}
