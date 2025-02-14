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
package org.winstarcloud.server.dao.sql.attributes;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.winstarcloud.server.common.data.AttributeScope;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.kv.AttributeKvEntry;
import org.winstarcloud.server.common.stats.StatsFactory;
import org.winstarcloud.server.dao.DaoUtil;
import org.winstarcloud.server.dao.attributes.AttributesDao;
import org.winstarcloud.server.dao.dictionary.KeyDictionaryDao;
import org.winstarcloud.server.dao.model.ModelConstants;
import org.winstarcloud.server.dao.model.sql.AttributeKvCompositeKey;
import org.winstarcloud.server.dao.model.sql.AttributeKvEntity;
import org.winstarcloud.server.dao.sql.JpaAbstractDaoListeningExecutorService;
import org.winstarcloud.server.dao.sql.ScheduledLogExecutorComponent;
import org.winstarcloud.server.dao.sql.TbSqlBlockingQueueParams;
import org.winstarcloud.server.dao.sql.TbSqlBlockingQueueWrapper;
import org.winstarcloud.server.dao.util.SqlDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@SqlDao
public class JpaAttributeDao extends JpaAbstractDaoListeningExecutorService implements AttributesDao {

    @Autowired
    ScheduledLogExecutorComponent logExecutor;

    @Autowired
    private AttributeKvRepository attributeKvRepository;

    @Autowired
    private AttributeKvInsertRepository attributeKvInsertRepository;

    @Autowired
    private StatsFactory statsFactory;

    @Autowired
    private KeyDictionaryDao keyDictionaryDao;

    @Value("${sql.attributes.batch_size:1000}")
    private int batchSize;

    @Value("${sql.attributes.batch_max_delay:100}")
    private long maxDelay;

    @Value("${sql.attributes.stats_print_interval_ms:1000}")
    private long statsPrintIntervalMs;

    @Value("${sql.attributes.batch_threads:4}")
    private int batchThreads;

    @Value("${sql.batch_sort:true}")
    private boolean batchSortEnabled;

    private TbSqlBlockingQueueWrapper<AttributeKvEntity> queue;

    @PostConstruct
    private void init() {
        TbSqlBlockingQueueParams params = TbSqlBlockingQueueParams.builder()
                .logName("Attributes")
                .batchSize(batchSize)
                .maxDelay(maxDelay)
                .statsPrintIntervalMs(statsPrintIntervalMs)
                .statsNamePrefix("attributes")
                .batchSortEnabled(batchSortEnabled)
                .build();

        Function<AttributeKvEntity, Integer> hashcodeFunction = entity -> entity.getId().getEntityId().hashCode();
        queue = new TbSqlBlockingQueueWrapper<>(params, hashcodeFunction, batchThreads, statsFactory);
        queue.init(logExecutor, v -> attributeKvInsertRepository.saveOrUpdate(v),
                Comparator.comparing((AttributeKvEntity attributeKvEntity) -> attributeKvEntity.getId().getEntityId())
                        .thenComparing(attributeKvEntity -> attributeKvEntity.getId().getAttributeType())
                        .thenComparing(attributeKvEntity -> attributeKvEntity.getId().getAttributeKey())
        );
    }

    @PreDestroy
    private void destroy() {
        if (queue != null) {
            queue.destroy();
        }
    }

    @Override
    public Optional<AttributeKvEntry> find(TenantId tenantId, EntityId entityId, AttributeScope attributeScope, String attributeKey) {
        AttributeKvCompositeKey compositeKey =
                getAttributeKvCompositeKey(entityId, attributeScope.getId(), keyDictionaryDao.getOrSaveKeyId(attributeKey));
        Optional<AttributeKvEntity> attributeKvEntityOptional = attributeKvRepository.findById(compositeKey);
        if (attributeKvEntityOptional.isPresent()) {
            AttributeKvEntity attributeKvEntity = attributeKvEntityOptional.get();
            attributeKvEntity.setStrKey(attributeKey);
            return Optional.ofNullable(DaoUtil.getData(attributeKvEntity));
        }
        return Optional.ofNullable(DaoUtil.getData(attributeKvEntityOptional));
    }

    @Override
    public List<AttributeKvEntry> find(TenantId tenantId, EntityId entityId, AttributeScope attributeScope, Collection<String> attributeKeys) {
        List<AttributeKvCompositeKey> compositeKeys =
                attributeKeys
                        .stream()
                        .map(attributeKey ->
                                getAttributeKvCompositeKey(entityId, attributeScope.getId(), keyDictionaryDao.getOrSaveKeyId(attributeKey)))
                        .collect(Collectors.toList());
        List<AttributeKvEntity> attributes = attributeKvRepository.findAllById(compositeKeys);
        attributes.forEach(attributeKvEntity -> attributeKvEntity.setStrKey(keyDictionaryDao.getKey(attributeKvEntity.getId().getAttributeKey())));
        return DaoUtil.convertDataList(Lists.newArrayList(attributes));
    }

