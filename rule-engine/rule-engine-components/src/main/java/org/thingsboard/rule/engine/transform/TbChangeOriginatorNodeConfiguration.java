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
package org.winstarcloud.rule.engine.transform;

import lombok.Data;
import org.winstarcloud.rule.engine.api.NodeConfiguration;
import org.winstarcloud.rule.engine.data.RelationsQuery;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.common.data.relation.EntitySearchDirection;
import org.winstarcloud.server.common.data.relation.RelationEntityTypeFilter;

import java.util.Collections;

@Data
public class TbChangeOriginatorNodeConfiguration implements NodeConfiguration<TbChangeOriginatorNodeConfiguration> {

    private static final String CUSTOMER_SOURCE = "CUSTOMER";

    private String originatorSource;

    private RelationsQuery relationsQuery;
    private String entityType;
    private String entityNamePattern;

    @Override
    public TbChangeOriginatorNodeConfiguration defaultConfiguration() {
        TbChangeOriginatorNodeConfiguration configuration = new TbChangeOriginatorNodeConfiguration();
        configuration.setOriginatorSource(CUSTOMER_SOURCE);

        RelationsQuery relationsQuery = new RelationsQuery();
        relationsQuery.setDirection(EntitySearchDirection.FROM);
        relationsQuery.setMaxLevel(1);
        RelationEntityTypeFilter relationEntityTypeFilter = new RelationEntityTypeFilter(EntityRelation.CONTAINS_TYPE, Collections.emptyList());
        relationsQuery.setFilters(Collections.singletonList(relationEntityTypeFilter));
        configuration.setRelationsQuery(relationsQuery);

        return configuration;
    }
}
