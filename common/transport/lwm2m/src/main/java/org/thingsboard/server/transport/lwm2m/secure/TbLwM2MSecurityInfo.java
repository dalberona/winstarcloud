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
package org.winstarcloud.server.transport.lwm2m.secure;

import lombok.Data;
import org.eclipse.leshan.core.SecurityMode;
import org.eclipse.leshan.server.bootstrap.BootstrapConfig;
import org.eclipse.leshan.server.security.SecurityInfo;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.transport.auth.ValidateDeviceCredentialsResponse;
import org.winstarcloud.server.transport.lwm2m.bootstrap.secure.LwM2MBootstrapConfig;

import java.io.Serializable;

@Data
public class TbLwM2MSecurityInfo implements Serializable {
    private ValidateDeviceCredentialsResponse msg;
    private DeviceProfile deviceProfile;
    private String endpoint;
    private SecurityInfo securityInfo;
    private SecurityMode securityMode;


    /** bootstrap */
    private LwM2MBootstrapConfig bootstrapCredentialConfig;
    private BootstrapConfig bootstrapConfig;
}
