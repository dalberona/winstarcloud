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
package org.winstarcloud.server.dao.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.dao.Dao;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EntityDaoRegistry {

    private final Map<EntityType, Dao<?>> daos = new EnumMap<>(EntityType.class);

    private EntityDaoRegistry(List<Dao<?>> daos) {
        daos.forEach(dao -> {
            EntityType entityType = dao.getEntityType();
            if (entityType != null) {
                this.daos.put(entityType, dao);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> Dao<T> getDao(EntityType entityType) {
        Dao<T> dao = (Dao<T>) daos.get(entityType);
        if (dao == null) {
            throw new IllegalArgumentException("Missing dao for entity type " + entityType);
        }
        return dao;
    }

}
