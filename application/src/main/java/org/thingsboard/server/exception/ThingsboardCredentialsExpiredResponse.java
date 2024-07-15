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
package org.winstarcloud.server.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;

@Schema
public class WinstarcloudCredentialsExpiredResponse extends WinstarcloudErrorResponse {

    private final String resetToken;

    protected WinstarcloudCredentialsExpiredResponse(String message, String resetToken) {
        super(message, WinstarcloudErrorCode.CREDENTIALS_EXPIRED, HttpStatus.UNAUTHORIZED);
        this.resetToken = resetToken;
    }

    public static WinstarcloudCredentialsExpiredResponse of(final String message, final String resetToken) {
        return new WinstarcloudCredentialsExpiredResponse(message, resetToken);
    }

    @Schema(description = "Password reset token", accessMode = Schema.AccessMode.READ_ONLY)
    public String getResetToken() {
        return resetToken;
    }
}
