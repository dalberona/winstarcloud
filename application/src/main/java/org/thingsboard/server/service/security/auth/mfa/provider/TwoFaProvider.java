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
package org.winstarcloud.server.service.security.auth.mfa.provider;

import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.security.model.mfa.account.TwoFaAccountConfig;
import org.winstarcloud.server.common.data.security.model.mfa.provider.TwoFaProviderConfig;
import org.winstarcloud.server.common.data.security.model.mfa.provider.TwoFaProviderType;
import org.winstarcloud.server.service.security.model.SecurityUser;

public interface TwoFaProvider<C extends TwoFaProviderConfig, A extends TwoFaAccountConfig> {

    A generateNewAccountConfig(User user, C providerConfig);

    default void prepareVerificationCode(SecurityUser user, C providerConfig, A accountConfig) throws WinstarcloudException {}

    boolean checkVerificationCode(SecurityUser user, String code, C providerConfig, A accountConfig);

    default void check(TenantId tenantId) throws WinstarcloudException {};


    TwoFaProviderType getType();

}
