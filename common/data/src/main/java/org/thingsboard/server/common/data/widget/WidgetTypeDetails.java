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
package org.winstarcloud.server.common.data.widget;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.winstarcloud.server.common.data.ExportableEntity;
import org.winstarcloud.server.common.data.HasImage;
import org.winstarcloud.server.common.data.HasName;
import org.winstarcloud.server.common.data.HasTenantId;
import org.winstarcloud.server.common.data.id.WidgetTypeId;
import org.winstarcloud.server.common.data.validation.Length;
import org.winstarcloud.server.common.data.validation.NoXss;

@Data
@JsonPropertyOrder({ "fqn", "name", "deprecated", "image", "description", "descriptor", "externalId" })
public class WidgetTypeDetails extends WidgetType implements HasName, HasTenantId, HasImage, ExportableEntity<WidgetTypeId> {

    @Schema(description = "Relative or external image URL. Replaced with image data URL (Base64) in case of relative URL and 'inlineImages' option enabled.")
    private String image;
    @NoXss
    @Length(fieldName = "description", max = 1024)
    @Schema(description = "Description of the widget")
    private String description;
    @NoXss
    @Schema(description = "Tags of the widget type")
    private String[] tags;

    @Getter
    @Setter
    private WidgetTypeId externalId;

    public WidgetTypeDetails() {
        super();
    }

    public WidgetTypeDetails(WidgetTypeId id) {
        super(id);
    }

    public WidgetTypeDetails(BaseWidgetType baseWidgetType) {
        super(baseWidgetType);
    }

    public WidgetTypeDetails(WidgetTypeDetails widgetTypeDetails) {
        super(widgetTypeDetails);
        this.image = widgetTypeDetails.getImage();
        this.description = widgetTypeDetails.getDescription();
        this.tags = widgetTypeDetails.getTags();
        this.externalId = widgetTypeDetails.getExternalId();
    }
}
