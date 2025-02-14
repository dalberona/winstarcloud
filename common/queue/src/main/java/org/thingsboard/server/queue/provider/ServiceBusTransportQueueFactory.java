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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.winstarcloud.server.gen.transport.TransportProtos;
import org.winstarcloud.server.gen.transport.TransportProtos.ToCoreMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ToCoreNotificationMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ToTransportMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.ToUsageStatsServiceMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.TransportApiRequestMsg;
import org.winstarcloud.server.gen.transport.TransportProtos.TransportApiResponseMsg;
import org.winstarcloud.server.queue.TbQueueAdmin;
import org.winstarcloud.server.queue.TbQueueConsumer;
import org.winstarcloud.server.queue.TbQueueProducer;
import org.winstarcloud.server.queue.TbQueueRequestTemplate;
import org.winstarcloud.server.queue.azure.servicebus.TbServiceBusAdmin;
import org.winstarcloud.server.queue.azure.servicebus.TbServiceBusConsumerTemplate;
import org.winstarcloud.server.queue.azure.servicebus.TbServiceBusProducerTemplate;
import org.winstarcloud.server.queue.azure.servicebus.TbServiceBusQueueConfigs;
import org.winstarcloud.server.queue.azure.servicebus.TbServiceBusSettings;
import org.winstarcloud.server.queue.common.DefaultTbQueueRequestTemplate;
import org.winstarcloud.server.queue.common.TbProtoQueueMsg;
import org.winstarcloud.server.queue.discovery.TbServiceInfoProvider;
import org.winstarcloud.server.queue.discovery.TopicService;
import org.winstarcloud.server.queue.settings.TbQueueCoreSettings;
import org.winstarcloud.server.queue.settings.TbQueueRuleEngineSettings;
import org.winstarcloud.server.queue.settings.TbQueueTransportApiSettings;
import org.winstarcloud.server.queue.settings.TbQueueTransportNotificationSettings;

import jakarta.annotation.PreDestroy;

@Component
@ConditionalOnExpression("'${queue.type:null}'=='service-bus' && (('${service.type:null}'=='monolith' && '${transport.api_enabled:true}'=='true') || '${service.type:null}'=='tb-transport')")
@Slf4j
public class ServiceBusTransportQueueFactory implements TbTransportQueueFactory {
    private final TbQueueTransportApiSettings transportApiSettings;
    private final TbQueueTransportNotificationSettings transportNotificationSettings;
    private final TbServiceBusSettings serviceBusSettings;
    private final TbServiceInfoProvider serviceInfoProvider;
    private final TbQueueCoreSettings coreSettings;
    private final TbQueueRuleEngineSettings ruleEngineSettings;
    private final TopicService topicService;

    private final TbQueueAdmin coreAdmin;
    private final TbQueueAdmin transportApiAdmin;
    private final TbQueueAdmin notificationAdmin;
    private final TbQueueAdmin ruleEngineAdmin;

    public ServiceBusTransportQueueFactory(TbQueueTransportApiSettings transportApiSettings,
                                           TbQueueTransportNotificationSettings transportNotificationSettings,
                                           TbServiceBusSettings serviceBusSettings,
                                           TbQueueRuleEngineSettings ruleEngineSettings,
                                           TbServiceInfoProvider serviceInfoProvider,
                                           TbQueueCoreSettings coreSettings,
                                           TbServiceBusQueueConfigs serviceBusQueueConfigs,
                                           TopicService topicService) {
        this.transportApiSettings = transportApiSettings;
        this.transportNotificationSettings = transportNotificationSettings;
        this.serviceBusSettings = serviceBusSettings;
        this.serviceInfoProvider = serviceInfoProvider;
        this.coreSettings = coreSettings;
        this.ruleEngineSettings = ruleEngineSettings;
        this.topicService = topicService;

        this.coreAdmin = new TbServiceBusAdmin(serviceBusSettings, serviceBusQueueConfigs.getCoreConfigs());
        this.transportApiAdmin = new TbServiceBusAdmin(serviceBusSettings, serviceBusQueueConfigs.getTransportApiConfigs());
        this.notificationAdmin = new TbServiceBusAdmin(serviceBusSettings, serviceBusQueueConfigs.getNotificationsConfigs());
        this.ruleEngineAdmin = new TbServiceBusAdmin(serviceBusSettings, serviceBusQueueConfigs.getRuleEngineConfigs());
    }

