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
package org.winstarcloud.server.queue.common;

import org.winstarcloud.server.common.msg.queue.RuleEngineException;
import org.winstarcloud.server.common.msg.queue.TbMsgCallback;
import org.winstarcloud.server.queue.TbQueueCallback;
import org.winstarcloud.server.queue.TbQueueMsgMetadata;

import java.util.concurrent.atomic.AtomicInteger;

public class MultipleTbQueueTbMsgCallbackWrapper implements TbQueueCallback {

    private final AtomicInteger tbQueueCallbackCount;
    private final TbMsgCallback tbMsgCallback;

    public MultipleTbQueueTbMsgCallbackWrapper(int tbQueueCallbackCount, TbMsgCallback tbMsgCallback) {
        this.tbQueueCallbackCount = new AtomicInteger(tbQueueCallbackCount);
        this.tbMsgCallback = tbMsgCallback;
    }

    @Override
    public void onSuccess(TbQueueMsgMetadata metadata) {
        if (tbQueueCallbackCount.decrementAndGet() <= 0) {
            tbMsgCallback.onSuccess();
        }
    }

    @Override
    public void onFailure(Throwable t) {
        tbMsgCallback.onFailure(new RuleEngineException(t.getMessage(), t));
    }
}
