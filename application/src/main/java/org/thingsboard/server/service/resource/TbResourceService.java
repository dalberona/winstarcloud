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
package org.winstarcloud.server.service.resource;

import org.winstarcloud.server.common.data.TbResource;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.lwm2m.LwM2mObject;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.service.entitiy.SimpleTbEntityService;

import java.util.List;

public interface TbResourceService extends SimpleTbEntityService<TbResource> {

    List<LwM2mObject> findLwM2mObject(TenantId tenantId,
                                      String sortOrder,
                                      String sortProperty,
                                      String[] objectIds);

    List<LwM2mObject> findLwM2mObjectPage(TenantId tenantId,
                                          String sortProperty,
                                          String sortOrder,
                                          PageLink pageLink);

}
