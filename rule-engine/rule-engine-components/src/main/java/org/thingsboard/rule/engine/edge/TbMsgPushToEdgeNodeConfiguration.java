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
package org.winstarcloud.rule.engine.edge;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.winstarcloud.server.common.data.AttributeScope;

@EqualsAndHashCode(callSuper = true)
@Data
public class TbMsgPushToEdgeNodeConfiguration extends BaseTbMsgPushNodeConfiguration {

    @Override
    public TbMsgPushToEdgeNodeConfiguration defaultConfiguration() {
        TbMsgPushToEdgeNodeConfiguration configuration = new TbMsgPushToEdgeNodeConfiguration();
        configuration.setScope(AttributeScope.SERVER_SCOPE.name());
        return configuration;
    }

}
