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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.winstarcloud.server.common.data.BaseData;
import org.winstarcloud.server.common.data.ExportableEntity;
import org.winstarcloud.server.common.data.HasImage;
import org.winstarcloud.server.common.data.HasName;
import org.winstarcloud.server.common.data.HasTenantId;
import org.winstarcloud.server.common.data.HasTitle;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.WidgetsBundleId;
import org.winstarcloud.server.common.data.validation.Length;
import org.winstarcloud.server.common.data.validation.NoXss;

@Schema
@EqualsAndHashCode(callSuper = true)
public class WidgetsBundle extends BaseData<WidgetsBundleId> implements HasName, HasTenantId, ExportableEntity<WidgetsBundleId>, HasTitle, HasImage {

    private static final long serialVersionUID = -7627368878362410489L;

    @Getter
    @Setter
    @Schema(description = "JSON object with Tenant Id.", accessMode = Schema.AccessMode.READ_ONLY)
    private TenantId tenantId;

    @NoXss
    @Length(fieldName = "alias")
    @Getter
    @Setter
    @Schema(description = "Unique alias that is used in widget types as a reference widget bundle", accessMode = Schema.AccessMode.READ_ONLY)
    private String alias;

    @NoXss
    @Length(fieldName = "title")
    @Getter
    @Setter
    @Schema(description = "Title used in search and UI", accessMode = Schema.AccessMode.READ_ONLY)
    private String title;

    @Getter
    @Setter
    @Schema(description = "Relative or external image URL. Replaced with image data URL (Base64) in case of relative URL and 'inlineImages' option enabled.", accessMode = Schema.AccessMode.READ_ONLY)
    private String image;

    @NoXss
    @Length(fieldName = "description", max = 1024)
    @Getter
    @Setter
    @Schema(description = "Description", accessMode = Schema.AccessMode.READ_ONLY)
    private String description;

    @Getter
    @Setter
    @Schema(description = "Order", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer order;

    @Getter
    @Setter
    private WidgetsBundleId externalId;

    public WidgetsBundle() {
        super();
    }

    public WidgetsBundle(WidgetsBundleId id) {
        super(id);
    }

    public WidgetsBundle(WidgetsBundle widgetsBundle) {
        super(widgetsBundle);
        this.tenantId = widgetsBundle.getTenantId();
        this.alias = widgetsBundle.getAlias();
        this.title = widgetsBundle.getTitle();
        this.image = widgetsBundle.getImage();
        this.description = widgetsBundle.getDescription();
        this.order = widgetsBundle.getOrder();
        this.externalId = widgetsBundle.getExternalId();
    }

    @Schema(description = "JSON object with the Widget Bundle Id. " +
            "Specify this field to update the Widget Bundle. " +
            "Referencing non-existing Widget Bundle Id will cause error. " +
            "Omit this field to create new Widget Bundle." )
    @Override
    public WidgetsBundleId getId() {
        return super.getId();
    }

    @Schema(description = "Timestamp of the Widget Bundle creation, in milliseconds", example = "1609459200000", accessMode = Schema.AccessMode.READ_ONLY)
    @Override
    public long getCreatedTime() {
        return super.getCreatedTime();
    }

    @Schema(description = "Same as title of the Widget Bundle. Read-only field. Update the 'title' to change the 'name' of the Widget Bundle.", accessMode = Schema.AccessMode.READ_ONLY)
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getName() {
        return title;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WidgetsBundle{");
        sb.append("tenantId=").append(tenantId);
        sb.append(", alias='").append(alias).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
