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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.ListenableFuture;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.context.request.async.DeferredResult;
import org.winstarcloud.common.util.DonAsynchron;
import org.winstarcloud.server.cluster.TbClusterService;
import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.Dashboard;
import org.winstarcloud.server.common.data.DashboardInfo;
import org.winstarcloud.server.common.data.Device;
import org.winstarcloud.server.common.data.DeviceInfo;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.EntityType;
import org.winstarcloud.server.common.data.EntityView;
import org.winstarcloud.server.common.data.EntityViewInfo;
import org.winstarcloud.server.common.data.HasName;
import org.winstarcloud.server.common.data.HasTenantId;
import org.winstarcloud.server.common.data.OtaPackage;
import org.winstarcloud.server.common.data.OtaPackageInfo;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.TbResource;
import org.winstarcloud.server.common.data.TbResourceInfo;
import org.winstarcloud.server.common.data.Tenant;
import org.winstarcloud.server.common.data.TenantInfo;
import org.winstarcloud.server.common.data.TenantProfile;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.alarm.Alarm;
import org.winstarcloud.server.common.data.alarm.AlarmComment;
import org.winstarcloud.server.common.data.alarm.AlarmInfo;
import org.winstarcloud.server.common.data.asset.Asset;
import org.winstarcloud.server.common.data.asset.AssetInfo;
import org.winstarcloud.server.common.data.asset.AssetProfile;
import org.winstarcloud.server.common.data.audit.ActionType;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.edge.EdgeInfo;
import org.winstarcloud.server.common.data.exception.WinstarcloudErrorCode;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.AlarmCommentId;
import org.winstarcloud.server.common.data.id.AlarmId;
import org.winstarcloud.server.common.data.id.AssetId;
import org.winstarcloud.server.common.data.id.AssetProfileId;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.DashboardId;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.DeviceProfileId;
import org.winstarcloud.server.common.data.id.EdgeId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.EntityIdFactory;
import org.winstarcloud.server.common.data.id.EntityViewId;
import org.winstarcloud.server.common.data.id.HasId;
import org.winstarcloud.server.common.data.id.OtaPackageId;
import org.winstarcloud.server.common.data.id.QueueId;
import org.winstarcloud.server.common.data.id.RpcId;
import org.winstarcloud.server.common.data.id.RuleChainId;
import org.winstarcloud.server.common.data.id.RuleNodeId;
import org.winstarcloud.server.common.data.id.TbResourceId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.TenantProfileId;
import org.winstarcloud.server.common.data.id.UUIDBased;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.id.WidgetTypeId;
import org.winstarcloud.server.common.data.id.WidgetsBundleId;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.data.page.SortOrder;
import org.winstarcloud.server.common.data.page.TimePageLink;
import org.winstarcloud.server.common.data.plugin.ComponentDescriptor;
import org.winstarcloud.server.common.data.plugin.ComponentType;
import org.winstarcloud.server.common.data.query.EntityDataSortOrder;
import org.winstarcloud.server.common.data.query.EntityKey;
import org.winstarcloud.server.common.data.queue.Queue;
import org.winstarcloud.server.common.data.rpc.Rpc;
import org.winstarcloud.server.common.data.rule.RuleChain;
import org.winstarcloud.server.common.data.rule.RuleChainType;
import org.winstarcloud.server.common.data.rule.RuleNode;
import org.winstarcloud.server.common.data.util.ThrowingBiFunction;
import org.winstarcloud.server.common.data.widget.WidgetTypeDetails;
import org.winstarcloud.server.common.data.widget.WidgetsBundle;
import org.winstarcloud.server.dao.alarm.AlarmCommentService;
import org.winstarcloud.server.dao.asset.AssetProfileService;
import org.winstarcloud.server.dao.asset.AssetService;
import org.winstarcloud.server.dao.attributes.AttributesService;
import org.winstarcloud.server.dao.audit.AuditLogService;
import org.winstarcloud.server.dao.customer.CustomerService;
import org.winstarcloud.server.dao.dashboard.DashboardService;
import org.winstarcloud.server.dao.device.ClaimDevicesService;
import org.winstarcloud.server.dao.device.DeviceCredentialsService;
import org.winstarcloud.server.dao.device.DeviceProfileService;
import org.winstarcloud.server.dao.device.DeviceService;
import org.winstarcloud.server.dao.edge.EdgeService;
import org.winstarcloud.server.dao.entityview.EntityViewService;
import org.winstarcloud.server.dao.exception.DataValidationException;
import org.winstarcloud.server.dao.exception.IncorrectParameterException;
import org.winstarcloud.server.dao.model.ModelConstants;
import org.winstarcloud.server.dao.oauth2.OAuth2ConfigTemplateService;
import org.winstarcloud.server.dao.oauth2.OAuth2Service;
import org.winstarcloud.server.dao.ota.OtaPackageService;
import org.winstarcloud.server.dao.queue.QueueService;
import org.winstarcloud.server.dao.relation.RelationService;
import org.winstarcloud.server.dao.resource.ResourceService;
import org.winstarcloud.server.dao.rpc.RpcService;
import org.winstarcloud.server.dao.rule.RuleChainService;
import org.winstarcloud.server.dao.service.ConstraintValidator;
import org.winstarcloud.server.dao.service.Validator;
import org.winstarcloud.server.dao.tenant.TbTenantProfileCache;
import org.winstarcloud.server.dao.tenant.TenantProfileService;
import org.winstarcloud.server.dao.tenant.TenantService;
import org.winstarcloud.server.dao.user.UserService;
import org.winstarcloud.server.dao.widget.WidgetTypeService;
import org.winstarcloud.server.dao.widget.WidgetsBundleService;
import org.winstarcloud.server.exception.WinstarcloudErrorResponseHandler;
import org.winstarcloud.server.queue.discovery.PartitionService;
import org.winstarcloud.server.queue.provider.TbQueueProducerProvider;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.action.EntityActionService;
import org.winstarcloud.server.service.component.ComponentDiscoveryService;
import org.winstarcloud.server.service.entitiy.TbLogEntityActionService;
import org.winstarcloud.server.service.entitiy.user.TbUserSettingsService;
import org.winstarcloud.server.service.ota.OtaPackageStateService;
import org.winstarcloud.server.service.profile.TbAssetProfileCache;
import org.winstarcloud.server.service.profile.TbDeviceProfileCache;
import org.winstarcloud.server.service.security.model.SecurityUser;
import org.winstarcloud.server.service.security.permission.AccessControlService;
import org.winstarcloud.server.service.security.permission.Operation;
import org.winstarcloud.server.service.security.permission.Resource;
import org.winstarcloud.server.service.state.DeviceStateService;
import org.winstarcloud.server.service.sync.ie.exporting.ExportableEntitiesService;
import org.winstarcloud.server.service.sync.vc.EntitiesVersionControlService;
import org.winstarcloud.server.service.telemetry.AlarmSubscriptionService;
import org.winstarcloud.server.service.telemetry.TelemetrySubscriptionService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.winstarcloud.server.common.data.StringUtils.isNotEmpty;
import static org.winstarcloud.server.common.data.query.EntityKeyType.ENTITY_FIELD;
import static org.winstarcloud.server.controller.UserController.YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION;
import static org.winstarcloud.server.dao.service.Validator.validateId;

