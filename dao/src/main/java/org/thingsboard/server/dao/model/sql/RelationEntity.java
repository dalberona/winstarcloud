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
package org.winstarcloud.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import org.winstarcloud.server.common.data.id.EntityIdFactory;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.common.data.relation.RelationTypeGroup;
import org.winstarcloud.server.dao.model.ToData;
import org.winstarcloud.server.dao.util.mapping.JsonConverter;

import java.util.UUID;

import static org.winstarcloud.server.dao.model.ModelConstants.ADDITIONAL_INFO_PROPERTY;
import static org.winstarcloud.server.dao.model.ModelConstants.RELATION_FROM_ID_PROPERTY;
import static org.winstarcloud.server.dao.model.ModelConstants.RELATION_FROM_TYPE_PROPERTY;
import static org.winstarcloud.server.dao.model.ModelConstants.RELATION_TABLE_NAME;
import static org.winstarcloud.server.dao.model.ModelConstants.RELATION_TO_ID_PROPERTY;
import static org.winstarcloud.server.dao.model.ModelConstants.RELATION_TO_TYPE_PROPERTY;
import static org.winstarcloud.server.dao.model.ModelConstants.RELATION_TYPE_GROUP_PROPERTY;
import static org.winstarcloud.server.dao.model.ModelConstants.RELATION_TYPE_PROPERTY;

@Data
@Entity
@Table(name = RELATION_TABLE_NAME)
@IdClass(RelationCompositeKey.class)
public final class RelationEntity implements ToData<EntityRelation> {

    @Id
    @Column(name = RELATION_FROM_ID_PROPERTY, columnDefinition = "uuid")
    private UUID fromId;

    @Id
    @Column(name = RELATION_FROM_TYPE_PROPERTY)
    private String fromType;

    @Id
    @Column(name = RELATION_TO_ID_PROPERTY, columnDefinition = "uuid")
    private UUID toId;

    @Id
    @Column(name = RELATION_TO_TYPE_PROPERTY)
    private String toType;

    @Id
    @Column(name = RELATION_TYPE_GROUP_PROPERTY)
    private String relationTypeGroup;

    @Id
    @Column(name = RELATION_TYPE_PROPERTY)
    private String relationType;

    @Convert(converter = JsonConverter.class)
    @Column(name = ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    public RelationEntity() {
        super();
    }

    public RelationEntity(EntityRelation relation) {
        if (relation.getTo() != null) {
            this.toId = relation.getTo().getId();
            this.toType = relation.getTo().getEntityType().name();
        }
        if (relation.getFrom() != null) {
            this.fromId = relation.getFrom().getId();
            this.fromType = relation.getFrom().getEntityType().name();
        }
        this.relationType = relation.getType();
        this.relationTypeGroup = relation.getTypeGroup().name();
        this.additionalInfo = relation.getAdditionalInfo();
    }

    @Override
    public EntityRelation toData() {
        EntityRelation relation = new EntityRelation();
        if (toId != null && toType != null) {
            relation.setTo(EntityIdFactory.getByTypeAndUuid(toType, toId));
        }
        if (fromId != null && fromType != null) {
            relation.setFrom(EntityIdFactory.getByTypeAndUuid(fromType, fromId));
        }
        relation.setType(relationType);
        relation.setTypeGroup(RelationTypeGroup.valueOf(relationTypeGroup));
        relation.setAdditionalInfo(additionalInfo);
        return relation;
    }

}