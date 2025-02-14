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
package org.winstarcloud.server.service.mail;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.winstarcloud.common.util.JacksonUtil;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Service
@Slf4j
public class DefaultTbMailConfigTemplateService implements TbMailConfigTemplateService {

    private JsonNode mailConfigTemplates;

    @PostConstruct
    private void postConstruct() throws IOException {
        mailConfigTemplates = JacksonUtil.toJsonNode(new ClassPathResource("/templates/mail_config_templates.json").getFile());
    }

    @Override
    public JsonNode findAllMailConfigTemplates() {
        return mailConfigTemplates;
    }
}
