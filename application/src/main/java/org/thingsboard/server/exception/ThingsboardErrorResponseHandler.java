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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.msg.tools.TbRateLimitsException;
import org.winstarcloud.server.service.security.exception.AuthMethodNotSupportedException;
import org.winstarcloud.server.service.security.exception.JwtExpiredTokenException;
import org.winstarcloud.server.service.security.exception.UserPasswordExpiredException;
import org.winstarcloud.server.service.security.exception.UserPasswordNotValidException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RestControllerAdvice
public class WinstarcloudErrorResponseHandler extends ResponseEntityExceptionHandler implements AccessDeniedHandler, ErrorController {

    private static final Map<HttpStatus, WinstarcloudErrorCode> statusToErrorCodeMap = new HashMap<>();

    static {
        statusToErrorCodeMap.put(HttpStatus.BAD_REQUEST, WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        statusToErrorCodeMap.put(HttpStatus.UNAUTHORIZED, WinstarcloudErrorCode.AUTHENTICATION);
        statusToErrorCodeMap.put(HttpStatus.FORBIDDEN, WinstarcloudErrorCode.PERMISSION_DENIED);
        statusToErrorCodeMap.put(HttpStatus.NOT_FOUND, WinstarcloudErrorCode.ITEM_NOT_FOUND);
        statusToErrorCodeMap.put(HttpStatus.METHOD_NOT_ALLOWED, WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        statusToErrorCodeMap.put(HttpStatus.NOT_ACCEPTABLE, WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        statusToErrorCodeMap.put(HttpStatus.UNSUPPORTED_MEDIA_TYPE, WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        statusToErrorCodeMap.put(HttpStatus.TOO_MANY_REQUESTS, WinstarcloudErrorCode.TOO_MANY_REQUESTS);
        statusToErrorCodeMap.put(HttpStatus.INTERNAL_SERVER_ERROR, WinstarcloudErrorCode.GENERAL);
        statusToErrorCodeMap.put(HttpStatus.SERVICE_UNAVAILABLE, WinstarcloudErrorCode.GENERAL);
    }

    private static final Map<WinstarcloudErrorCode, HttpStatus> errorCodeToStatusMap = new HashMap<>();

    static {
        errorCodeToStatusMap.put(WinstarcloudErrorCode.GENERAL, HttpStatus.INTERNAL_SERVER_ERROR);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.JWT_TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.CREDENTIALS_EXPIRED, HttpStatus.UNAUTHORIZED);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.INVALID_ARGUMENTS, HttpStatus.BAD_REQUEST);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.BAD_REQUEST_PARAMS, HttpStatus.BAD_REQUEST);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.ITEM_NOT_FOUND, HttpStatus.NOT_FOUND);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.TOO_MANY_UPDATES, HttpStatus.TOO_MANY_REQUESTS);
        errorCodeToStatusMap.put(WinstarcloudErrorCode.SUBSCRIPTION_VIOLATION, HttpStatus.FORBIDDEN);
    }

    private static WinstarcloudErrorCode statusToErrorCode(HttpStatus status) {
        return statusToErrorCodeMap.getOrDefault(status, WinstarcloudErrorCode.GENERAL);
    }

    private static HttpStatus errorCodeToStatus(WinstarcloudErrorCode errorCode) {
        return errorCodeToStatusMap.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping("/error")
    public ResponseEntity<Object> handleError(HttpServletRequest request) {
        HttpStatus httpStatus = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                .map(status -> HttpStatus.resolve(Integer.parseInt(status.toString())))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        String errorMessage = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION))
                .map(e -> (ExceptionUtils.getMessage((Throwable) e)))
                .orElse(httpStatus.getReasonPhrase());
        return new ResponseEntity<>(WinstarcloudErrorResponse.of(errorMessage, statusToErrorCode(httpStatus), httpStatus), httpStatus);
    }

    @Override
    @ExceptionHandler(AccessDeniedException.class)
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException,
            ServletException {
        if (!response.isCommitted()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            JacksonUtil.writeValue(response.getWriter(),
                    WinstarcloudErrorResponse.of("You don't have permission to perform this operation!",
                            WinstarcloudErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN));
        }
    }

    @ExceptionHandler(Exception.class)
    public void handle(Exception exception, HttpServletResponse response) {
        log.debug("Processing exception {}", exception.getMessage(), exception);
        if (!response.isCommitted()) {
            try {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                if (exception instanceof WinstarcloudException) {
                    WinstarcloudException winstarcloudException = (WinstarcloudException) exception;
                    if (winstarcloudException.getErrorCode() == WinstarcloudErrorCode.SUBSCRIPTION_VIOLATION) {
                        handleSubscriptionException((WinstarcloudException) exception, response);
                    } else {
                        handleWinstarcloudException((WinstarcloudException) exception, response);
                    }
                } else if (exception instanceof TbRateLimitsException) {
                    handleRateLimitException(response, (TbRateLimitsException) exception);
                } else if (exception instanceof AccessDeniedException) {
                    handleAccessDeniedException(response);
                } else if (exception instanceof AuthenticationException) {
                    handleAuthenticationException((AuthenticationException) exception, response);
                } else {
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of(exception.getMessage(),
                            WinstarcloudErrorCode.GENERAL, HttpStatus.INTERNAL_SERVER_ERROR));
                }
            } catch (IOException e) {
                log.error("Can't handle exception", e);
            }
        }
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body,
            HttpHeaders headers, HttpStatusCode statusCode,
            WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(statusCode)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        WinstarcloudErrorCode errorCode = statusToErrorCode((HttpStatus) statusCode);
        return new ResponseEntity<>(WinstarcloudErrorResponse.of(ex.getMessage(), errorCode, (HttpStatus) statusCode), headers, statusCode);
    }

    private void handleWinstarcloudException(WinstarcloudException winstarcloudException, HttpServletResponse response) throws IOException {
        WinstarcloudErrorCode errorCode = winstarcloudException.getErrorCode();
        HttpStatus status = errorCodeToStatus(errorCode);
        response.setStatus(status.value());
        JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of(winstarcloudException.getMessage(), errorCode, status));
    }

    private void handleRateLimitException(HttpServletResponse response, TbRateLimitsException exception) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        String message = "Too many requests for current " + exception.getEntityType().name().toLowerCase() + "!";
        JacksonUtil.writeValue(response.getWriter(),
                WinstarcloudErrorResponse.of(message,
                        WinstarcloudErrorCode.TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS));
    }

    private void handleSubscriptionException(WinstarcloudException subscriptionException, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        JacksonUtil.writeValue(response.getWriter(),
                JacksonUtil.fromBytes(((HttpClientErrorException) subscriptionException.getCause()).getResponseBodyAsByteArray(), Object.class));
    }

    private void handleAccessDeniedException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        JacksonUtil.writeValue(response.getWriter(),
                WinstarcloudErrorResponse.of("You don't have permission to perform this operation!",
                        WinstarcloudErrorCode.PERMISSION_DENIED, HttpStatus.FORBIDDEN));

    }

    private void handleAuthenticationException(AuthenticationException authenticationException, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        if (authenticationException instanceof BadCredentialsException || authenticationException instanceof UsernameNotFoundException) {
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of("Invalid username or password", WinstarcloudErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
        } else if (authenticationException instanceof DisabledException) {
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of("User account is not active", WinstarcloudErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
        } else if (authenticationException instanceof LockedException) {
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of("User account is locked due to security policy", WinstarcloudErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
        } else if (authenticationException instanceof JwtExpiredTokenException) {
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of("Token has expired", WinstarcloudErrorCode.JWT_TOKEN_EXPIRED, HttpStatus.UNAUTHORIZED));
        } else if (authenticationException instanceof AuthMethodNotSupportedException) {
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of(authenticationException.getMessage(), WinstarcloudErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
        } else if (authenticationException instanceof UserPasswordExpiredException) {
            UserPasswordExpiredException expiredException = (UserPasswordExpiredException) authenticationException;
            String resetToken = expiredException.getResetToken();
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudCredentialsExpiredResponse.of(expiredException.getMessage(), resetToken));
        } else if (authenticationException instanceof UserPasswordNotValidException) {
            UserPasswordNotValidException expiredException = (UserPasswordNotValidException) authenticationException;
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudCredentialsViolationResponse.of(expiredException.getMessage()));
        } else {
            JacksonUtil.writeValue(response.getWriter(), WinstarcloudErrorResponse.of("Authentication failed", WinstarcloudErrorCode.AUTHENTICATION, HttpStatus.UNAUTHORIZED));
        }
    }

}
