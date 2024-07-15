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
package org.winstarcloud.server.service.security.auth.mfa.config;

import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.security.model.mfa.PlatformTwoFaSettings;
import org.winstarcloud.server.common.data.security.model.mfa.account.AccountTwoFaSettings;
import org.winstarcloud.server.common.data.security.model.mfa.account.TwoFaAccountConfig;
import org.winstarcloud.server.common.data.security.model.mfa.provider.TwoFaProviderType;

import java.util.Optional;

public interface TwoFaConfigManager {

    Optional<AccountTwoFaSettings> getAccountTwoFaSettings(TenantId tenantId, UserId userId);


    Optional<TwoFaAccountConfig> getTwoFaAccountConfig(TenantId tenantId, UserId userId, TwoFaProviderType providerType);

    AccountTwoFaSettings saveTwoFaAccountConfig(TenantId tenantId, UserId userId, TwoFaAccountConfig accountConfig);

    AccountTwoFaSettings deleteTwoFaAccountConfig(TenantId tenantId, UserId userId, TwoFaProviderType providerType);


    Optional<PlatformTwoFaSettings> getPlatformTwoFaSettings(TenantId tenantId, boolean sysadminSettingsAsDefault);

    PlatformTwoFaSettings savePlatformTwoFaSettings(TenantId tenantId, PlatformTwoFaSettings twoFactorAuthSettings) throws WinstarcloudException;

    void deletePlatformTwoFaSettings(TenantId tenantId);

}
