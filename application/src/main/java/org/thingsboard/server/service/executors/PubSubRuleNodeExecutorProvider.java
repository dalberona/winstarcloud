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
package org.winstarcloud.server.service.executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.winstarcloud.common.util.ExecutorProvider;
import org.winstarcloud.common.util.WinstarCloudThreadFactory;
import org.winstarcloud.server.queue.util.TbRuleEngineComponent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Lazy
@TbRuleEngineComponent
@Component
public class PubSubRuleNodeExecutorProvider implements ExecutorProvider {

    @Value("${service.rule_engine.pubsub.executor_thread_pool_size}")
    private Integer threadPoolSize;

    /**
    * Refers to com.google.cloud.pubsub.v1.Publisher default executor configuration
    */
    private static final int THREADS_PER_CPU = 5;
    private ScheduledExecutorService executor;

    @PostConstruct
    public void init() {
        if (threadPoolSize == null) {
            threadPoolSize = THREADS_PER_CPU * Runtime.getRuntime().availableProcessors();
        }
        executor = Executors.newScheduledThreadPool(threadPoolSize, WinstarCloudThreadFactory.forName("pubsub-rule-nodes"));
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    @PreDestroy
    private void destroy() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
