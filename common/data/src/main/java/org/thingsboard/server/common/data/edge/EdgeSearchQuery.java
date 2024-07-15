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
package org.winstarcloud.server.common.data.edge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.common.data.relation.EntityRelationsQuery;
import org.winstarcloud.server.common.data.relation.RelationEntityTypeFilter;
import org.winstarcloud.server.common.data.relation.RelationsSearchParameters;

import java.util.Collections;
import java.util.List;

@Data
public class EdgeSearchQuery {

    @Schema(description = "Main search parameters.")
    private RelationsSearchParameters parameters;
    @Schema(description = "Type of the relation between root entity and edge (e.g. 'Contains' or 'Manages').")
    private String relationType;
    @Schema(description = "Array of edge types to filter the related entities (e.g. 'Silos', 'Stores').")
    private List<String> edgeTypes;

    public EntityRelationsQuery toEntitySearchQuery() {
        EntityRelationsQuery query = new EntityRelationsQuery();
        query.setParameters(parameters);
        query.setFilters(
                Collections.singletonList(new RelationEntityTypeFilter(relationType == null ? EntityRelation.CONTAINS_TYPE : relationType,
                        Collections.singletonList(EntityType.EDGE))));
        return query;
    }
}
