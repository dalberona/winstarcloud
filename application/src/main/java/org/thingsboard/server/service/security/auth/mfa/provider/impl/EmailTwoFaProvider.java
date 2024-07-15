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
package org.winstarcloud.server.service.security.auth.mfa.provider.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.winstarcloud.rule.engine.api.MailService;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.security.model.mfa.account.EmailTwoFaAccountConfig;
import org.winstarcloud.server.common.data.security.model.mfa.provider.EmailTwoFaProviderConfig;
import org.winstarcloud.server.common.data.security.model.mfa.provider.TwoFaProviderType;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.security.model.SecurityUser;

@Service
@TbCoreComponent
public class EmailTwoFaProvider extends OtpBasedTwoFaProvider<EmailTwoFaProviderConfig, EmailTwoFaAccountConfig> {

    private final MailService mailService;

    protected EmailTwoFaProvider(CacheManager cacheManager, MailService mailService) {
        super(cacheManager);
        this.mailService = mailService;
    }

    @Override
    public EmailTwoFaAccountConfig generateNewAccountConfig(User user, EmailTwoFaProviderConfig providerConfig) {
        EmailTwoFaAccountConfig config = new EmailTwoFaAccountConfig();
        config.setEmail(user.getEmail());
        return config;
    }

    @Override
    public void check(TenantId tenantId) throws WinstarcloudException {
        try {
            mailService.testConnection(tenantId);
        } catch (Exception e) {
            throw new WinstarcloudException("Mail service is not set up", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    @Override
    protected void sendVerificationCode(SecurityUser user, String verificationCode, EmailTwoFaProviderConfig providerConfig, EmailTwoFaAccountConfig accountConfig) throws WinstarcloudException {
        mailService.sendTwoFaVerificationEmail(accountConfig.getEmail(), verificationCode, providerConfig.getVerificationCodeLifetime());
    }

    @Override
    public TwoFaProviderType getType() {
        return TwoFaProviderType.EMAIL;
    }

}
