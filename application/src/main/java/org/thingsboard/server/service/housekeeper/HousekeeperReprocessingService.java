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
package org.winstarcloud.server.service.housekeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.winstarcloud.common.util.WinstarCloudThreadFactory;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.msg.queue.TopicPartitionInfo;
import org.winstarcloud.server.gen.transport.TransportProtos.HousekeeperTaskProto;
import org.winstarcloud.server.gen.transport.TransportProtos.ToHousekeeperServiceMsg;
import org.winstarcloud.server.queue.TbQueueConsumer;
import org.winstarcloud.server.queue.TbQueueProducer;
import org.winstarcloud.server.queue.common.TbProtoQueueMsg;
import org.winstarcloud.server.queue.common.consumer.QueueConsumerManager;
import org.winstarcloud.server.queue.housekeeper.HousekeeperConfig;
import org.winstarcloud.server.queue.provider.TbCoreQueueFactory;
import org.winstarcloud.server.queue.util.AfterStartUp;
import org.winstarcloud.server.queue.util.TbCoreComponent;

import javax.annotation.PreDestroy;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TbCoreComponent
@Service
@Slf4j
public class HousekeeperReprocessingService {

    private final HousekeeperConfig config;
    private final HousekeeperService housekeeperService;
    private final QueueConsumerManager<TbProtoQueueMsg<ToHousekeeperServiceMsg>> consumer;
    private final TbQueueProducer<TbProtoQueueMsg<ToHousekeeperServiceMsg>> producer;
    private final TopicPartitionInfo submitTpi;

    private final ExecutorService consumerExecutor = Executors.newSingleThreadExecutor(WinstarCloudThreadFactory.forName("housekeeper-reprocessing-consumer"));

    public HousekeeperReprocessingService(HousekeeperConfig config,
                                          @Lazy HousekeeperService housekeeperService,
                                          TbCoreQueueFactory queueFactory) {
        this.config = config;
        this.housekeeperService = housekeeperService;
        this.consumer = QueueConsumerManager.<TbProtoQueueMsg<ToHousekeeperServiceMsg>>builder()
                .name("Housekeeper reprocessing")
                .msgPackProcessor(this::processMsgs)
                .pollInterval(config.getPollInterval())
                .consumerCreator(queueFactory::createHousekeeperReprocessingMsgConsumer)
                .consumerExecutor(consumerExecutor)
                .build();
        this.producer = queueFactory.createHousekeeperReprocessingMsgProducer();
        this.submitTpi = TopicPartitionInfo.builder().topic(producer.getDefaultTopic()).build();
    }

    @AfterStartUp(order = AfterStartUp.REGULAR_SERVICE)
    public void afterStartUp() {
        consumer.subscribe(); // Kafka topic for tasks reprocessing has only 1 partition, so only one TB Core will reprocess tasks
        consumer.launch();
    }

    private void processMsgs(List<TbProtoQueueMsg<ToHousekeeperServiceMsg>> msgs, TbQueueConsumer<TbProtoQueueMsg<ToHousekeeperServiceMsg>> consumer) throws Exception {
        Thread.sleep(config.getTaskReprocessingDelay());

        for (TbProtoQueueMsg<ToHousekeeperServiceMsg> msg : msgs) {
            log.trace("Reprocessing task: {}", msg);
            try {
                housekeeperService.processTask(msg.getValue());
            } catch (InterruptedException e) {
                return;
            } catch (Throwable e) {
                log.error("Unexpected error during message reprocessing [{}]", msg, e);
                submitForReprocessing(msg.getValue(), e);
            }
        }
        consumer.commit();
    }

    public void submitForReprocessing(ToHousekeeperServiceMsg msg, Throwable error) {
        HousekeeperTaskProto task = msg.getTask();
        Set<String> errors = new LinkedHashSet<>(task.getErrorsList());
        errors.add(StringUtils.truncate(ExceptionUtils.getStackTrace(error), 1024));
        msg = msg.toBuilder()
                .setTask(task.toBuilder()
                        .setAttempt(task.getAttempt() + 1)
                        .clearErrors().addAllErrors(errors)
                        .build())
                .build();

        log.trace("Submitting for reprocessing: {}", msg);
        producer.send(submitTpi, new TbProtoQueueMsg<>(UUID.randomUUID(), msg), null);
    }

    @PreDestroy
    private void stop() {
        consumer.stop();
        consumerExecutor.shutdownNow();
        log.info("Stopped Housekeeper reprocessing service");
    }

}
