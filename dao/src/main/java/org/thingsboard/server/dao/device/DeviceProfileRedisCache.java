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
package org.winstarcloud.server.dao.device;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.cache.CacheSpecsMap;
import org.winstarcloud.server.cache.RedisTbTransactionalCache;
import org.winstarcloud.server.cache.TBRedisCacheConfiguration;
import org.winstarcloud.server.cache.TbRedisSerializer;
import org.winstarcloud.server.common.data.CacheConstants;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.util.ProtoUtils;
import org.winstarcloud.server.gen.transport.TransportProtos;

@ConditionalOnProperty(prefix = "cache", value = "type", havingValue = "redis")
@Service("DeviceProfileCache")
public class DeviceProfileRedisCache extends RedisTbTransactionalCache<DeviceProfileCacheKey, DeviceProfile> {

    public DeviceProfileRedisCache(TBRedisCacheConfiguration configuration, CacheSpecsMap cacheSpecsMap, RedisConnectionFactory connectionFactory) {
        super(CacheConstants.DEVICE_PROFILE_CACHE, cacheSpecsMap, connectionFactory, configuration, new TbRedisSerializer<DeviceProfileCacheKey, DeviceProfile>() {
            @Override
            public byte[] serialize(DeviceProfile deviceProfile) throws SerializationException {
                return ProtoUtils.toProto(deviceProfile).toByteArray();
            }

            @Override
            public DeviceProfile deserialize(DeviceProfileCacheKey key, byte[] bytes) throws SerializationException {
                try {
                    return ProtoUtils.fromProto(TransportProtos.DeviceProfileProto.parseFrom(bytes));
                } catch (InvalidProtocolBufferException e) {
                    throw new SerializationException(e.getMessage());
                }
            }
        });
    }
}
