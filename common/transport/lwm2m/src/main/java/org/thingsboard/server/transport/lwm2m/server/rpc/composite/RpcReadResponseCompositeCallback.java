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
package org.winstarcloud.server.transport.lwm2m.server.rpc.composite;

import org.eclipse.leshan.core.request.LwM2mRequest;
import org.eclipse.leshan.core.response.ReadCompositeResponse;
import org.winstarcloud.server.common.transport.TransportService;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.transport.lwm2m.server.client.LwM2mClient;
import org.winstarcloud.server.transport.lwm2m.server.downlink.DownlinkRequestCallback;
import org.winstarcloud.server.transport.lwm2m.server.rpc.RpcLwM2MDownlinkCallback;

import java.util.Optional;

import static org.winstarcloud.server.transport.lwm2m.utils.LwM2MTransportUtil.contentToString;

public class RpcReadResponseCompositeCallback<R extends LwM2mRequest<T>, T extends ReadCompositeResponse> extends RpcLwM2MDownlinkCallback<R, T> {

    public RpcReadResponseCompositeCallback(TransportService transportService, LwM2mClient client, TransportProtos.ToDeviceRpcRequestMsg requestMsg, DownlinkRequestCallback<R, T> callback) {
        super(transportService, client, requestMsg, callback);
    }

    @Override
    protected Optional<String> serializeSuccessfulResponse(T response) {
        return contentToString(response.getContent());
    }
}
