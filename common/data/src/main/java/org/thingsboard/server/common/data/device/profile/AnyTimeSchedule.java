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
package org.winstarcloud.server.common.data.device.profile;

import org.winstarcloud.server.common.data.query.DynamicValue;

public class AnyTimeSchedule implements AlarmSchedule {

    @Override
    public AlarmScheduleType getType() {
        return AlarmScheduleType.ANY_TIME;
    }

    @Override
    public DynamicValue<String> getDynamicValue() {
        return null;
    }

}
