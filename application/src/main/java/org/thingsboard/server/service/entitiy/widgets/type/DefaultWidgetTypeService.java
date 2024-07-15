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
package org.winstarcloud.server.service.entitiy.widgets.type;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.audit.ActionType;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.widget.WidgetType;
import org.winstarcloud.server.common.data.widget.WidgetTypeDetails;
import org.winstarcloud.server.dao.resource.ImageService;
import org.winstarcloud.server.dao.widget.WidgetTypeService;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.entitiy.AbstractTbEntityService;

@Service
@TbCoreComponent
@AllArgsConstructor
public class DefaultWidgetTypeService extends AbstractTbEntityService implements TbWidgetTypeService {


    private final WidgetTypeService widgetTypeService;

    @Override
    public WidgetTypeDetails save(WidgetTypeDetails entity, User user) throws Exception {
        return this.save(entity, false, user);
    }

    @Override
    public WidgetTypeDetails save(WidgetTypeDetails widgetTypeDetails, boolean updateExistingByFqn, User user) throws Exception {
        TenantId tenantId = widgetTypeDetails.getTenantId();
        if (widgetTypeDetails.getId() == null && StringUtils.isNotEmpty(widgetTypeDetails.getFqn()) && updateExistingByFqn) {
            WidgetType widgetType = widgetTypeService.findWidgetTypeByTenantIdAndFqn(tenantId, widgetTypeDetails.getFqn());
            if (widgetType != null) {
                widgetTypeDetails.setId(widgetType.getId());
            }
        }
        ActionType actionType = widgetTypeDetails.getId() == null ? ActionType.ADDED : ActionType.UPDATED;
        try {
            WidgetTypeDetails savedWidgetTypeDetails = checkNotNull(widgetTypeService.saveWidgetType(widgetTypeDetails));
            autoCommit(user, savedWidgetTypeDetails.getId());
            logEntityActionService.logEntityAction(tenantId, savedWidgetTypeDetails.getId(), savedWidgetTypeDetails,
                    null, actionType, user);
            return savedWidgetTypeDetails;
        } catch (Exception e) {
            logEntityActionService.logEntityAction(tenantId, emptyId(EntityType.WIDGET_TYPE), widgetTypeDetails, actionType, user, e);
            throw e;
        }
    }

    @Override
    public void delete(WidgetTypeDetails widgetTypeDetails, User user) {
        ActionType actionType = ActionType.DELETED;
        TenantId tenantId = widgetTypeDetails.getTenantId();
        try {
            widgetTypeService.deleteWidgetType(widgetTypeDetails.getTenantId(), widgetTypeDetails.getId());
            logEntityActionService.logEntityAction(tenantId, widgetTypeDetails.getId(), widgetTypeDetails, null, actionType, user);
        } catch (Exception e) {
            logEntityActionService.logEntityAction(tenantId, emptyId(EntityType.WIDGET_TYPE), actionType, user, e, widgetTypeDetails.getId());
            throw e;
        }
    }
}
