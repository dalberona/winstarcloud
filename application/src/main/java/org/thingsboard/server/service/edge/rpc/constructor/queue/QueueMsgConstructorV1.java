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
package org.winstarcloud.server.service.edge.rpc.constructor.queue;

import org.springframework.stereotype.Component;
import org.winstarcloud.server.common.data.id.QueueId;
import org.winstarcloud.server.common.data.queue.ProcessingStrategy;
import org.winstarcloud.server.common.data.queue.Queue;
import org.winstarcloud.server.common.data.queue.SubmitStrategy;
import org.winstarcloud.server.gen.edge.v1.ProcessingStrategyProto;
import org.winstarcloud.server.gen.edge.v1.QueueUpdateMsg;
import org.winstarcloud.server.gen.edge.v1.SubmitStrategyProto;
import org.winstarcloud.server.gen.edge.v1.UpdateMsgType;
import org.winstarcloud.server.queue.util.TbCoreComponent;

@Component
@TbCoreComponent
public class QueueMsgConstructorV1 extends BaseQueueMsgConstructor {

    @Override
    public QueueUpdateMsg constructQueueUpdatedMsg(UpdateMsgType msgType, Queue queue) {
        return QueueUpdateMsg.newBuilder()
                .setMsgType(msgType)
                .setIdMSB(queue.getId().getId().getMostSignificantBits())
                .setIdLSB(queue.getId().getId().getLeastSignificantBits())
                .setTenantIdMSB(queue.getTenantId().getId().getMostSignificantBits())
                .setTenantIdLSB(queue.getTenantId().getId().getLeastSignificantBits())
                .setName(queue.getName())
                .setTopic(queue.getTopic())
                .setPollInterval(queue.getPollInterval())
                .setPartitions(queue.getPartitions())
                .setConsumerPerPartition(queue.isConsumerPerPartition())
                .setPackProcessingTimeout(queue.getPackProcessingTimeout())
                .setSubmitStrategy(createSubmitStrategyProto(queue.getSubmitStrategy()))
                .setProcessingStrategy(createProcessingStrategyProto(queue.getProcessingStrategy())).build();
    }

    private ProcessingStrategyProto createProcessingStrategyProto(ProcessingStrategy processingStrategy) {
        return ProcessingStrategyProto.newBuilder()
                .setType(processingStrategy.getType().name())
                .setRetries(processingStrategy.getRetries())
                .setFailurePercentage(processingStrategy.getFailurePercentage())
                .setPauseBetweenRetries(processingStrategy.getPauseBetweenRetries())
                .setMaxPauseBetweenRetries(processingStrategy.getMaxPauseBetweenRetries())
                .build();
    }

    private SubmitStrategyProto createSubmitStrategyProto(SubmitStrategy submitStrategy) {
        return SubmitStrategyProto.newBuilder()
                .setType(submitStrategy.getType().name())
                .setBatchSize(submitStrategy.getBatchSize())
                .build();
    }

    public QueueUpdateMsg constructQueueDeleteMsg(QueueId queueId) {
        return QueueUpdateMsg.newBuilder()
                .setMsgType(UpdateMsgType.ENTITY_DELETED_RPC_MESSAGE)
                .setIdMSB(queueId.getId().getMostSignificantBits())
                .setIdLSB(queueId.getId().getLeastSignificantBits()).build();
    }
}