    @Override
    public List<AttributeKvEntry> findAll(TenantId tenantId, EntityId entityId, AttributeScope attributeScope) {
        List<AttributeKvEntity> attributes = attributeKvRepository.findAllEntityIdAndAttributeType(
                entityId.getId(),
                attributeScope.getId());
        attributes.forEach(attributeKvEntity -> attributeKvEntity.setStrKey(keyDictionaryDao.getKey(attributeKvEntity.getId().getAttributeKey())));
        return DaoUtil.convertDataList(Lists.newArrayList(attributes));
    }

    @Override
    public List<String> findAllKeysByDeviceProfileId(TenantId tenantId, DeviceProfileId deviceProfileId) {
        if (deviceProfileId != null) {
            return attributeKvRepository.findAllKeysByDeviceProfileId(tenantId.getId(), deviceProfileId.getId())
                    .stream().map(id -> keyDictionaryDao.getKey(id)).collect(Collectors.toList());
        } else {
            return attributeKvRepository.findAllKeysByTenantId(tenantId.getId())
                    .stream().map(id -> keyDictionaryDao.getKey(id)).collect(Collectors.toList());
        }
    }

    @Override
    public List<String> findAllKeysByEntityIds(TenantId tenantId, List<EntityId> entityIds) {
        return attributeKvRepository
                .findAllKeysByEntityIds(entityIds.stream().map(EntityId::getId).collect(Collectors.toList()))
                .stream().map(id -> keyDictionaryDao.getKey(id)).collect(Collectors.toList());
    }

    @Override
    public List<String> findAllKeysByEntityIdsAndAttributeType(TenantId tenantId, List<EntityId> entityIds, String attributeType) {
        return attributeKvRepository
                .findAllKeysByEntityIdsAndAttributeType(entityIds.stream().map(EntityId::getId).collect(Collectors.toList()), AttributeScope.valueOf(attributeType).getId())
                .stream().map(id -> keyDictionaryDao.getKey(id)).collect(Collectors.toList());
    }

    @Override
    public ListenableFuture<String> save(TenantId tenantId, EntityId entityId, AttributeScope attributeScope, AttributeKvEntry attribute) {
        AttributeKvEntity entity = new AttributeKvEntity();
        entity.setId(new AttributeKvCompositeKey(entityId.getId(), attributeScope.getId(), keyDictionaryDao.getOrSaveKeyId(attribute.getKey())));
        entity.setLastUpdateTs(attribute.getLastUpdateTs());
        entity.setStrValue(attribute.getStrValue().orElse(null));
        entity.setDoubleValue(attribute.getDoubleValue().orElse(null));
        entity.setLongValue(attribute.getLongValue().orElse(null));
        entity.setBooleanValue(attribute.getBooleanValue().orElse(null));
        entity.setJsonValue(attribute.getJsonValue().orElse(null));
        return addToQueue(entity, attribute.getKey());
    }

    private ListenableFuture<String> addToQueue(AttributeKvEntity entity, String key) {
        return Futures.transform(queue.add(entity), v -> key, MoreExecutors.directExecutor());
    }

    @Override
    public List<ListenableFuture<String>> removeAll(TenantId tenantId, EntityId entityId, AttributeScope attributeScope, List<String> keys) {
        List<ListenableFuture<String>> futuresList = new ArrayList<>(keys.size());
        for (String key : keys) {
            futuresList.add(service.submit(() -> {
                attributeKvRepository.delete(entityId.getId(), attributeScope.getId(), keyDictionaryDao.getOrSaveKeyId(key));
                return key;
            }));
        }
        return futuresList;
    }

    @Transactional
    @Override
    public List<Pair<AttributeScope, String>> removeAllByEntityId(TenantId tenantId, EntityId entityId) {
        return jdbcTemplate.queryForList("DELETE FROM attribute_kv WHERE entity_id = ? " +
                        "RETURNING attribute_type, attribute_key", entityId.getId()).stream()
                .map(row -> Pair.of(AttributeScope.valueOf((Integer) row.get(ModelConstants.ATTRIBUTE_TYPE_COLUMN)),
                        keyDictionaryDao.getKey((Integer) row.get(ModelConstants.ATTRIBUTE_KEY_COLUMN))))
                .collect(Collectors.toList());
    }

    private AttributeKvCompositeKey getAttributeKvCompositeKey(EntityId entityId, Integer attributeType, Integer attributeKey) {
        return new AttributeKvCompositeKey(
                entityId.getId(),
                attributeType,
                attributeKey);
    }
}
