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
package org.winstarcloud.server.queue.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.queue.TbQueueAdmin;
import org.winstarcloud.server.queue.TbQueueConsumer;
import org.winstarcloud.server.queue.TbQueueProducer;
import org.winstarcloud.server.queue.common.TbProtoQueueMsg;
import org.winstarcloud.server.queue.discovery.TopicService;
import org.winstarcloud.server.queue.settings.TbQueueCoreSettings;
import org.winstarcloud.server.queue.settings.TbQueueVersionControlSettings;
import org.winstarcloud.server.queue.sqs.TbAwsSqsAdmin;
import org.winstarcloud.server.queue.sqs.TbAwsSqsConsumerTemplate;
import org.winstarcloud.server.queue.sqs.TbAwsSqsProducerTemplate;
import org.winstarcloud.server.queue.sqs.TbAwsSqsQueueAttributes;
import org.winstarcloud.server.queue.sqs.TbAwsSqsSettings;

import jakarta.annotation.PreDestroy;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='aws-sqs' && '${service.type:null}'=='tb-vc-executor'")
public class AwsSqsTbVersionControlQueueFactory implements TbVersionControlQueueFactory {

    private final TbAwsSqsSettings sqsSettings;
    private final TbQueueCoreSettings coreSettings;
    private final TbQueueVersionControlSettings vcSettings;
    private final TopicService topicService;


    private final TbQueueAdmin coreAdmin;
    private final TbQueueAdmin notificationAdmin;
    private final TbQueueAdmin vcAdmin;

    public AwsSqsTbVersionControlQueueFactory(TbAwsSqsSettings sqsSettings,
                                              TbQueueCoreSettings coreSettings,
                                              TbQueueVersionControlSettings vcSettings,
                                              TbAwsSqsQueueAttributes sqsQueueAttributes,
                                              TopicService topicService
    ) {
        this.sqsSettings = sqsSettings;
        this.coreSettings = coreSettings;
        this.vcSettings = vcSettings;
        this.topicService = topicService;

        this.coreAdmin = new TbAwsSqsAdmin(sqsSettings, sqsQueueAttributes.getCoreAttributes());
        this.notificationAdmin = new TbAwsSqsAdmin(sqsSettings, sqsQueueAttributes.getNotificationsAttributes());
        this.vcAdmin = new TbAwsSqsAdmin(sqsSettings, sqsQueueAttributes.getVcAttributes());
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportProtos.ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer() {
        return new TbAwsSqsProducerTemplate<>(coreAdmin, sqsSettings, topicService.buildTopicName(coreSettings.getUsageStatsTopic()));
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportProtos.ToCoreNotificationMsg>> createTbCoreNotificationsMsgProducer() {
        return new TbAwsSqsProducerTemplate<>(notificationAdmin, sqsSettings, topicService.buildTopicName(coreSettings.getTopic()));
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<TransportProtos.ToVersionControlServiceMsg>> createToVersionControlMsgConsumer() {
        return new TbAwsSqsConsumerTemplate<>(vcAdmin, sqsSettings, topicService.buildTopicName(vcSettings.getTopic()),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), TransportProtos.ToVersionControlServiceMsg.parseFrom(msg.getData()), msg.getHeaders())
        );
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportProtos.ToHousekeeperServiceMsg>> createHousekeeperMsgProducer() {
        return new TbAwsSqsProducerTemplate<>(coreAdmin, sqsSettings, topicService.buildTopicName(coreSettings.getHousekeeperTopic()));
    }

    @PreDestroy
    private void destroy() {
        if (coreAdmin != null) {
            coreAdmin.destroy();
        }
        if (notificationAdmin != null) {
            notificationAdmin.destroy();
        }
        if (vcAdmin != null) {
            vcAdmin.destroy();
        }
    }
}
