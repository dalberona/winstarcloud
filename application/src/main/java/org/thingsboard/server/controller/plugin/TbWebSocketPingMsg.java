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
package org.winstarcloud.server.controller.plugin;

import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class TbWebSocketPingMsg implements TbWebSocketMsg<ByteBuffer> {

    public static TbWebSocketPingMsg INSTANCE = new TbWebSocketPingMsg();

    private static final ByteBuffer PING_MSG = ByteBuffer.wrap(new byte[]{});

    @Override
    public TbWebSocketMsgType getType() {
        return TbWebSocketMsgType.PING;
    }

    @Override
    public ByteBuffer getMsg() {
        return PING_MSG;
    }
}