@TbCoreComponent
public abstract class BaseController {

    private final Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    /*Swagger UI description*/

    @Autowired
    private WinstarcloudErrorResponseHandler errorResponseHandler;

    @Autowired
    protected AccessControlService accessControlService;

    @Autowired
    protected TenantService tenantService;

    @Autowired
    protected TenantProfileService tenantProfileService;

    @Autowired
    protected CustomerService customerService;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TbUserSettingsService userSettingsService;

    @Autowired
    protected DeviceService deviceService;

    @Autowired
    protected DeviceProfileService deviceProfileService;

    @Autowired
    protected AssetService assetService;

    @Autowired
    protected AssetProfileService assetProfileService;

    @Autowired
    protected AlarmSubscriptionService alarmService;

    @Autowired
    protected AlarmCommentService alarmCommentService;

    @Autowired
    protected DeviceCredentialsService deviceCredentialsService;

    @Autowired
    protected WidgetsBundleService widgetsBundleService;

    @Autowired
    protected WidgetTypeService widgetTypeService;

    @Autowired
    protected DashboardService dashboardService;

    @Autowired
    protected OAuth2Service oAuth2Service;

    @Autowired
    protected OAuth2ConfigTemplateService oAuth2ConfigTemplateService;

    @Autowired
    protected ComponentDiscoveryService componentDescriptorService;

