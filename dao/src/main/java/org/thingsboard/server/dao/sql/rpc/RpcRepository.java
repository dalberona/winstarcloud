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
package org.winstarcloud.server.dao.sql.rpc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.winstarcloud.server.common.data.rpc.RpcStatus;
import org.winstarcloud.server.dao.model.sql.RpcEntity;

import java.util.UUID;

public interface RpcRepository extends JpaRepository<RpcEntity, UUID> {
    Page<RpcEntity> findAllByTenantIdAndDeviceId(UUID tenantId, UUID deviceId, Pageable pageable);

    Page<RpcEntity> findAllByTenantIdAndDeviceIdAndStatus(UUID tenantId, UUID deviceId, RpcStatus status, Pageable pageable);

    Page<RpcEntity> findAllByTenantId(UUID tenantId, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM rpc WHERE tenant_id = :tenantId AND created_time < :expirationTime",
            nativeQuery = true)
    int deleteOutdatedRpcByTenantId(@Param("tenantId") UUID tenantId, @Param("expirationTime") Long expirationTime);
}
