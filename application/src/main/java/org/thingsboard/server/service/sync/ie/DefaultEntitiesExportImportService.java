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
package org.winstarcloud.server.service.sync.ie;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.ExportableEntity;
import org.winstarcloud.server.common.data.audit.ActionType;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.limit.LimitedApi;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.common.data.sync.ie.EntityExportData;
import org.winstarcloud.server.common.data.sync.ie.EntityImportResult;
import org.winstarcloud.server.common.data.util.ThrowingRunnable;
import org.winstarcloud.server.dao.exception.DataValidationException;
import org.winstarcloud.server.dao.relation.RelationService;
import org.winstarcloud.server.cache.limits.RateLimitService;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.entitiy.TbLogEntityActionService;
import org.winstarcloud.server.service.sync.ie.exporting.EntityExportService;
import org.winstarcloud.server.service.sync.ie.exporting.impl.BaseEntityExportService;
import org.winstarcloud.server.service.sync.ie.exporting.impl.DefaultEntityExportService;
import org.winstarcloud.server.service.sync.ie.importing.EntityImportService;
import org.winstarcloud.server.service.sync.ie.importing.impl.MissingEntityException;
import org.winstarcloud.server.service.sync.vc.LoadEntityException;
import org.winstarcloud.server.service.sync.vc.data.EntitiesExportCtx;
import org.winstarcloud.server.service.sync.vc.data.EntitiesImportCtx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@TbCoreComponent
@RequiredArgsConstructor
@Slf4j
public class DefaultEntitiesExportImportService implements EntitiesExportImportService {

    private final Map<EntityType, EntityExportService<?, ?, ?>> exportServices = new HashMap<>();
    private final Map<EntityType, EntityImportService<?, ?, ?>> importServices = new HashMap<>();

    private final RelationService relationService;
    private final RateLimitService rateLimitService;
    private final TbLogEntityActionService logEntityActionService;

    protected static final List<EntityType> SUPPORTED_ENTITY_TYPES = List.of(
            EntityType.CUSTOMER, EntityType.RULE_CHAIN, EntityType.TB_RESOURCE,
            EntityType.DASHBOARD, EntityType.ASSET_PROFILE, EntityType.ASSET,
            EntityType.DEVICE_PROFILE, EntityType.DEVICE,
            EntityType.ENTITY_VIEW, EntityType.WIDGET_TYPE, EntityType.WIDGETS_BUNDLE,
            EntityType.NOTIFICATION_TEMPLATE, EntityType.NOTIFICATION_TARGET, EntityType.NOTIFICATION_RULE
    );


    @Override
    public <E extends ExportableEntity<I>, I extends EntityId> EntityExportData<E> exportEntity(EntitiesExportCtx<?> ctx, I entityId) throws WinstarcloudException {
        if (!rateLimitService.checkRateLimit(LimitedApi.ENTITY_EXPORT, ctx.getTenantId())) {
            throw new WinstarcloudException("Rate limit for entities export is exceeded", WinstarcloudErrorCode.TOO_MANY_REQUESTS);
        }

        EntityType entityType = entityId.getEntityType();
        EntityExportService<I, E, EntityExportData<E>> exportService = getExportService(entityType);

        return exportService.getExportData(ctx, entityId);
    }

    @Override
    public <E extends ExportableEntity<I>, I extends EntityId> EntityImportResult<E> importEntity(EntitiesImportCtx ctx, EntityExportData<E> exportData) throws WinstarcloudException {
        if (!rateLimitService.checkRateLimit(LimitedApi.ENTITY_IMPORT, ctx.getTenantId())) {
            throw new WinstarcloudException("Rate limit for entities import is exceeded", WinstarcloudErrorCode.TOO_MANY_REQUESTS);
        }
        if (exportData.getEntity() == null || exportData.getEntity().getId() == null) {
            throw new DataValidationException("Invalid entity data");
        }

        EntityType entityType = exportData.getEntityType();
        EntityImportService<I, E, EntityExportData<E>> importService = getImportService(entityType);

        EntityImportResult<E> importResult = importService.importEntity(ctx, exportData);
        ctx.putInternalId(exportData.getExternalId(), importResult.getSavedEntity().getId());

        ctx.addReferenceCallback(exportData.getExternalId(), importResult.getSaveReferencesCallback());
        if (ctx.isRollbackOnError()) {
            ctx.addEventCallback(importResult.getSendEventsCallback());
        } else {
            importResult.getSendEventsCallback().run();
        }
        return importResult;
    }

    @Override
    public void saveReferencesAndRelations(EntitiesImportCtx ctx) throws WinstarcloudException {
        for (Map.Entry<EntityId, ThrowingRunnable> callbackEntry : ctx.getReferenceCallbacks().entrySet()) {
            EntityId externalId = callbackEntry.getKey();
            ThrowingRunnable saveReferencesCallback = callbackEntry.getValue();
            try {
                saveReferencesCallback.run();
            } catch (MissingEntityException e) {
                throw new LoadEntityException(externalId, e);
            }
        }

        relationService.saveRelations(ctx.getTenantId(), new ArrayList<>(ctx.getRelations()));

        for (EntityRelation relation : ctx.getRelations()) {
            logEntityActionService.logEntityRelationAction(ctx.getTenantId(), null,
                    relation, ctx.getUser(), ActionType.RELATION_ADD_OR_UPDATE, null, relation);
        }
    }


    @Override
    public Comparator<EntityType> getEntityTypeComparatorForImport() {
        return Comparator.comparing(SUPPORTED_ENTITY_TYPES::indexOf);
    }


    @SuppressWarnings("unchecked")
    private <I extends EntityId, E extends ExportableEntity<I>, D extends EntityExportData<E>> EntityExportService<I, E, D> getExportService(EntityType entityType) {
        EntityExportService<?, ?, ?> exportService = exportServices.get(entityType);
        if (exportService == null) {
            throw new IllegalArgumentException("Export for entity type " + entityType + " is not supported");
        }
        return (EntityExportService<I, E, D>) exportService;
    }

    @SuppressWarnings("unchecked")
    private <I extends EntityId, E extends ExportableEntity<I>, D extends EntityExportData<E>> EntityImportService<I, E, D> getImportService(EntityType entityType) {
        EntityImportService<?, ?, ?> importService = importServices.get(entityType);
        if (importService == null) {
            throw new IllegalArgumentException("Import for entity type " + entityType + " is not supported");
        }
        return (EntityImportService<I, E, D>) importService;
    }

    @Autowired
    private void setExportServices(DefaultEntityExportService<?, ?, ?> defaultExportService,
                                   Collection<BaseEntityExportService<?, ?, ?>> exportServices) {
        exportServices.stream()
                .sorted(Comparator.comparing(exportService -> exportService.getSupportedEntityTypes().size(), Comparator.reverseOrder()))
                .forEach(exportService -> {
                    exportService.getSupportedEntityTypes().forEach(entityType -> {
                        this.exportServices.put(entityType, exportService);
                    });
                });
        SUPPORTED_ENTITY_TYPES.forEach(entityType -> {
            this.exportServices.putIfAbsent(entityType, defaultExportService);
        });
    }

    @Autowired
    private void setImportServices(Collection<EntityImportService<?, ?, ?>> importServices) {
        importServices.forEach(entityImportService -> {
            this.importServices.put(entityImportService.getEntityType(), entityImportService);
        });
    }

}
