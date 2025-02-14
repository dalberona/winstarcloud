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
package org.winstarcloud.server.transport.coap.attributes.updates;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.server.resources.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.winstarcloud.server.coapserver.DefaultCoapServerService;
import org.winstarcloud.server.common.transport.service.DefaultTransportService;
import org.winstarcloud.server.dao.service.DaoSqlTest;
import org.winstarcloud.server.transport.coap.CoapTestConfigProperties;
import org.winstarcloud.server.transport.coap.CoapTransportResource;
import org.winstarcloud.server.transport.coap.attributes.AbstractCoapAttributesIntegrationTest;

import static org.mockito.Mockito.spy;

@Slf4j
@DaoSqlTest
public class CoapAttributesUpdatesIntegrationTest extends AbstractCoapAttributesIntegrationTest {

    @Autowired
    DefaultCoapServerService defaultCoapServerService;

    @Autowired
    DefaultTransportService defaultTransportService;

    @Before
    public void beforeTest() throws Exception {
        CoapTestConfigProperties configProperties = CoapTestConfigProperties.builder()
                .deviceName("Test Subscribe to attribute updates")
                .build();
        processBeforeTest(configProperties);
    }

    @After
    public void afterTest() throws Exception {
        processAfterTest();
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServer() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(false);
    }

    @Test
    public void testSubscribeToAttributesUpdatesFromTheServerWithEmptyCurrentStateNotification() throws Exception {
        processJsonTestSubscribeToAttributesUpdates(true);
    }
}
