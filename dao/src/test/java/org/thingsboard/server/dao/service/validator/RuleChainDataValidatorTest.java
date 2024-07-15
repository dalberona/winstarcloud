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
package org.winstarcloud.server.dao.service.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.rule.RuleChain;
import org.winstarcloud.server.common.data.rule.RuleChainType;
import org.winstarcloud.server.dao.rule.RuleChainService;
import org.winstarcloud.server.dao.tenant.TenantService;

import java.util.UUID;

import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = RuleChainDataValidator.class)
class RuleChainDataValidatorTest {

    @MockBean
    RuleChainService ruleChainService;
    @MockBean
    TenantService tenantService;
    @SpyBean
    RuleChainDataValidator validator;
    TenantId tenantId = TenantId.fromUUID(UUID.fromString("9ef79cdf-37a8-4119-b682-2e7ed4e018da"));

    @BeforeEach
    void setUp() {
        willReturn(true).given(tenantService).tenantExists(tenantId);
    }

    @Test
    void testValidateNameInvocation() {
        RuleChain ruleChain = new RuleChain();
        ruleChain.setName("generate daily report");
        ruleChain.setType(RuleChainType.CORE);
        ruleChain.setTenantId(tenantId);

        validator.validateDataImpl(tenantId, ruleChain);
        verify(validator).validateString("Rule chain name", ruleChain.getName());
    }

}
