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
package org.winstarcloud.server.dao.widget;

import org.winstarcloud.server.common.data.DashboardInfo;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.WidgetsBundleId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.data.widget.WidgetsBundle;
import org.winstarcloud.server.dao.Dao;
import org.winstarcloud.server.dao.ExportableEntityDao;
import org.winstarcloud.server.dao.ImageContainerDao;

import java.util.UUID;

/**
 * The Interface WidgetsBundleDao.
 */
public interface WidgetsBundleDao extends Dao<WidgetsBundle>, ExportableEntityDao<WidgetsBundleId, WidgetsBundle>, ImageContainerDao<WidgetsBundle> {

    /**
     * Save or update widgets bundle object
     *
     * @param tenantId the tenantId
     * @param widgetsBundle the widgets bundle object
     * @return saved widgets bundle object
     */
    WidgetsBundle save(TenantId tenantId, WidgetsBundle widgetsBundle);

    /**
     * Find widgets bundle by tenantId and alias.
     *
     * @param tenantId the tenantId
     * @param alias the alias
     * @return the widgets bundle object
     */
    WidgetsBundle findWidgetsBundleByTenantIdAndAlias(UUID tenantId, String alias);

    /**
     * Find system widgets bundles by page link.
     *
     * @param pageLink the page link
     * @return the list of widgets bundles objects
     */
    PageData<WidgetsBundle> findSystemWidgetsBundles(TenantId tenantId, boolean fullSearch, PageLink pageLink);

    /**
     * Find tenant widgets bundles by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of widgets bundles objects
     */
    PageData<WidgetsBundle> findTenantWidgetsBundlesByTenantId(UUID tenantId, PageLink pageLink);

    /**
     * Find all tenant widgets bundles (including system) by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of widgets bundles objects
     */
    PageData<WidgetsBundle> findAllTenantWidgetsBundlesByTenantId(UUID tenantId, boolean fullSearch, PageLink pageLink);

    /**
     * Find all tenant widgets bundles (does not include system) by tenantId and page link.
     *
     * @param tenantId the tenantId
     * @param pageLink the page link
     * @return the list of widgets bundles objects
     */
    PageData<WidgetsBundle> findTenantWidgetsBundlesByTenantId(UUID tenantId, boolean fullSearch, PageLink pageLink);

    PageData<WidgetsBundle> findAllWidgetsBundles(PageLink pageLink);

}

