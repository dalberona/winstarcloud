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
package org.winstarcloud.rule.engine.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.mail.javamail.JavaMailSender;
import org.winstarcloud.server.common.data.ApiFeature;
import org.winstarcloud.server.common.data.ApiUsageRecordState;
import org.winstarcloud.server.common.data.ApiUsageStateValue;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.TenantId;

public interface MailService {

    void updateMailConfiguration();

    void sendEmail(TenantId tenantId, String email, String subject, String message) throws WinstarcloudException;

    void sendTestMail(JsonNode config, String email) throws WinstarcloudException;

    void sendActivationEmail(String activationLink, String email) throws WinstarcloudException;

    void sendAccountActivatedEmail(String loginLink, String email) throws WinstarcloudException;

    void sendResetPasswordEmail(String passwordResetLink, String email) throws WinstarcloudException;

    void sendResetPasswordEmailAsync(String passwordResetLink, String email);

    void sendPasswordWasResetEmail(String loginLink, String email) throws WinstarcloudException;

    void sendAccountLockoutEmail(String lockoutEmail, String email, Integer maxFailedLoginAttempts) throws WinstarcloudException;

    void sendTwoFaVerificationEmail(String email, String verificationCode, int expirationTimeSeconds) throws WinstarcloudException;

    void send(TenantId tenantId, CustomerId customerId, TbEmail tbEmail) throws WinstarcloudException;

    void send(TenantId tenantId, CustomerId customerId, TbEmail tbEmail, JavaMailSender javaMailSender, long timeout) throws WinstarcloudException;

    void sendApiFeatureStateEmail(ApiFeature apiFeature, ApiUsageStateValue stateValue, String email, ApiUsageRecordState recordState) throws WinstarcloudException;

    void testConnection(TenantId tenantId) throws Exception;

    boolean isConfigured(TenantId tenantId);

}
