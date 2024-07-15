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
package org.winstarcloud.server.service.sms;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedRuntimeException;
import org.springframework.stereotype.Service;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.rule.engine.api.SmsService;
import org.winstarcloud.rule.engine.api.sms.SmsSender;
import org.winstarcloud.rule.engine.api.sms.SmsSenderFactory;
import org.winstarcloud.server.common.data.AdminSettings;
import org.winstarcloud.server.common.data.ApiUsageRecordKey;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.sms.config.SmsProviderConfiguration;
import org.winstarcloud.server.common.data.sms.config.TestSmsRequest;
import org.winstarcloud.server.common.stats.TbApiUsageReportClient;
import org.winstarcloud.server.dao.settings.AdminSettingsService;
import org.winstarcloud.server.service.apiusage.TbApiUsageStateService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
@Slf4j
public class DefaultSmsService implements SmsService {

    private final SmsSenderFactory smsSenderFactory;
    private final AdminSettingsService adminSettingsService;
    private final TbApiUsageStateService apiUsageStateService;
    private final TbApiUsageReportClient apiUsageClient;

    private SmsSender smsSender;

    public DefaultSmsService(SmsSenderFactory smsSenderFactory, AdminSettingsService adminSettingsService, TbApiUsageStateService apiUsageStateService, TbApiUsageReportClient apiUsageClient) {
        this.smsSenderFactory = smsSenderFactory;
        this.adminSettingsService = adminSettingsService;
        this.apiUsageStateService = apiUsageStateService;
        this.apiUsageClient = apiUsageClient;
    }

    @PostConstruct
    private void init() {
        updateSmsConfiguration();
    }

    @PreDestroy
    private void destroy() {
        if (this.smsSender != null) {
            this.smsSender.destroy();
        }
    }

    @Override
    public void updateSmsConfiguration() {
        AdminSettings settings = adminSettingsService.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, "sms");
        if (settings != null) {
            try {
                JsonNode jsonConfig = settings.getJsonValue();
                SmsProviderConfiguration configuration = JacksonUtil.convertValue(jsonConfig, SmsProviderConfiguration.class);
                SmsSender newSmsSender = this.smsSenderFactory.createSmsSender(configuration);
                if (this.smsSender != null) {
                    this.smsSender.destroy();
                }
                this.smsSender = newSmsSender;
            } catch (Exception e) {
                log.error("Failed to create SMS sender", e);
            }
        }
    }

    protected int sendSms(String numberTo, String message) throws WinstarcloudException {
        if (this.smsSender == null) {
            throw new WinstarcloudException("Unable to send SMS: no SMS provider configured!", WinstarcloudErrorCode.GENERAL);
        }
        return this.sendSms(this.smsSender, numberTo, message);
    }

    @Override
    public void sendSms(TenantId tenantId, CustomerId customerId, String[] numbersTo, String message) throws WinstarcloudException {
        if (apiUsageStateService.getApiUsageState(tenantId).isSmsSendEnabled()) {
            int smsCount = 0;
            try {
                for (String numberTo : numbersTo) {
                    smsCount += this.sendSms(numberTo, message);
                }
            } finally {
                if (smsCount > 0) {
                    apiUsageClient.report(tenantId, customerId, ApiUsageRecordKey.SMS_EXEC_COUNT, smsCount);
                }
            }
        } else {
            throw new RuntimeException("SMS sending is disabled due to API limits!");
        }
    }

    @Override
    public void sendTestSms(TestSmsRequest testSmsRequest) throws WinstarcloudException {
        SmsSender testSmsSender;
        try {
            testSmsSender = this.smsSenderFactory.createSmsSender(testSmsRequest.getProviderConfiguration());
        } catch (Exception e) {
            throw handleException(e);
        }
        this.sendSms(testSmsSender, testSmsRequest.getNumberTo(), testSmsRequest.getMessage());
        testSmsSender.destroy();
    }

    @Override
    public boolean isConfigured(TenantId tenantId) {
        return smsSender != null;
    }

    private int sendSms(SmsSender smsSender, String numberTo, String message) throws WinstarcloudException {
        try {
            int sentSms = smsSender.sendSms(numberTo, message);
            log.trace("Successfully sent sms to number: {}", numberTo);
            return sentSms;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private WinstarcloudException handleException(Exception exception) {
        String message;
        if (exception instanceof NestedRuntimeException) {
            message = ((NestedRuntimeException) exception).getMostSpecificCause().getMessage();
        } else {
            message = exception.getMessage();
        }
        log.warn("Unable to send SMS: {}", message);
        return new WinstarcloudException(String.format("Unable to send SMS: %s", message),
                WinstarcloudErrorCode.GENERAL);
    }
}
