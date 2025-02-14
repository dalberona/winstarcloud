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

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.AdminSettings;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.security.UserAuthSettings;
import org.winstarcloud.server.common.data.security.model.mfa.PlatformTwoFaSettings;
import org.winstarcloud.server.common.data.security.model.mfa.account.AccountTwoFaSettings;
import org.winstarcloud.server.common.data.security.model.mfa.account.TwoFaAccountConfig;
import org.winstarcloud.server.common.data.security.model.mfa.provider.TwoFaProviderConfig;
import org.winstarcloud.server.common.data.security.model.mfa.provider.TwoFaProviderType;
import org.winstarcloud.server.dao.service.ConstraintValidator;
import org.winstarcloud.server.dao.settings.AdminSettingsDao;
import org.winstarcloud.server.dao.settings.AdminSettingsService;
import org.winstarcloud.server.dao.user.UserAuthSettingsDao;
import org.winstarcloud.server.service.security.auth.mfa.TwoFactorAuthService;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultTwoFaConfigManager implements TwoFaConfigManager {

    private final UserAuthSettingsDao userAuthSettingsDao;
    private final AdminSettingsService adminSettingsService;
    private final AdminSettingsDao adminSettingsDao;
    @Autowired @Lazy
    private TwoFactorAuthService twoFactorAuthService;

    protected static final String TWO_FACTOR_AUTH_SETTINGS_KEY = "twoFaSettings";


    @Override
    public Optional<AccountTwoFaSettings> getAccountTwoFaSettings(TenantId tenantId, UserId userId) {
        PlatformTwoFaSettings platformTwoFaSettings = getPlatformTwoFaSettings(tenantId, true).orElse(null);
        return Optional.ofNullable(userAuthSettingsDao.findByUserId(userId))
                .map(userAuthSettings -> {
                    AccountTwoFaSettings twoFaSettings = userAuthSettings.getTwoFaSettings();
                    if (twoFaSettings == null) return null;
                    boolean updateNeeded;

                    Map<TwoFaProviderType, TwoFaAccountConfig> configs = twoFaSettings.getConfigs();
                    updateNeeded = configs.keySet().removeIf(providerType -> {
                        return platformTwoFaSettings == null || platformTwoFaSettings.getProviderConfig(providerType).isEmpty();
                    });
                    if (configs.size() == 1 && configs.containsKey(TwoFaProviderType.BACKUP_CODE)) {
                        configs.remove(TwoFaProviderType.BACKUP_CODE);
                        updateNeeded = true;
                    }
                    if (!configs.isEmpty() && configs.values().stream().noneMatch(TwoFaAccountConfig::isUseByDefault)) {
                        configs.values().stream()
                                .filter(config -> config.getProviderType() != TwoFaProviderType.BACKUP_CODE)
                                .findFirst().ifPresent(config -> config.setUseByDefault(true));
                        updateNeeded = true;
                    }

                    if (updateNeeded) {
                        twoFaSettings = saveAccountTwoFaSettings(tenantId, userId, twoFaSettings);
                    }
                    return twoFaSettings;
                });
    }

    protected AccountTwoFaSettings saveAccountTwoFaSettings(TenantId tenantId, UserId userId, AccountTwoFaSettings settings) {
        UserAuthSettings userAuthSettings = Optional.ofNullable(userAuthSettingsDao.findByUserId(userId))
                .orElseGet(() -> {
                    UserAuthSettings newUserAuthSettings = new UserAuthSettings();
                    newUserAuthSettings.setUserId(userId);
                    return newUserAuthSettings;
                });
        userAuthSettings.setTwoFaSettings(settings);
        settings.getConfigs().values().forEach(accountConfig -> accountConfig.setSerializeHiddenFields(true));
        userAuthSettingsDao.save(tenantId, userAuthSettings);
        settings.getConfigs().values().forEach(accountConfig -> accountConfig.setSerializeHiddenFields(false));
        return settings;
    }


    @Override
    public Optional<TwoFaAccountConfig> getTwoFaAccountConfig(TenantId tenantId, UserId userId, TwoFaProviderType providerType) {
        return getAccountTwoFaSettings(tenantId, userId)
                .map(AccountTwoFaSettings::getConfigs)
                .flatMap(configs -> Optional.ofNullable(configs.get(providerType)));
    }

    @Override
    public AccountTwoFaSettings saveTwoFaAccountConfig(TenantId tenantId, UserId userId, TwoFaAccountConfig accountConfig) {
        getTwoFaProviderConfig(tenantId, accountConfig.getProviderType())
                .orElseThrow(() -> new IllegalArgumentException("2FA provider is not configured"));

        AccountTwoFaSettings settings = getAccountTwoFaSettings(tenantId, userId).orElseGet(() -> {
            AccountTwoFaSettings newSettings = new AccountTwoFaSettings();
            newSettings.setConfigs(new LinkedHashMap<>());
            return newSettings;
        });
        Map<TwoFaProviderType, TwoFaAccountConfig> configs = settings.getConfigs();
        if (configs.isEmpty() && accountConfig.getProviderType() == TwoFaProviderType.BACKUP_CODE) {
            throw new IllegalArgumentException("To use 2FA backup codes you first need to configure at least one provider");
        }
        if (accountConfig.isUseByDefault()) {
            configs.values().forEach(config -> config.setUseByDefault(false));
        }
        configs.put(accountConfig.getProviderType(), accountConfig);
        if (configs.values().stream().noneMatch(TwoFaAccountConfig::isUseByDefault)) {
            configs.values().stream().findFirst().ifPresent(config -> config.setUseByDefault(true));
        }
        return saveAccountTwoFaSettings(tenantId, userId, settings);
    }

    @Override
    public AccountTwoFaSettings deleteTwoFaAccountConfig(TenantId tenantId, UserId userId, TwoFaProviderType providerType) {
        AccountTwoFaSettings settings = getAccountTwoFaSettings(tenantId, userId)
                .orElseThrow(() -> new IllegalArgumentException("2FA not configured"));
        settings.getConfigs().remove(providerType);
        if (settings.getConfigs().size() == 1) {
            settings.getConfigs().remove(TwoFaProviderType.BACKUP_CODE);
        }
        if (!settings.getConfigs().isEmpty() && settings.getConfigs().values().stream()
                .noneMatch(TwoFaAccountConfig::isUseByDefault)) {
            settings.getConfigs().values().stream()
                    .min(Comparator.comparing(TwoFaAccountConfig::getProviderType))
                    .ifPresent(config -> config.setUseByDefault(true));
        }
        return saveAccountTwoFaSettings(tenantId, userId, settings);
    }


    private Optional<TwoFaProviderConfig> getTwoFaProviderConfig(TenantId tenantId, TwoFaProviderType providerType) {
        return getPlatformTwoFaSettings(tenantId, true)
                .flatMap(twoFaSettings -> twoFaSettings.getProviderConfig(providerType));
    }

    @Override
    public Optional<PlatformTwoFaSettings> getPlatformTwoFaSettings(TenantId tenantId, boolean sysadminSettingsAsDefault) {
        return Optional.ofNullable(adminSettingsService.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, TWO_FACTOR_AUTH_SETTINGS_KEY))
                .map(adminSettings -> JacksonUtil.treeToValue(adminSettings.getJsonValue(), PlatformTwoFaSettings.class));
    }

    @Override
    public PlatformTwoFaSettings savePlatformTwoFaSettings(TenantId tenantId, PlatformTwoFaSettings twoFactorAuthSettings) throws WinstarcloudException {
        ConstraintValidator.validateFields(twoFactorAuthSettings);
        for (TwoFaProviderConfig providerConfig : twoFactorAuthSettings.getProviders()) {
            twoFactorAuthService.checkProvider(tenantId, providerConfig.getProviderType());
        }

        AdminSettings settings = Optional.ofNullable(adminSettingsService.findAdminSettingsByKey(tenantId, TWO_FACTOR_AUTH_SETTINGS_KEY))
                .orElseGet(() -> {
                    AdminSettings newSettings = new AdminSettings();
                    newSettings.setKey(TWO_FACTOR_AUTH_SETTINGS_KEY);
                    return newSettings;
                });
        settings.setJsonValue(JacksonUtil.valueToTree(twoFactorAuthSettings));
        adminSettingsService.saveAdminSettings(tenantId, settings);
        return twoFactorAuthSettings;
    }

    @Override
    public void deletePlatformTwoFaSettings(TenantId tenantId) {
        Optional.ofNullable(adminSettingsService.findAdminSettingsByKey(tenantId, TWO_FACTOR_AUTH_SETTINGS_KEY))
                .ifPresent(adminSettings -> adminSettingsDao.removeById(tenantId, adminSettings.getId().getId()));
    }

}