    @Override
    public TbQueueRequestTemplate<TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> createTransportApiRequestTemplate() {
        TbQueueProducer<TbProtoQueueMsg<TransportApiRequestMsg>> producerTemplate =
                new TbServiceBusProducerTemplate<>(transportApiAdmin, serviceBusSettings, topicService.buildTopicName(transportApiSettings.getRequestsTopic()));

        TbQueueConsumer<TbProtoQueueMsg<TransportApiResponseMsg>> consumerTemplate =
                new TbServiceBusConsumerTemplate<>(transportApiAdmin, serviceBusSettings,
                        topicService.buildTopicName(transportApiSettings.getResponsesTopic() + "." + serviceInfoProvider.getServiceId()),
                        msg -> new TbProtoQueueMsg<>(msg.getKey(), TransportApiResponseMsg.parseFrom(msg.getData()), msg.getHeaders()));

        DefaultTbQueueRequestTemplate.DefaultTbQueueRequestTemplateBuilder
                <TbProtoQueueMsg<TransportApiRequestMsg>, TbProtoQueueMsg<TransportApiResponseMsg>> templateBuilder = DefaultTbQueueRequestTemplate.builder();
        templateBuilder.queueAdmin(transportApiAdmin);
        templateBuilder.requestTemplate(producerTemplate);
        templateBuilder.responseTemplate(consumerTemplate);
        templateBuilder.maxPendingRequests(transportApiSettings.getMaxPendingRequests());
        templateBuilder.maxRequestTimeout(transportApiSettings.getMaxRequestsTimeout());
        templateBuilder.pollInterval(transportApiSettings.getResponsePollInterval());
        return templateBuilder.build();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> createRuleEngineMsgProducer() {
        return new TbServiceBusProducerTemplate<>(ruleEngineAdmin, serviceBusSettings, topicService.buildTopicName(ruleEngineSettings.getTopic()));
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> createTbCoreMsgProducer() {
        return new TbServiceBusProducerTemplate<>(coreAdmin, serviceBusSettings, topicService.buildTopicName(coreSettings.getTopic()));
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreNotificationMsg>> createTbCoreNotificationsMsgProducer() {
        return new TbServiceBusProducerTemplate<>(notificationAdmin, serviceBusSettings, topicService.buildTopicName(coreSettings.getTopic()));
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToTransportMsg>> createTransportNotificationsConsumer() {
        return new TbServiceBusConsumerTemplate<>(notificationAdmin, serviceBusSettings,
                topicService.buildTopicName(transportNotificationSettings.getNotificationsTopic() + "." + serviceInfoProvider.getServiceId()),
                msg -> new TbProtoQueueMsg<>(msg.getKey(), ToTransportMsg.parseFrom(msg.getData()), msg.getHeaders()));
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToUsageStatsServiceMsg>> createToUsageStatsServiceMsgProducer() {
        return new TbServiceBusProducerTemplate<>(coreAdmin, serviceBusSettings, topicService.buildTopicName(coreSettings.getUsageStatsTopic()));
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportProtos.ToHousekeeperServiceMsg>> createHousekeeperMsgProducer() {
        return new TbServiceBusProducerTemplate<>(coreAdmin, serviceBusSettings, topicService.buildTopicName(coreSettings.getHousekeeperTopic()));
    }

    @PreDestroy
    private void destroy() {
        if (coreAdmin != null) {
            coreAdmin.destroy();
        }
        if (transportApiAdmin != null) {
            transportApiAdmin.destroy();
        }
        if (notificationAdmin != null) {
            notificationAdmin.destroy();
        }
        if (ruleEngineAdmin != null) {
            ruleEngineAdmin.destroy();
        }
    }
}