    @Autowired
    protected RuleChainService ruleChainService;

    @Autowired
    protected TbClusterService tbClusterService;

    @Autowired
    protected RelationService relationService;

    @Autowired
    protected AuditLogService auditLogService;

    @Autowired
    protected DeviceStateService deviceStateService;

    @Autowired
    protected EntityViewService entityViewService;

    @Autowired
    protected TelemetrySubscriptionService tsSubService;

    @Autowired
    protected AttributesService attributesService;

    @Autowired
    protected ClaimDevicesService claimDevicesService;

    @Autowired
    protected PartitionService partitionService;

    @Autowired
    protected ResourceService resourceService;

    @Autowired
    protected OtaPackageService otaPackageService;

    @Autowired
    protected OtaPackageStateService otaPackageStateService;

    @Autowired
    protected RpcService rpcService;

    @Autowired
    protected TbQueueProducerProvider producerProvider;

    @Autowired
    protected TbTenantProfileCache tenantProfileCache;

    @Autowired
    protected TbDeviceProfileCache deviceProfileCache;

    @Autowired
    protected TbAssetProfileCache assetProfileCache;

    @Autowired(required = false)
    protected EdgeService edgeService;

    @Autowired
    protected TbLogEntityActionService logEntityActionService;

    @Autowired
    protected EntityActionService entityActionService;

    @Autowired
    protected QueueService queueService;

    @Autowired
    protected EntitiesVersionControlService vcService;

    @Autowired
    protected ExportableEntitiesService entitiesService;

    @Value("${server.log_controller_error_stack_trace}")
    @Getter
    private boolean logControllerErrorStackTrace;

    @Value("${edges.enabled}")
    @Getter
    protected boolean edgesEnabled;

    @ExceptionHandler(Exception.class)
    public void handleControllerException(Exception e, HttpServletResponse response) {
        WinstarcloudException winstarcloudException = handleException(e);
        if (winstarcloudException.getErrorCode() == WinstarcloudErrorCode.GENERAL && winstarcloudException.getCause() instanceof Exception
                && StringUtils.equals(winstarcloudException.getCause().getMessage(), winstarcloudException.getMessage())) {
            e = (Exception) winstarcloudException.getCause();
        } else {
            e = winstarcloudException;
        }
        errorResponseHandler.handle(e, response);
    }

    @ExceptionHandler(WinstarcloudException.class)
    public void handleWinstarcloudException(WinstarcloudException ex, HttpServletResponse response) {
        errorResponseHandler.handle(ex, response);
    }

    /**
     * @deprecated Exceptions that are not of {@link WinstarcloudException} type
     * are now caught and mapped to {@link WinstarcloudException} by
     * {@link ExceptionHandler} {@link BaseController#handleControllerException(Exception, HttpServletResponse)}
     * which basically acts like the following boilerplate:
     * {@code
     *  try {
     *      someExceptionThrowingMethod();
     *  } catch (Exception e) {
     *      throw handleException(e);
     *  }
     * }
     * */
    @Deprecated
    WinstarcloudException handleException(Exception exception) {
        return handleException(exception, true);
    }

