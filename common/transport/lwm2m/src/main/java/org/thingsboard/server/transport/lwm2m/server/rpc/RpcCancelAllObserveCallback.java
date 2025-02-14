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
package org.winstarcloud.server.transport.lwm2m.server.rpc;

import org.eclipse.leshan.core.ResponseCode;
import org.winstarcloud.server.common.transport.TransportService;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.transport.lwm2m.server.client.LwM2mClient;
import org.winstarcloud.server.transport.lwm2m.server.downlink.DownlinkRequestCallback;
import org.winstarcloud.server.transport.lwm2m.server.downlink.TbLwM2MCancelAllRequest;

public class RpcCancelAllObserveCallback extends RpcDownlinkRequestCallbackProxy<TbLwM2MCancelAllRequest, Integer> {

    public RpcCancelAllObserveCallback(TransportService transportService, LwM2mClient client, TransportProtos.ToDeviceRpcRequestMsg requestMsg, DownlinkRequestCallback<TbLwM2MCancelAllRequest, Integer> callback) {
        super(transportService, client, requestMsg, callback);
    }

    @Override
    protected void sendRpcReplyOnSuccess(Integer response) {
        reply(LwM2MRpcResponseBody.builder().result(ResponseCode.CONTENT.getName()).value(response.toString()).build());
    }
}
