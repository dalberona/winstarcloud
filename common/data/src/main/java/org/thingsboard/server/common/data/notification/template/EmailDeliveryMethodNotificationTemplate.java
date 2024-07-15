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
package org.winstarcloud.server.common.data.notification.template;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.winstarcloud.server.common.data.notification.NotificationDeliveryMethod;
import org.winstarcloud.server.common.data.validation.Length;
import org.winstarcloud.server.common.data.validation.NoXss;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmailDeliveryMethodNotificationTemplate extends DeliveryMethodNotificationTemplate implements HasSubject {

    @NoXss(fieldName = "email subject")
    @Length(fieldName = "email subject", max = 250, message = "cannot be longer than 250 chars")
    @NotEmpty
    private String subject;

    private final List<TemplatableValue> templatableValues = List.of(
            TemplatableValue.of(this::getBody, this::setBody),
            TemplatableValue.of(this::getSubject, this::setSubject)
    );

    public EmailDeliveryMethodNotificationTemplate(EmailDeliveryMethodNotificationTemplate other) {
        super(other);
        this.subject = other.subject;
    }

    @Override
    public NotificationDeliveryMethod getMethod() {
        return NotificationDeliveryMethod.EMAIL;
    }

    @Override
    public EmailDeliveryMethodNotificationTemplate copy() {
        return new EmailDeliveryMethodNotificationTemplate(this);
    }

}
