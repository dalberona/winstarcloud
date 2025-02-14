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
package org.winstarcloud.rule.engine.util;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.winstarcloud.rule.engine.api.TbContext;
import org.winstarcloud.rule.engine.api.TbNodeException;
import org.winstarcloud.server.common.data.alarm.Alarm;
import org.winstarcloud.server.common.data.id.AlarmId;
import org.winstarcloud.server.common.data.id.EntityId;

public class EntitiesAlarmOriginatorIdAsyncLoader {

    public static ListenableFuture<EntityId> findEntityIdAsync(TbContext ctx, EntityId originator) {
        switch (originator.getEntityType()) {
            case ALARM:
                return getAlarmOriginatorAsync(ctx.getAlarmService().findAlarmByIdAsync(ctx.getTenantId(), (AlarmId) originator), ctx);
            default:
                return Futures.immediateFailedFuture(new TbNodeException("Unexpected originator EntityType " + originator.getEntityType()));
        }
    }

    private static ListenableFuture<EntityId> getAlarmOriginatorAsync(ListenableFuture<Alarm> future, TbContext ctx) {
        return Futures.transformAsync(future, in -> in != null ?
                Futures.immediateFuture(in.getOriginator())
                : Futures.immediateFuture(null), ctx.getDbCallbackExecutor());
    }

}
