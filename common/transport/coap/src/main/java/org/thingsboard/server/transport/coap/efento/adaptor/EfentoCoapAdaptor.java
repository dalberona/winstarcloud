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
package org.winstarcloud.server.transport.coap.efento.adaptor;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.common.adaptor.AdaptorException;
import org.winstarcloud.server.common.adaptor.JsonConverter;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.transport.coap.efento.CoapEfentoTransportResource;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class EfentoCoapAdaptor {

    private static final Gson gson = new Gson();

    public TransportProtos.PostTelemetryMsg convertToPostTelemetry(UUID sessionId, List<CoapEfentoTransportResource.EfentoTelemetry> telemetryList) throws AdaptorException {
        try {
            return JsonConverter.convertToTelemetryProto(gson.toJsonTree(telemetryList));
        } catch (Exception ex) {
            log.warn("[{}] Failed to convert EfentoMeasurements to PostTelemetry request!", sessionId);
            throw new AdaptorException(ex);
        }
    }

    public TransportProtos.PostAttributeMsg convertToPostAttributes(UUID sessionId, JsonElement deviceInfo) throws AdaptorException {
        try {
            return JsonConverter.convertToAttributesProto(deviceInfo);
        } catch (Exception ex) {
            log.warn("[{}] Failed to convert JsonObject to PostTelemetry request!", sessionId);
            throw new AdaptorException(ex);
        }
    }


}
