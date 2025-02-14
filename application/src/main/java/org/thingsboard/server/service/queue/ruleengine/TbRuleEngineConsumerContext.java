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
package org.winstarcloud.server.service.queue.ruleengine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.actors.ActorSystemContext;
import org.winstarcloud.server.common.stats.StatsFactory;
import org.winstarcloud.server.queue.TbQueueAdmin;
import org.winstarcloud.server.queue.discovery.PartitionService;
import org.winstarcloud.server.queue.discovery.TbServiceInfoProvider;
import org.winstarcloud.server.queue.provider.TbQueueProducerProvider;
import org.winstarcloud.server.queue.provider.TbRuleEngineQueueFactory;
import org.winstarcloud.server.queue.util.TbRuleEngineComponent;
import org.winstarcloud.server.service.queue.processing.TbRuleEngineProcessingStrategyFactory;
import org.winstarcloud.server.service.queue.processing.TbRuleEngineSubmitStrategyFactory;
import org.winstarcloud.server.service.stats.RuleEngineStatisticsService;

@Component
@TbRuleEngineComponent
@Slf4j
@Data
public class TbRuleEngineConsumerContext {

    @Value("${queue.rule-engine.poll-interval}")
    private long pollDuration;
    @Value("${queue.rule-engine.pack-processing-timeout}")
    private long packProcessingTimeout;
    @Value("${queue.rule-engine.stats.enabled:true}")
    private boolean statsEnabled;
    @Value("${queue.rule-engine.prometheus-stats.enabled:false}")
    private boolean prometheusStatsEnabled;
    @Value("${queue.rule-engine.topic-deletion-delay:15}")
    private int topicDeletionDelayInSec;
    @Value("${queue.rule-engine.management-thread-pool-size:12}")
    private int mgmtThreadPoolSize;

    private final ActorSystemContext actorContext;
    private final StatsFactory statsFactory;
    private final TbRuleEngineSubmitStrategyFactory submitStrategyFactory;
    private final TbRuleEngineProcessingStrategyFactory processingStrategyFactory;
    private final TbRuleEngineQueueFactory queueFactory;
    private final RuleEngineStatisticsService statisticsService;
    private final TbServiceInfoProvider serviceInfoProvider;
    private final PartitionService partitionService;
    private final TbQueueProducerProvider producerProvider;
    private final TbQueueAdmin queueAdmin;

}
