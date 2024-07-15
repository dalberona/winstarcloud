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
package org.winstarcloud.server.dao.sql.mobile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.mobile.MobileAppSettings;
import org.winstarcloud.server.dao.DaoUtil;
import org.winstarcloud.server.dao.mobile.MobileAppSettingsDao;
import org.winstarcloud.server.dao.model.sql.MobileAppSettingsEntity;
import org.winstarcloud.server.dao.sql.JpaAbstractDao;
import org.winstarcloud.server.dao.util.SqlDao;

import java.util.UUID;


@Component
@Slf4j
@SqlDao
public class JpaMobileAppSettingsDao extends JpaAbstractDao<MobileAppSettingsEntity, MobileAppSettings> implements MobileAppSettingsDao {

    @Autowired
    private MobileAppSettingsRepository mobileAppSettingsRepository;


    @Override
    public MobileAppSettings findByTenantId(TenantId tenantId) {
        return DaoUtil.getData(mobileAppSettingsRepository.findByTenantId(tenantId.getId()));
    }

    @Override
    public void removeByTenantId(TenantId tenantId) {
        mobileAppSettingsRepository.deleteByTenantId(tenantId.getId());
    }

    @Override
    protected Class<MobileAppSettingsEntity> getEntityClass() {
        return MobileAppSettingsEntity.class;
    }

    @Override
    protected JpaRepository<MobileAppSettingsEntity, UUID> getRepository() {
        return mobileAppSettingsRepository;
    }
}
