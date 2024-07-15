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
package org.winstarcloud.server.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.winstarcloud.rule.engine.api.MailService;
import org.winstarcloud.server.cache.limits.RateLimitService;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.audit.ActionType;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.limit.LimitedApi;
import org.winstarcloud.server.common.data.security.UserCredentials;
import org.winstarcloud.server.common.data.security.event.UserCredentialsInvalidationEvent;
import org.winstarcloud.server.common.data.security.event.UserSessionInvalidationEvent;
import org.winstarcloud.server.common.data.security.model.JwtPair;
import org.winstarcloud.server.common.data.security.model.SecuritySettings;
import org.winstarcloud.server.common.data.security.model.UserPasswordPolicy;
import org.winstarcloud.server.config.annotations.ApiOperation;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.security.auth.rest.RestAuthenticationDetails;
import org.winstarcloud.server.service.security.model.ActivateUserRequest;
import org.winstarcloud.server.service.security.model.ChangePasswordRequest;
import org.winstarcloud.server.service.security.model.ResetPasswordEmailRequest;
import org.winstarcloud.server.service.security.model.ResetPasswordRequest;
import org.winstarcloud.server.service.security.model.SecurityUser;
import org.winstarcloud.server.service.security.model.UserPrincipal;
import org.winstarcloud.server.service.security.model.token.JwtTokenFactory;
import org.winstarcloud.server.service.security.system.SystemSecurityService;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@TbCoreComponent
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class AuthController extends BaseController {

    @Value("${server.rest.rate_limits.reset_password_per_user:5:3600}")
    private String defaultLimitsConfiguration;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenFactory tokenFactory;
    private final MailService mailService;
    private final SystemSecurityService systemSecurityService;
    private final RateLimitService rateLimitService;
    private final ApplicationEventPublisher eventPublisher;


    @ApiOperation(value = "Get current User (getUser)",
            notes = "Get the information about the User which credentials are used to perform this REST API call.")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/auth/user", method = RequestMethod.GET)
    public @ResponseBody
    User getUser() throws WinstarcloudException {
        SecurityUser securityUser = getCurrentUser();
        return userService.findUserById(securityUser.getTenantId(), securityUser.getId());
    }

    @ApiOperation(value = "Logout (logout)",
            notes = "Special API call to record the 'logout' of the user to the Audit Logs. Since platform uses [JWT](https://jwt.io/), the actual logout is the procedure of clearing the [JWT](https://jwt.io/) token on the client side. ")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/auth/logout", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void logout(HttpServletRequest request) throws WinstarcloudException {
        logLogoutAction(request);
    }

    @ApiOperation(value = "Change password for current User (changePassword)",
            notes = "Change the password for the User which credentials are used to perform this REST API call. Be aware that previously generated [JWT](https://jwt.io/) tokens will be still valid until they expire.")
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/auth/changePassword", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public JwtPair changePassword(@Parameter(description = "Change Password Request")
                                  @RequestBody ChangePasswordRequest changePasswordRequest) throws WinstarcloudException {
        String currentPassword = changePasswordRequest.getCurrentPassword();
        String newPassword = changePasswordRequest.getNewPassword();
        SecurityUser securityUser = getCurrentUser();
        UserCredentials userCredentials = userService.findUserCredentialsByUserId(TenantId.SYS_TENANT_ID, securityUser.getId());
        if (!passwordEncoder.matches(currentPassword, userCredentials.getPassword())) {
            throw new WinstarcloudException("Current password doesn't match!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        }
        systemSecurityService.validatePassword(newPassword, userCredentials);
        if (passwordEncoder.matches(newPassword, userCredentials.getPassword())) {
            throw new WinstarcloudException("New password should be different from existing!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        }
        userCredentials.setPassword(passwordEncoder.encode(newPassword));
        userService.replaceUserCredentials(securityUser.getTenantId(), userCredentials);

        eventPublisher.publishEvent(new UserCredentialsInvalidationEvent(securityUser.getId()));
        return tokenFactory.createTokenPair(securityUser);
    }

    @ApiOperation(value = "Get the current User password policy (getUserPasswordPolicy)",
            notes = "API call to get the password policy for the password validation form(s).")
    @RequestMapping(value = "/noauth/userPasswordPolicy", method = RequestMethod.GET)
    @ResponseBody
    public UserPasswordPolicy getUserPasswordPolicy() throws WinstarcloudException {
        SecuritySettings securitySettings =
                checkNotNull(systemSecurityService.getSecuritySettings());
        return securitySettings.getPasswordPolicy();
    }

    @ApiOperation(value = "Check Activate User Token (checkActivateToken)",
            notes = "Checks the activation token and forwards user to 'Create Password' page. " +
                    "If token is valid, returns '303 See Other' (redirect) response code with the correct address of 'Create Password' page and same 'activateToken' specified in the URL parameters. " +
                    "If token is not valid, returns '409 Conflict'.")
    @RequestMapping(value = "/noauth/activate", params = {"activateToken"}, method = RequestMethod.GET)
    public ResponseEntity<String> checkActivateToken(
            @Parameter(description = "The activate token string.")
            @RequestParam(value = "activateToken") String activateToken) {
        HttpHeaders headers = new HttpHeaders();
        HttpStatus responseStatus;
        UserCredentials userCredentials = userService.findUserCredentialsByActivateToken(TenantId.SYS_TENANT_ID, activateToken);
        if (userCredentials != null) {
            String createURI = "/login/createPassword";
            try {
                URI location = new URI(createURI + "?activateToken=" + activateToken);
                headers.setLocation(location);
                responseStatus = HttpStatus.SEE_OTHER;
            } catch (URISyntaxException e) {
                log.error("Unable to create URI with address [{}]", createURI);
                responseStatus = HttpStatus.BAD_REQUEST;
            }
        } else {
            responseStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(headers, responseStatus);
    }

    @ApiOperation(value = "Request reset password email (requestResetPasswordByEmail)",
            notes = "Request to send the reset password email if the user with specified email address is present in the database. " +
                    "Always return '200 OK' status for security purposes.")
    @RequestMapping(value = "/noauth/resetPasswordByEmail", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void requestResetPasswordByEmail(
            @Parameter(description = "The JSON object representing the reset password email request.")
            @RequestBody ResetPasswordEmailRequest resetPasswordByEmailRequest,
            HttpServletRequest request) throws WinstarcloudException {
        try {
            String email = resetPasswordByEmailRequest.getEmail();
            UserCredentials userCredentials = userService.requestPasswordReset(TenantId.SYS_TENANT_ID, email);
            User user = userService.findUserById(TenantId.SYS_TENANT_ID, userCredentials.getUserId());
            String baseUrl = systemSecurityService.getBaseUrl(user.getTenantId(), user.getCustomerId(), request);
            String resetUrl = String.format("%s/api/noauth/resetPassword?resetToken=%s", baseUrl,
                    userCredentials.getResetToken());

            mailService.sendResetPasswordEmailAsync(resetUrl, email);
        } catch (Exception e) {
            log.warn("Error occurred: {}", e.getMessage());
        }
    }

    @ApiOperation(value = "Check password reset token (checkResetToken)",
            notes = "Checks the password reset token and forwards user to 'Reset Password' page. " +
                    "If token is valid, returns '303 See Other' (redirect) response code with the correct address of 'Reset Password' page and same 'resetToken' specified in the URL parameters. " +
                    "If token is not valid, returns '409 Conflict'.")
    @RequestMapping(value = "/noauth/resetPassword", params = {"resetToken"}, method = RequestMethod.GET)
    public ResponseEntity<String> checkResetToken(
            @Parameter(description = "The reset token string.")
            @RequestParam(value = "resetToken") String resetToken) {
        HttpHeaders headers = new HttpHeaders();
        HttpStatus responseStatus;
        String resetURI = "/login/resetPassword";
        UserCredentials userCredentials = userService.findUserCredentialsByResetToken(TenantId.SYS_TENANT_ID, resetToken);

        if (userCredentials != null) {
            if (!rateLimitService.checkRateLimit(LimitedApi.PASSWORD_RESET, userCredentials.getUserId(), defaultLimitsConfiguration)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
            }
            try {
                URI location = new URI(resetURI + "?resetToken=" + resetToken);
                headers.setLocation(location);
                responseStatus = HttpStatus.SEE_OTHER;
            } catch (URISyntaxException e) {
                log.error("Unable to create URI with address [{}]", resetURI);
                responseStatus = HttpStatus.BAD_REQUEST;
            }
        } else {
            responseStatus = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(headers, responseStatus);
    }

    @ApiOperation(value = "Activate User",
            notes = "Checks the activation token and updates corresponding user password in the database. " +
                    "Now the user may start using his password to login. " +
                    "The response already contains the [JWT](https://jwt.io) activation and refresh tokens, " +
                    "to simplify the user activation flow and avoid asking user to input password again after activation. " +
                    "If token is valid, returns the object that contains [JWT](https://jwt.io/) access and refresh tokens. " +
                    "If token is not valid, returns '404 Bad Request'.")
    @RequestMapping(value = "/noauth/activate", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public JwtPair activateUser(
            @Parameter(description = "Activate user request.")
            @RequestBody ActivateUserRequest activateRequest,
            @RequestParam(required = false, defaultValue = "true") boolean sendActivationMail,
            HttpServletRequest request) throws WinstarcloudException {
        String activateToken = activateRequest.getActivateToken();
        String password = activateRequest.getPassword();
        systemSecurityService.validatePassword(password, null);
        String encodedPassword = passwordEncoder.encode(password);
        UserCredentials credentials = userService.activateUserCredentials(TenantId.SYS_TENANT_ID, activateToken, encodedPassword);
        User user = userService.findUserById(TenantId.SYS_TENANT_ID, credentials.getUserId());
        UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
        SecurityUser securityUser = new SecurityUser(user, credentials.isEnabled(), principal);
        userService.setUserCredentialsEnabled(user.getTenantId(), user.getId(), true);
        String baseUrl = systemSecurityService.getBaseUrl(user.getTenantId(), user.getCustomerId(), request);
        String loginUrl = String.format("%s/login", baseUrl);
        String email = user.getEmail();

        if (sendActivationMail) {
            try {
                mailService.sendAccountActivatedEmail(loginUrl, email);
            } catch (Exception e) {
                log.info("Unable to send account activation email [{}]", e.getMessage());
            }
        }

        var tokenPair = tokenFactory.createTokenPair(securityUser);
        systemSecurityService.logLoginAction(user, new RestAuthenticationDetails(request), ActionType.LOGIN, null);
        return tokenPair;
    }

    @ApiOperation(value = "Reset password (resetPassword)",
            notes = "Checks the password reset token and updates the password. " +
                    "If token is valid, returns the object that contains [JWT](https://jwt.io/) access and refresh tokens. " +
                    "If token is not valid, returns '404 Bad Request'.")
    @RequestMapping(value = "/noauth/resetPassword", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public JwtPair resetPassword(
            @Parameter(description = "Reset password request.")
            @RequestBody ResetPasswordRequest resetPasswordRequest,
            HttpServletRequest request) throws WinstarcloudException {
        String resetToken = resetPasswordRequest.getResetToken();
        String password = resetPasswordRequest.getPassword();
        UserCredentials userCredentials = userService.findUserCredentialsByResetToken(TenantId.SYS_TENANT_ID, resetToken);
        if (userCredentials != null) {
            systemSecurityService.validatePassword(password, userCredentials);
            if (passwordEncoder.matches(password, userCredentials.getPassword())) {
                throw new WinstarcloudException("New password should be different from existing!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
            }
            String encodedPassword = passwordEncoder.encode(password);
            userCredentials.setPassword(encodedPassword);
            userCredentials.setResetToken(null);
            userCredentials = userService.replaceUserCredentials(TenantId.SYS_TENANT_ID, userCredentials);
            User user = userService.findUserById(TenantId.SYS_TENANT_ID, userCredentials.getUserId());
            UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
            SecurityUser securityUser = new SecurityUser(user, userCredentials.isEnabled(), principal);
            String baseUrl = systemSecurityService.getBaseUrl(user.getTenantId(), user.getCustomerId(), request);
            String loginUrl = String.format("%s/login", baseUrl);
            String email = user.getEmail();
            mailService.sendPasswordWasResetEmail(loginUrl, email);

            eventPublisher.publishEvent(new UserCredentialsInvalidationEvent(securityUser.getId()));

            return tokenFactory.createTokenPair(securityUser);
        } else {
            throw new WinstarcloudException("Invalid reset token!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    private void logLogoutAction(HttpServletRequest request) throws WinstarcloudException {
        var user = getCurrentUser();
        systemSecurityService.logLoginAction(user, new RestAuthenticationDetails(request), ActionType.LOGOUT, null);
        eventPublisher.publishEvent(new UserSessionInvalidationEvent(user.getSessionId()));
    }

}
