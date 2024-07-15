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
package org.winstarcloud.server.common.data.exception;

public class WinstarcloudException extends Exception {

    private static final long serialVersionUID = 1L;

    private WinstarcloudErrorCode errorCode;

    public WinstarcloudException() {
        super();
    }

    public WinstarcloudException(WinstarcloudErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public WinstarcloudException(String message, WinstarcloudErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public WinstarcloudException(String message, Throwable cause, WinstarcloudErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public WinstarcloudException(Throwable cause, WinstarcloudErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public WinstarcloudErrorCode getErrorCode() {
        return errorCode;
    }

}