    private WinstarcloudException handleException(Exception exception, boolean logException) {
        if (logException && logControllerErrorStackTrace) {
            try {
                SecurityUser user = getCurrentUser();
                log.error("[{}][{}] Error", user.getTenantId(), user.getId(), exception);
            } catch (Exception e) {
                log.error("Error", exception);
            }
        }

        Throwable cause = exception.getCause();
        if (exception instanceof WinstarcloudException) {
            return (WinstarcloudException) exception;
        } else if (exception instanceof IllegalArgumentException || exception instanceof IncorrectParameterException
                || exception instanceof DataValidationException || cause instanceof IncorrectParameterException) {
            return new WinstarcloudException(exception.getMessage(), WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        } else if (exception instanceof MessagingException) {
            return new WinstarcloudException("Unable to send mail: " + exception.getMessage(), WinstarcloudErrorCode.GENERAL);
        } else if (exception instanceof AsyncRequestTimeoutException) {
            return new WinstarcloudException("Request timeout", WinstarcloudErrorCode.GENERAL);
        } else if (exception instanceof DataAccessException) {
            if (!logControllerErrorStackTrace) { // not to log the error twice
                log.warn("Database error: {} - {}", exception.getClass().getSimpleName(), ExceptionUtils.getRootCauseMessage(exception));
            }
            if (cause instanceof ConstraintViolationException) {
                return new WinstarcloudException(ExceptionUtils.getRootCause(exception).getMessage(), WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
            } else {
                return new WinstarcloudException("Database error", WinstarcloudErrorCode.GENERAL);
            }
        }
        return new WinstarcloudException(exception.getMessage(), exception, WinstarcloudErrorCode.GENERAL);
    }

    /**
     * Handles validation error for controller method arguments annotated with @{@link jakarta.validation.Valid}
     * */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationError(MethodArgumentNotValidException validationError, HttpServletResponse response) {
        List<ConstraintViolation<Object>> constraintsViolations = validationError.getFieldErrors().stream()
                .map(fieldError -> {
                    try {
                        return (ConstraintViolation<Object>) fieldError.unwrap(ConstraintViolation.class);
                    } catch (Exception e) {
                        log.warn("FieldError source is not of type ConstraintViolation");
                        return null; // should not happen
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        String errorMessage = "Validation error: " + ConstraintValidator.getErrorMessage(constraintsViolations);
        WinstarcloudException winstarcloudException = new WinstarcloudException(errorMessage, WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        handleControllerException(winstarcloudException, response);
    }

    <T> T checkNotNull(T reference) throws WinstarcloudException {
        return checkNotNull(reference, "Requested item wasn't found!");
    }

    <T> T checkNotNull(T reference, String notFoundMessage) throws WinstarcloudException {
        if (reference == null) {
            throw new WinstarcloudException(notFoundMessage, WinstarcloudErrorCode.ITEM_NOT_FOUND);
        }
        return reference;
    }

    <T> T checkNotNull(Optional<T> reference) throws WinstarcloudException {
        return checkNotNull(reference, "Requested item wasn't found!");
    }

    <T> T checkNotNull(Optional<T> reference, String notFoundMessage) throws WinstarcloudException {
        if (reference.isPresent()) {
            return reference.get();
        } else {
            throw new WinstarcloudException(notFoundMessage, WinstarcloudErrorCode.ITEM_NOT_FOUND);
        }
    }

    void checkParameter(String name, String param) throws WinstarcloudException {
        if (StringUtils.isEmpty(param)) {
            throw new WinstarcloudException("Parameter '" + name + "' can't be empty!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    void checkArrayParameter(String name, String[] params) throws WinstarcloudException {
        if (params == null || params.length == 0) {
            throw new WinstarcloudException("Parameter '" + name + "' can't be empty!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        } else {
            for (String param : params) {
                checkParameter(name, param);
            }
        }
    }

    protected <T> T checkEnumParameter(String name, String param, Function<String, T> valueOf) throws WinstarcloudException {
        try {
            return valueOf.apply(param.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new WinstarcloudException(name + " \"" + param + "\" is not supported!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    UUID toUUID(String id) throws WinstarcloudException {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw handleException(e, false);
        }
    }

    PageLink createPageLink(int pageSize, int page, String textSearch, String sortProperty, String sortOrder) throws WinstarcloudException {
        if (StringUtils.isNotEmpty(sortProperty)) {
            if (!Validator.isValidProperty(sortProperty)) {
                throw new IllegalArgumentException("Invalid sort property");
            }
            SortOrder.Direction direction = SortOrder.Direction.ASC;
            if (StringUtils.isNotEmpty(sortOrder)) {
                try {
                    direction = SortOrder.Direction.valueOf(sortOrder.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new WinstarcloudException("Unsupported sort order '" + sortOrder + "'! Only 'ASC' or 'DESC' types are allowed.", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
                }
            }
            SortOrder sort = new SortOrder(sortProperty, direction);
            return new PageLink(pageSize, page, textSearch, sort);
        } else {
            return new PageLink(pageSize, page, textSearch);
        }
    }

    TimePageLink createTimePageLink(int pageSize, int page, String textSearch,
                                    String sortProperty, String sortOrder, Long startTime, Long endTime) throws WinstarcloudException {
        PageLink pageLink = this.createPageLink(pageSize, page, textSearch, sortProperty, sortOrder);
        return new TimePageLink(pageLink, startTime, endTime);
    }

    protected SecurityUser getCurrentUser() throws WinstarcloudException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            return (SecurityUser) authentication.getPrincipal();
        } else {
            throw new WinstarcloudException("You aren't authorized to perform this operation!", WinstarcloudErrorCode.AUTHENTICATION);
        }
    }

    Tenant checkTenantId(TenantId tenantId, Operation operation) throws WinstarcloudException {
        return checkEntityId(tenantId, (t, i) -> tenantService.findTenantById(tenantId), operation);
    }

    TenantInfo checkTenantInfoId(TenantId tenantId, Operation operation) throws WinstarcloudException {
        return checkEntityId(tenantId, (t, i) -> tenantService.findTenantInfoById(tenantId), operation);
    }

    TenantProfile checkTenantProfileId(TenantProfileId tenantProfileId, Operation operation) throws WinstarcloudException {
        try {
            validateId(tenantProfileId, id -> "Incorrect tenantProfileId " + id);
            TenantProfile tenantProfile = tenantProfileService.findTenantProfileById(getTenantId(), tenantProfileId);
            checkNotNull(tenantProfile, "Tenant profile with id [" + tenantProfileId + "] is not found");
            accessControlService.checkPermission(getCurrentUser(), Resource.TENANT_PROFILE, operation);
            return tenantProfile;
        } catch (Exception e) {
            throw handleException(e, false);
        }
    }

    protected TenantId getTenantId() throws WinstarcloudException {
        return getCurrentUser().getTenantId();
    }

    Customer checkCustomerId(CustomerId customerId, Operation operation) throws WinstarcloudException {
        return checkEntityId(customerId, customerService::findCustomerById, operation);
    }

    User checkUserId(UserId userId, Operation operation) throws WinstarcloudException {
        return checkEntityId(userId, userService::findUserById, operation);
    }

    protected <I extends EntityId, T extends HasTenantId> void checkEntity(I entityId, T entity, Resource resource) throws WinstarcloudException {
        if (entityId == null) {
            accessControlService.checkPermission(getCurrentUser(), resource, Operation.CREATE, null, entity);
        } else {
            checkEntityId(entityId, Operation.WRITE);
        }
    }

    protected void checkEntityId(EntityId entityId, Operation operation) throws WinstarcloudException {
        try {
            if (entityId == null) {
                throw new WinstarcloudException("Parameter entityId can't be empty!", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
            }
            validateId(entityId.getId(), id -> "Incorrect entityId " + id);
            switch (entityId.getEntityType()) {
                case ALARM:
                    checkAlarmId(new AlarmId(entityId.getId()), operation);
                    return;
                case DEVICE:
                    checkDeviceId(new DeviceId(entityId.getId()), operation);
                    return;
                case DEVICE_PROFILE:
                    checkDeviceProfileId(new DeviceProfileId(entityId.getId()), operation);
                    return;
                case CUSTOMER:
                    checkCustomerId(new CustomerId(entityId.getId()), operation);
                    return;
                case TENANT:
                    checkTenantId(TenantId.fromUUID(entityId.getId()), operation);
                    return;
                case TENANT_PROFILE:
                    checkTenantProfileId(new TenantProfileId(entityId.getId()), operation);
                    return;
                case RULE_CHAIN:
                    checkRuleChain(new RuleChainId(entityId.getId()), operation);
                    return;
                case RULE_NODE:
                    checkRuleNode(new RuleNodeId(entityId.getId()), operation);
                    return;
                case ASSET:
                    checkAssetId(new AssetId(entityId.getId()), operation);
                    return;
                case ASSET_PROFILE:
                    checkAssetProfileId(new AssetProfileId(entityId.getId()), operation);
                    return;
                case DASHBOARD:
                    checkDashboardId(new DashboardId(entityId.getId()), operation);
                    return;
                case USER:
                    checkUserId(new UserId(entityId.getId()), operation);
                    return;
                case ENTITY_VIEW:
                    checkEntityViewId(new EntityViewId(entityId.getId()), operation);
                    return;
                case EDGE:
                    checkEdgeId(new EdgeId(entityId.getId()), operation);
                    return;
                case WIDGETS_BUNDLE:
                    checkWidgetsBundleId(new WidgetsBundleId(entityId.getId()), operation);
                    return;
                case WIDGET_TYPE:
                    checkWidgetTypeId(new WidgetTypeId(entityId.getId()), operation);
                    return;
                case TB_RESOURCE:
                    checkResourceInfoId(new TbResourceId(entityId.getId()), operation);
                    return;
                case OTA_PACKAGE:
                    checkOtaPackageId(new OtaPackageId(entityId.getId()), operation);
                    return;
                case QUEUE:
                    checkQueueId(new QueueId(entityId.getId()), operation);
                    return;
                default:
                    checkEntityId(entityId, entitiesService::findEntityByTenantIdAndId, operation);
            }
        } catch (Exception e) {
            throw handleException(e, false);
        }
    }

    protected <E extends HasId<I> & HasTenantId, I extends EntityId> E checkEntityId(I entityId, ThrowingBiFunction<TenantId, I, E> findingFunction, Operation operation) throws WinstarcloudException {
        try {
            validateId((UUIDBased) entityId, "Invalid entity id");
            SecurityUser user = getCurrentUser();
            E entity = findingFunction.apply(user.getTenantId(), entityId);
            checkNotNull(entity, entityId.getEntityType().getNormalName() + " with id [" + entityId + "] is not found");
            return checkEntity(user, entity, operation);
        } catch (Exception e) {
            throw handleException(e, false);
        }
    }

    protected <E extends HasId<I> & HasTenantId, I extends EntityId> E checkEntity(SecurityUser user, E entity, Operation operation) throws WinstarcloudException {
        checkNotNull(entity, "Entity not found");
        accessControlService.checkPermission(user, Resource.of(entity.getId().getEntityType()), operation, entity.getId(), entity);
        return entity;
    }

    Device checkDeviceId(DeviceId deviceId, Operation operation) throws WinstarcloudException {
        return checkEntityId(deviceId, deviceService::findDeviceById, operation);
    }

    DeviceInfo checkDeviceInfoId(DeviceId deviceId, Operation operation) throws WinstarcloudException {
        return checkEntityId(deviceId, deviceService::findDeviceInfoById, operation);
    }

    DeviceProfile checkDeviceProfileId(DeviceProfileId deviceProfileId, Operation operation) throws WinstarcloudException {
        return checkEntityId(deviceProfileId, deviceProfileService::findDeviceProfileById, operation);
    }

    protected EntityView checkEntityViewId(EntityViewId entityViewId, Operation operation) throws WinstarcloudException {
        return checkEntityId(entityViewId, entityViewService::findEntityViewById, operation);
    }

    EntityViewInfo checkEntityViewInfoId(EntityViewId entityViewId, Operation operation) throws WinstarcloudException {
        return checkEntityId(entityViewId, entityViewService::findEntityViewInfoById, operation);
    }

    Asset checkAssetId(AssetId assetId, Operation operation) throws WinstarcloudException {
        return checkEntityId(assetId, assetService::findAssetById, operation);
    }

    AssetInfo checkAssetInfoId(AssetId assetId, Operation operation) throws WinstarcloudException {
        return checkEntityId(assetId, assetService::findAssetInfoById, operation);
    }

    AssetProfile checkAssetProfileId(AssetProfileId assetProfileId, Operation operation) throws WinstarcloudException {
        return checkEntityId(assetProfileId, assetProfileService::findAssetProfileById, operation);
    }

    Alarm checkAlarmId(AlarmId alarmId, Operation operation) throws WinstarcloudException {
        return checkEntityId(alarmId, alarmService::findAlarmById, operation);
    }

    AlarmInfo checkAlarmInfoId(AlarmId alarmId, Operation operation) throws WinstarcloudException {
        return checkEntityId(alarmId, alarmService::findAlarmInfoById, operation);
    }

    AlarmComment checkAlarmCommentId(AlarmCommentId alarmCommentId, AlarmId alarmId) throws WinstarcloudException {
        try {
            validateId(alarmCommentId, id -> "Incorrect alarmCommentId " + id);
            AlarmComment alarmComment = alarmCommentService.findAlarmCommentByIdAsync(getCurrentUser().getTenantId(), alarmCommentId).get();
            checkNotNull(alarmComment, "Alarm comment with id [" + alarmCommentId + "] is not found");
            if (!alarmId.equals(alarmComment.getAlarmId())) {
                throw new WinstarcloudException("Alarm id does not match with comment alarm id", WinstarcloudErrorCode.BAD_REQUEST_PARAMS);
            }
            return alarmComment;
        } catch (Exception e) {
            throw handleException(e, false);
        }
    }

    WidgetsBundle checkWidgetsBundleId(WidgetsBundleId widgetsBundleId, Operation operation) throws WinstarcloudException {
        return checkEntityId(widgetsBundleId, widgetsBundleService::findWidgetsBundleById, operation);
    }

    WidgetTypeDetails checkWidgetTypeId(WidgetTypeId widgetTypeId, Operation operation) throws WinstarcloudException {
        return checkEntityId(widgetTypeId, widgetTypeService::findWidgetTypeDetailsById, operation);
    }

    Dashboard checkDashboardId(DashboardId dashboardId, Operation operation) throws WinstarcloudException {
        return checkEntityId(dashboardId, dashboardService::findDashboardById, operation);
    }

    Edge checkEdgeId(EdgeId edgeId, Operation operation) throws WinstarcloudException {
        return checkEntityId(edgeId, edgeService::findEdgeById, operation);
    }

    EdgeInfo checkEdgeInfoId(EdgeId edgeId, Operation operation) throws WinstarcloudException {
        return checkEntityId(edgeId, edgeService::findEdgeInfoById, operation);
    }

    DashboardInfo checkDashboardInfoId(DashboardId dashboardId, Operation operation) throws WinstarcloudException {
        return checkEntityId(dashboardId, dashboardService::findDashboardInfoById, operation);
    }

    ComponentDescriptor checkComponentDescriptorByClazz(String clazz) throws WinstarcloudException {
        try {
            log.debug("[{}] Lookup component descriptor", clazz);
            return checkNotNull(componentDescriptorService.getComponent(clazz));
        } catch (Exception e) {
            throw handleException(e, false);
        }
    }

    List<ComponentDescriptor> checkComponentDescriptorsByType(ComponentType type, RuleChainType ruleChainType) throws WinstarcloudException {
        try {
            log.debug("[{}] Lookup component descriptors", type);
            return componentDescriptorService.getComponents(type, ruleChainType);
        } catch (Exception e) {
            throw handleException(e, false);
        }
    }

    List<ComponentDescriptor> checkComponentDescriptorsByTypes(Set<ComponentType> types, RuleChainType ruleChainType) throws WinstarcloudException {
        try {
            log.debug("[{}] Lookup component descriptors", types);
            return componentDescriptorService.getComponents(types, ruleChainType);
        } catch (Exception e) {
            throw handleException(e, false);
        }
    }

    protected RuleChain checkRuleChain(RuleChainId ruleChainId, Operation operation) throws WinstarcloudException {
        return checkEntityId(ruleChainId, ruleChainService::findRuleChainById, operation);
    }

    protected RuleNode checkRuleNode(RuleNodeId ruleNodeId, Operation operation) throws WinstarcloudException {
        validateId(ruleNodeId, id -> "Incorrect ruleNodeId " + id);
        RuleNode ruleNode = ruleChainService.findRuleNodeById(getTenantId(), ruleNodeId);
        checkNotNull(ruleNode, "Rule node with id [" + ruleNodeId + "] is not found");
        checkRuleChain(ruleNode.getRuleChainId(), operation);
        return ruleNode;
    }

    TbResource checkResourceId(TbResourceId resourceId, Operation operation) throws WinstarcloudException {
        return checkEntityId(resourceId, resourceService::findResourceById, operation);
    }

    TbResourceInfo checkResourceInfoId(TbResourceId resourceId, Operation operation) throws WinstarcloudException {
        return checkEntityId(resourceId, resourceService::findResourceInfoById, operation);
    }

    OtaPackage checkOtaPackageId(OtaPackageId otaPackageId, Operation operation) throws WinstarcloudException {
        return checkEntityId(otaPackageId, otaPackageService::findOtaPackageById, operation);
    }

    OtaPackageInfo checkOtaPackageInfoId(OtaPackageId otaPackageId, Operation operation) throws WinstarcloudException {
        return checkEntityId(otaPackageId, otaPackageService::findOtaPackageInfoById, operation);
    }

    Rpc checkRpcId(RpcId rpcId, Operation operation) throws WinstarcloudException {
        return checkEntityId(rpcId, rpcService::findById, operation);
    }

    protected Queue checkQueueId(QueueId queueId, Operation operation) throws WinstarcloudException {
        Queue queue = checkEntityId(queueId, queueService::findQueueById, operation);
        TenantId tenantId = getTenantId();
        if (queue.getTenantId().isNullUid() && !tenantId.isNullUid()) {
            TenantProfile tenantProfile = tenantProfileCache.get(tenantId);
            if (tenantProfile.isIsolatedTbRuleEngine()) {
                throw new WinstarcloudException(YOU_DON_T_HAVE_PERMISSION_TO_PERFORM_THIS_OPERATION,
                        WinstarcloudErrorCode.PERMISSION_DENIED);
            }
        }
        return queue;
    }

    protected <I extends EntityId> I emptyId(EntityType entityType) {
        return (I) EntityIdFactory.getByTypeAndUuid(entityType, ModelConstants.NULL_UUID);
    }

    public static Exception toException(Throwable error) {
        return error != null ? (Exception.class.isInstance(error) ? (Exception) error : new Exception(error)) : null;
    }

    protected <E extends HasName & HasId<? extends EntityId>> void logEntityAction(SecurityUser user, EntityType entityType, E savedEntity, ActionType actionType) {
        logEntityAction(user, entityType, null, savedEntity, actionType, null);
    }

    protected <E extends HasName & HasId<? extends EntityId>> void logEntityAction(SecurityUser user, EntityType entityType, E entity, E savedEntity, ActionType actionType, Exception e) {
        EntityId entityId = savedEntity != null ? savedEntity.getId() : emptyId(entityType);
        if (!user.isSystemAdmin()) {
            entityActionService.logEntityAction(user, entityId, savedEntity != null ? savedEntity : entity,
                    user.getCustomerId(), actionType, e);
        }
    }

    protected <E extends HasName & HasId<? extends EntityId>> E doSaveAndLog(EntityType entityType, E entity, BiFunction<TenantId, E, E> savingFunction) throws Exception {
        ActionType actionType = entity.getId() == null ? ActionType.ADDED : ActionType.UPDATED;
        SecurityUser user = getCurrentUser();
        try {
            E savedEntity = savingFunction.apply(user.getTenantId(), entity);
            logEntityAction(user, entityType, savedEntity, actionType);
            return savedEntity;
        } catch (Exception e) {
            logEntityAction(user, entityType, entity, null, actionType, e);
            throw e;
        }
    }

    protected <E extends HasName & HasId<I>, I extends EntityId> void doDeleteAndLog(EntityType entityType, E entity, BiConsumer<TenantId, I> deleteFunction) throws Exception {
        SecurityUser user = getCurrentUser();
        try {
            deleteFunction.accept(user.getTenantId(), entity.getId());
            logEntityAction(user, entityType, entity, ActionType.DELETED);
        } catch (Exception e) {
            logEntityAction(user, entityType, entity, entity, ActionType.DELETED, e);
            throw e;
        }
    }

    protected void processDashboardIdFromAdditionalInfo(ObjectNode additionalInfo, String requiredFields) throws WinstarcloudException {
        String dashboardId = additionalInfo.has(requiredFields) ? additionalInfo.get(requiredFields).asText() : null;
        if (dashboardId != null && !dashboardId.equals("null")) {
            if (dashboardService.findDashboardById(getTenantId(), new DashboardId(UUID.fromString(dashboardId))) == null) {
                additionalInfo.remove(requiredFields);
            }
        }
    }

    protected MediaType parseMediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    protected <T> DeferredResult<T> wrapFuture(ListenableFuture<T> future) {
        DeferredResult<T> deferredResult = new DeferredResult<>(); // Timeout of spring.mvc.async.request-timeout is used
        DonAsynchron.withCallback(future, deferredResult::setResult, deferredResult::setErrorResult);
        return deferredResult;
    }

    protected <T> DeferredResult<T> wrapFuture(ListenableFuture<T> future, long timeoutMs) {
        DeferredResult<T> deferredResult = new DeferredResult<>(timeoutMs);
        DonAsynchron.withCallback(future, deferredResult::setResult, deferredResult::setErrorResult);
        return deferredResult;
    }

    protected EntityDataSortOrder createEntityDataSortOrder(String sortProperty, String sortOrder) {
        if (isNotEmpty(sortProperty)) {
            EntityDataSortOrder entityDataSortOrder = new EntityDataSortOrder();
            entityDataSortOrder.setKey(new EntityKey(ENTITY_FIELD, sortProperty));
            if (isNotEmpty(sortOrder)) {
                entityDataSortOrder.setDirection(EntityDataSortOrder.Direction.valueOf(sortOrder));
            }
            return entityDataSortOrder;
        } else {
            return null;
        }
    }

}
