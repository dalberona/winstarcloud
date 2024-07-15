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
package org.winstarcloud.rule.engine.mail;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.rule.engine.api.RuleNode;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbEmail;
import org.winstarcloud.rule.engine.api.TbNode;
import org.winstarcloud.rule.engine.api.TbNodeConfiguration;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.rule.engine.api.util.TbNodeUtils;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.msg.TbMsgType;
import org.winstarcloud.server.common.data.msg.TbNodeConnectionType;
import org.winstarcloud.server.common.data.plugin.ComponentType;
import org.winstarcloud.server.common.msg.TbMsg;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RuleNode(
        type = ComponentType.TRANSFORMATION,
        name = "to email",
        configClazz = TbMsgToEmailNodeConfiguration.class,
        nodeDescription = "Transforms message to email message",
        nodeDetails = "Transforms message to email message. If transformation completed successfully output message type will be set to <code>SEND_EMAIL</code>.<br><br>" +
                "Output connections: <code>Success</code>, <code>Failure</code>.",
        uiResources = {"static/rulenode/rulenode-core-config.js"},
        configDirective = "tbTransformationNodeToEmailConfig",
        icon = "email"
)
public class TbMsgToEmailNode implements TbNode {

    private static final String IMAGES = "images";
    private static final String DYNAMIC = "dynamic";

    private TbMsgToEmailNodeConfiguration config;
    private boolean dynamicMailBodyType;

    @Override
    public void init(TbContext ctx, TbNodeConfiguration configuration) throws TbNodeException {
        this.config = TbNodeUtils.convert(configuration, TbMsgToEmailNodeConfiguration.class);
        this.dynamicMailBodyType = DYNAMIC.equals(this.config.getMailBodyType());
     }

    @Override
    public void onMsg(TbContext ctx, TbMsg msg) {
        try {
            TbEmail email = convert(msg);
            TbMsg emailMsg = buildEmailMsg(ctx, msg, email);
            ctx.tellNext(emailMsg, TbNodeConnectionType.SUCCESS);
        } catch (Exception ex) {
            log.warn("Can not convert message to email " + ex.getMessage());
            ctx.tellFailure(msg, ex);
        }
    }

    private TbMsg buildEmailMsg(TbContext ctx, TbMsg msg, TbEmail email) {
        String emailJson = JacksonUtil.toString(email);
        return ctx.transformMsg(msg, TbMsgType.SEND_EMAIL, msg.getOriginator(), msg.getMetaData().copy(), emailJson);
    }

    private TbEmail convert(TbMsg msg) {
        TbEmail.TbEmailBuilder builder = TbEmail.builder();
        builder.from(fromTemplate(config.getFromTemplate(), msg));
        builder.to(fromTemplate(config.getToTemplate(), msg));
        builder.cc(fromTemplate(config.getCcTemplate(), msg));
        builder.bcc(fromTemplate(config.getBccTemplate(), msg));
        String htmlStr = dynamicMailBodyType ?
                fromTemplate(config.getIsHtmlTemplate(), msg) : config.getMailBodyType();
        builder.html(Boolean.parseBoolean(htmlStr));
        builder.subject(fromTemplate(config.getSubjectTemplate(), msg));
        builder.body(fromTemplate(config.getBodyTemplate(), msg));
        String imagesStr = msg.getMetaData().getValue(IMAGES);
        if (!StringUtils.isEmpty(imagesStr)) {
            Map<String, String> imgMap = JacksonUtil.fromString(imagesStr, new TypeReference<HashMap<String, String>>() {});
            builder.images(imgMap);
        }
        return builder.build();
    }

    private String fromTemplate(String template, TbMsg msg) {
        return StringUtils.isNotEmpty(template) ? TbNodeUtils.processPattern(template, msg) : null;
    }

}
