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
package org.winstarcloud.server.dao.edge;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.winstarcloud.common.util.WinstarCloudThreadFactory;
import org.winstarcloud.server.cache.limits.RateLimitService;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.edge.EdgeEvent;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.limit.LimitedApi;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.TimePageLink;
import org.winstarcloud.server.common.msg.tools.TbRateLimitsException;
import org.winstarcloud.server.dao.eventsourcing.SaveEntityEvent;
import org.winstarcloud.server.dao.service.DataValidator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BaseEdgeEventService implements EdgeEventService {

    private final EdgeEventDao edgeEventDao;
    private final RateLimitService rateLimitService;
    private final DataValidator<EdgeEvent> edgeEventValidator;

    private final ApplicationEventPublisher eventPublisher;

    private ExecutorService edgeEventExecutor;

    @PostConstruct
    public void initExecutor() {
        edgeEventExecutor = Executors.newSingleThreadExecutor(WinstarCloudThreadFactory.forName("edge-event-service"));
    }

    @PreDestroy
    public void shutdownExecutor() {
        if (edgeEventExecutor != null) {
            edgeEventExecutor.shutdown();
        }
    }

    @Override
    public ListenableFuture<Void> saveAsync(EdgeEvent edgeEvent) {
        if (!rateLimitService.checkRateLimit(LimitedApi.EDGE_EVENTS, edgeEvent.getTenantId())) {
            throw new TbRateLimitsException(EntityType.TENANT);
        }
        if (!rateLimitService.checkRateLimit(LimitedApi.EDGE_EVENTS_PER_EDGE, edgeEvent.getTenantId(), edgeEvent.getEdgeId())) {
            throw new TbRateLimitsException(EntityType.EDGE);
        }
        edgeEventValidator.validate(edgeEvent, EdgeEvent::getTenantId);

        ListenableFuture<Void> saveFuture = edgeEventDao.saveAsync(edgeEvent);

        Futures.addCallback(saveFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(Void result) {
                eventPublisher.publishEvent(SaveEntityEvent.builder().tenantId(edgeEvent.getTenantId())
                        .entity(edgeEvent).entityId(edgeEvent.getEdgeId()).build());
            }

            @Override
            public void onFailure(@NotNull Throwable throwable) {}
        }, edgeEventExecutor);

        return saveFuture;
    }

    @Override
    public PageData<EdgeEvent> findEdgeEvents(TenantId tenantId, EdgeId edgeId, Long seqIdStart, Long seqIdEnd, TimePageLink pageLink) {
        return edgeEventDao.findEdgeEvents(tenantId.getId(), edgeId, seqIdStart, seqIdEnd, pageLink);
    }

    @Override
    public void cleanupEvents(long ttl) {
        edgeEventDao.cleanupEvents(ttl);
    }
}
