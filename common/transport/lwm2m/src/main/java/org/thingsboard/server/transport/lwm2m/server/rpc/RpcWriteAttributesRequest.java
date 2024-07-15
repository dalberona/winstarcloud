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

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.winstarcloud.server.common.data.device.profile.lwm2m.ObjectAttributes;

@Data
@EqualsAndHashCode(callSuper = true)
public class RpcWriteAttributesRequest extends LwM2MRpcRequestHeader {

    private ObjectAttributes attributes;

}
