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
package org.winstarcloud.rule.engine.telemetry;

import com.google.common.util.concurrent.FutureCallback;
import lombok.Data;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.server.common.msg.TbMsg;

import jakarta.annotation.Nullable;

/**
 * Created by ashvayka on 02.04.18.
 */
@Data
class TelemetryNodeCallback implements FutureCallback<Void> {
    private final TbContext ctx;
    private final TbMsg msg;

    @Override
    public void onSuccess(@Nullable Void result) {
        ctx.tellSuccess(msg);
    }

    @Override
    public void onFailure(Throwable t) {
        ctx.tellFailure(msg, t);
    }
}
