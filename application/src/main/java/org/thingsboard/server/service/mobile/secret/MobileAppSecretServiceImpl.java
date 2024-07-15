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
package org.winstarcloud.server.service.mobile.secret;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.winstarcloud.server.cache.TbCacheValueWrapper;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.security.model.JwtPair;
import org.winstarcloud.server.dao.entity.AbstractCachedService;
import org.winstarcloud.server.service.security.model.SecurityUser;
import org.winstarcloud.server.service.security.model.token.JwtTokenFactory;
import org.winstarcloud.server.service.security.system.SystemSecurityService;

import static org.winstarcloud.server.service.security.system.DefaultSystemSecurityService.DEFAULT_MOBILE_SECRET_KEY_LENGTH;

@Service
@Slf4j
@RequiredArgsConstructor
public class MobileAppSecretServiceImpl extends AbstractCachedService<String, JwtPair, MobileSecretEvictEvent> implements MobileAppSecretService {

    private final JwtTokenFactory tokenFactory;
    private final SystemSecurityService systemSecurityService;

    @Override
    public String generateMobileAppSecret(SecurityUser securityUser) {
        log.trace("Executing generateSecret for user [{}]", securityUser.getId());
        Integer mobileSecretKeyLength = systemSecurityService.getSecuritySettings().getMobileSecretKeyLength();
        String secret = StringUtils.generateSafeToken(mobileSecretKeyLength == null ? DEFAULT_MOBILE_SECRET_KEY_LENGTH : mobileSecretKeyLength);
        cache.put(secret, tokenFactory.createTokenPair(securityUser));
        return secret;
    }

    @Override
    public JwtPair getJwtPair(String secret) throws WinstarcloudException {
        TbCacheValueWrapper<JwtPair> jwtPair = cache.get(secret);
        if (jwtPair != null) {
            return jwtPair.get();
        } else {
            throw new WinstarcloudException("Jwt token not found or expired!", WinstarcloudErrorCode.JWT_TOKEN_EXPIRED);
        }
    }

    @TransactionalEventListener(classes = MobileSecretEvictEvent.class)
    @Override
    public void handleEvictEvent(MobileSecretEvictEvent event) {
        cache.evict(event.getSecret());
    }
}
