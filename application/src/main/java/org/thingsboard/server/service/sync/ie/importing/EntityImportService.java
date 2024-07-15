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
package org.winstarcloud.server.service.sync.ie.importing;

import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.ExportableEntity;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.sync.ie.EntityExportData;
import org.winstarcloud.server.common.data.sync.ie.EntityImportResult;
import org.winstarcloud.server.service.sync.vc.data.EntitiesImportCtx;

public interface EntityImportService<I extends EntityId, E extends ExportableEntity<I>, D extends EntityExportData<E>> {

    EntityImportResult<E> importEntity(EntitiesImportCtx ctx, D exportData) throws WinstarcloudException;

    EntityType getEntityType();

}
