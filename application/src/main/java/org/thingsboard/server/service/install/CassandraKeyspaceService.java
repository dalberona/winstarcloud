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
package org.winstarcloud.server.service.install;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.winstarcloud.server.dao.util.NoSqlAnyDaoNonCloud;

/*
* Create keyspace for Cassandra NoSQL database for non-cloud deployment.
* For cloud service like Astra DBaas admin have to create keyspace manually on cloud UI.
* Then create tokens with database admin role and put it on Winstarcloud parameters.
* Without this service cloud DB will end up with exception like
* UnauthorizedException: Missing correct permission on winstarcloud
* */
@Service
@NoSqlAnyDaoNonCloud
@Profile("install")
public class CassandraKeyspaceService extends CassandraAbstractDatabaseSchemaService
        implements NoSqlKeyspaceService {
    public CassandraKeyspaceService() {
        super("schema-keyspace.cql");
    }
}
