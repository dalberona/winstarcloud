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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.rule.engine.api.MailService;
import org.winstarcloud.server.actors.DefaultTbActorSystem;
import org.winstarcloud.server.actors.TbActorId;
import org.winstarcloud.server.actors.TbActorMailbox;
import org.winstarcloud.server.actors.TbEntityActorId;
import org.winstarcloud.server.actors.device.DeviceActor;
import org.winstarcloud.server.actors.device.DeviceActorMessageProcessor;
import org.winstarcloud.server.actors.device.SessionInfo;
import org.winstarcloud.server.actors.device.ToDeviceRpcRequestMetadata;
import org.winstarcloud.server.actors.service.DefaultActorService;
import org.winstarcloud.server.common.data.Customer;
import org.winstarcloud.server.common.data.Device;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.DeviceProfileType;
import org.winstarcloud.server.common.data.DeviceTransportType;
import org.winstarcloud.server.common.data.SaveDeviceWithCredentialsRequest;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.Tenant;
import org.winstarcloud.server.common.data.TenantProfile;
import org.winstarcloud.server.common.data.User;
import org.winstarcloud.server.common.data.asset.AssetProfile;
import org.winstarcloud.server.common.data.device.data.DefaultDeviceConfiguration;
import org.winstarcloud.server.common.data.device.data.DefaultDeviceTransportConfiguration;
import org.winstarcloud.server.common.data.device.data.DeviceData;
import org.winstarcloud.server.common.data.device.profile.DefaultDeviceProfileConfiguration;
import org.winstarcloud.server.common.data.device.profile.DefaultDeviceProfileTransportConfiguration;
import org.winstarcloud.server.common.data.device.profile.DeviceProfileData;
import org.winstarcloud.server.common.data.device.profile.DeviceProfileTransportConfiguration;
import org.winstarcloud.server.common.data.device.profile.MqttDeviceProfileTransportConfiguration;
import org.winstarcloud.server.common.data.device.profile.MqttTopics;
import org.winstarcloud.server.common.data.device.profile.ProtoTransportPayloadConfiguration;
import org.winstarcloud.server.common.data.device.profile.TransportPayloadTypeConfiguration;
import org.winstarcloud.server.common.data.edge.Edge;
import org.winstarcloud.server.common.data.exception.WinstarcloudException;
import org.winstarcloud.server.common.data.id.CustomerId;
import org.winstarcloud.server.common.data.id.DeviceId;
import org.winstarcloud.server.common.data.id.EntityId;
import org.winstarcloud.server.common.data.id.HasId;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.id.TenantProfileId;
import org.winstarcloud.server.common.data.id.UUIDBased;
import org.winstarcloud.server.common.data.id.UserId;
import org.winstarcloud.server.common.data.page.PageData;
import org.winstarcloud.server.common.data.page.PageLink;
import org.winstarcloud.server.common.data.page.TimePageLink;
import org.winstarcloud.server.common.data.relation.EntityRelation;
import org.winstarcloud.server.common.data.security.Authority;
import org.winstarcloud.server.common.data.security.DeviceCredentials;
import org.winstarcloud.server.common.data.security.DeviceCredentialsType;
import org.winstarcloud.server.common.data.tenant.profile.DefaultTenantProfileConfiguration;
import org.winstarcloud.server.common.data.tenant.profile.TenantProfileData;
import org.winstarcloud.server.common.msg.session.FeatureType;
import org.winstarcloud.server.config.WinstarcloudSecurityConfiguration;
import org.winstarcloud.server.dao.Dao;
import org.winstarcloud.server.dao.attributes.AttributesService;
import org.winstarcloud.server.dao.device.ClaimDevicesService;
import org.winstarcloud.server.dao.tenant.TenantProfileService;
import org.winstarcloud.server.dao.timeseries.TimeseriesService;
import org.winstarcloud.server.queue.memory.InMemoryStorage;
import org.winstarcloud.server.service.entitiy.tenant.profile.TbTenantProfileService;
import org.winstarcloud.server.service.security.auth.jwt.RefreshTokenRequest;
import org.winstarcloud.server.service.security.auth.rest.LoginRequest;
import org.winstarcloud.server.service.security.model.token.JwtTokenFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.winstarcloud.server.common.data.CacheConstants.CLAIM_DEVICES_CACHE;

@Slf4j
public abstract class AbstractWebTest extends AbstractInMemoryStorageTest {

    public static final int TIMEOUT = 30;

    protected static final String TEST_TENANT_NAME = "TEST TENANT";
    protected static final String TEST_DIFFERENT_TENANT_NAME = "TEST DIFFERENT TENANT";

    protected static final String SYS_ADMIN_EMAIL = "sysadmin@winstarcloud.org";
    private static final String SYS_ADMIN_PASSWORD = "sysadmin";

    protected static final String TENANT_ADMIN_EMAIL = "testtenant@winstarcloud.org";
    protected static final String TENANT_ADMIN_PASSWORD = "tenant";

    protected static final String DIFFERENT_TENANT_ADMIN_EMAIL = "testdifftenant@winstarcloud.org";
    private static final String DIFFERENT_TENANT_ADMIN_PASSWORD = "difftenant";

    protected static final String CUSTOMER_USER_EMAIL = "testcustomer@winstarcloud.org";
    private static final String CUSTOMER_USER_PASSWORD = "customer";

    protected static final String DIFFERENT_CUSTOMER_USER_EMAIL = "testdifferentcustomer@winstarcloud.org";

    protected static final String DIFFERENT_TENANT_CUSTOMER_USER_EMAIL = "testdifferenttenantcustomer@winstarcloud.org";
    private static final String DIFFERENT_CUSTOMER_USER_PASSWORD = "diffcustomer";

    /**
     * See {@link org.springframework.test.web.servlet.DefaultMvcResult#getAsyncResult(long)}
     * and {@link org.springframework.mock.web.MockAsyncContext#getTimeout()}
     */
    private static final long DEFAULT_TIMEOUT = -1L;
    private static final int CLEANUP_TENANT_RETRIES_COUNT = 3;

    protected MediaType contentType = MediaType.APPLICATION_JSON;

    protected MockMvc mockMvc;

    protected String currentActivateToken;
    protected String currentResetPasswordToken;

    protected String token;
    protected String refreshToken;
    protected String mobileToken;
    protected String username;

    protected TenantId tenantId;
    protected TenantProfileId tenantProfileId;
    protected UserId tenantAdminUserId;
    protected User tenantAdminUser;
    protected CustomerId tenantAdminCustomerId;
    protected CustomerId customerId;
    protected TenantId differentTenantId;
    protected CustomerId differentCustomerId;

    protected CustomerId differentTenantCustomerId;
    protected UserId customerUserId;
    protected UserId differentCustomerUserId;

    protected UserId differentTenantCustomerUserId;

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @SuppressWarnings("rawtypes")
    private HttpMessageConverter stringHttpMessageConverter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TenantProfileService tenantProfileService;

    @Autowired
    private TbTenantProfileService tbTenantProfileService;

    @Autowired
    protected TimeseriesService tsService;

    @Autowired
    protected AttributesService attributesService;

    @Autowired
    protected DefaultActorService actorService;

    @Autowired
    protected ClaimDevicesService claimDevicesService;

    @Autowired
    private JwtTokenFactory jwtTokenFactory;

    @SpyBean
    protected MailService mailService;

    @Autowired
    protected InMemoryStorage storage;

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            log.info("Starting test: {}", description.getMethodName());
        }

        protected void finished(Description description) {
            log.info("Finished test: {}", description.getMethodName());
        }
    };

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .get();

        this.stringHttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof StringHttpMessageConverter)
                .findAny()
                .get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setupWebTest() throws Exception {
        log.debug("Executing web test setup");

        setupMailServiceMock();

        if (this.mockMvc == null) {
            this.mockMvc = webAppContextSetup(webApplicationContext)
                    .apply(springSecurity()).build();
        }
        loginSysAdmin();

        Tenant tenant = new Tenant();
        tenant.setTitle(TEST_TENANT_NAME);
        Tenant savedTenant = saveTenant(tenant);
        Assert.assertNotNull(savedTenant);
        tenantId = savedTenant.getId();
        tenantProfileId = savedTenant.getTenantProfileId();

        tenantAdminUser = new User();
        tenantAdminUser.setAuthority(Authority.TENANT_ADMIN);
        tenantAdminUser.setTenantId(tenantId);
        tenantAdminUser.setEmail(TENANT_ADMIN_EMAIL);

        tenantAdminUser = createUserAndLogin(tenantAdminUser, TENANT_ADMIN_PASSWORD);
        tenantAdminUserId = tenantAdminUser.getId();
        tenantAdminCustomerId = tenantAdminUser.getCustomerId();

        Customer customer = new Customer();
        customer.setTitle("Customer");
        customer.setTenantId(tenantId);
        Customer savedCustomer = doPost("/api/customer", customer, Customer.class);
        customerId = savedCustomer.getId();

        User customerUser = new User();
        customerUser.setAuthority(Authority.CUSTOMER_USER);
        customerUser.setTenantId(tenantId);
        customerUser.setCustomerId(savedCustomer.getId());
        customerUser.setEmail(CUSTOMER_USER_EMAIL);

        customerUser = createUserAndLogin(customerUser, CUSTOMER_USER_PASSWORD);
        customerUserId = customerUser.getId();

        resetTokens();

        log.debug("Executed web test setup");
    }

    private void setupMailServiceMock() throws WinstarcloudException {
        Mockito.doNothing().when(mailService).sendAccountActivatedEmail(anyString(), anyString());
        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String activationLink = (String) args[0];
                currentActivateToken = activationLink.split("=")[1];
                return null;
            }
        }).when(mailService).sendActivationEmail(anyString(), anyString());

        Mockito.doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String passwordResetLink = (String) args[0];
                currentResetPasswordToken = passwordResetLink.split("=")[1];
                return null;
            }
        }).when(mailService).sendResetPasswordEmailAsync(anyString(), anyString());
    }

    @After
    public void teardownWebTest() throws Exception {
        log.debug("Executing web test teardown");

        loginSysAdmin();
        deleteTenant(tenantId);
        deleteDifferentTenant();
        verifyNoTenantsLeft();

        tenantProfileService.deleteTenantProfiles(TenantId.SYS_TENANT_ID);

        log.info("Executed web test teardown");
    }

    private void verifyNoTenantsLeft() throws Exception {
        List<Tenant> loadedTenants = getAllTenants();
        if (!loadedTenants.isEmpty()) {
            loadedTenants.forEach(tenant -> deleteTenant(tenant.getId()));
            loadedTenants = getAllTenants();
        }
        assertThat(loadedTenants).as("All tenants expected to be deleted, but some tenants left in the database").isEmpty();
    }

    protected void deleteTenant(TenantId tenantId) {
        try {
            doDelete("/api/tenant/" + tenantId.getId()).andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Awaitility.await("all tasks processed").atMost(60, TimeUnit.SECONDS).during(300, TimeUnit.MILLISECONDS)
                .until(() -> storage.getLag("tb_housekeeper") == 0);
    }

    private List<Tenant> getAllTenants() throws Exception {
        List<Tenant> loadedTenants = new ArrayList<>();
        PageLink pageLink = new PageLink(10);
        PageData<Tenant> pageData;
        do {
            pageData = doGetTypedWithPageLink("/api/tenants?", new TypeReference<PageData<Tenant>>() {
            }, pageLink);
            loadedTenants.addAll(pageData.getData());
            if (pageData.hasNext()) {
                pageLink = pageLink.nextPageLink();
            }
        } while (pageData.hasNext());
        return loadedTenants;
    }

    protected void loginSysAdmin() throws Exception {
        login(SYS_ADMIN_EMAIL, SYS_ADMIN_PASSWORD);
    }

    protected void loginTenantAdmin() throws Exception {
        login(TENANT_ADMIN_EMAIL, TENANT_ADMIN_PASSWORD);
    }

    protected void loginCustomerUser() throws Exception {
        login(CUSTOMER_USER_EMAIL, CUSTOMER_USER_PASSWORD);
    }

    protected void loginUser(String userName, String password) throws Exception {
        login(userName, password);
    }

    protected Tenant savedDifferentTenant;
    protected User savedDifferentTenantUser;
    private Customer savedDifferentCustomer;
    private Customer savedDifferentTenantCustomer;
    protected User differentCustomerUser;
    protected User differentTenantCustomerUser;

    protected void loginDifferentTenant() throws Exception {
        if (savedDifferentTenant != null) {
            login(DIFFERENT_TENANT_ADMIN_EMAIL, DIFFERENT_TENANT_ADMIN_PASSWORD);
        } else {
            createDifferentTenant();
        }
    }

    protected void createDifferentTenant() throws Exception {
        loginSysAdmin();
        Tenant tenant = new Tenant();
        tenant.setTitle(TEST_DIFFERENT_TENANT_NAME);
        savedDifferentTenant = saveTenant(tenant);
        differentTenantId = savedDifferentTenant.getId();
        Assert.assertNotNull(savedDifferentTenant);
        User differentTenantAdmin = new User();
        differentTenantAdmin.setAuthority(Authority.TENANT_ADMIN);
        differentTenantAdmin.setTenantId(savedDifferentTenant.getId());
        differentTenantAdmin.setEmail(DIFFERENT_TENANT_ADMIN_EMAIL);
        savedDifferentTenantUser = createUserAndLogin(differentTenantAdmin, DIFFERENT_TENANT_ADMIN_PASSWORD);
    }

    protected Tenant saveTenant(Tenant tenant) throws Exception {
        return doPost("/api/tenant", tenant, Tenant.class);
    }

    protected void loginDifferentCustomer() throws Exception {
        if (savedDifferentCustomer != null) {
            login(savedDifferentCustomer.getEmail(), CUSTOMER_USER_PASSWORD);
        } else {
            createDifferentCustomer();

            loginTenantAdmin();
            differentCustomerUser = new User();
            differentCustomerUser.setAuthority(Authority.CUSTOMER_USER);
            differentCustomerUser.setTenantId(tenantId);
            differentCustomerUser.setCustomerId(savedDifferentCustomer.getId());
            differentCustomerUser.setEmail(DIFFERENT_CUSTOMER_USER_EMAIL);

            differentCustomerUser = createUserAndLogin(differentCustomerUser, DIFFERENT_CUSTOMER_USER_PASSWORD);
            differentCustomerUserId = differentCustomerUser.getId();
        }
    }

    protected void loginDifferentTenantCustomer() throws Exception {
        if (savedDifferentTenantCustomer != null) {
            login(savedDifferentTenantCustomer.getEmail(), CUSTOMER_USER_PASSWORD);
        } else {
            createDifferentTenantCustomer();

            loginDifferentTenant();
            differentTenantCustomerUser = new User();
            differentTenantCustomerUser.setAuthority(Authority.CUSTOMER_USER);
            differentTenantCustomerUser.setTenantId(savedDifferentTenantCustomer.getTenantId());
            differentTenantCustomerUser.setCustomerId(savedDifferentTenantCustomer.getId());
            differentTenantCustomerUser.setEmail(DIFFERENT_TENANT_CUSTOMER_USER_EMAIL);

            differentTenantCustomerUser = createUserAndLogin(differentTenantCustomerUser, DIFFERENT_CUSTOMER_USER_PASSWORD);
            differentTenantCustomerUserId = differentTenantCustomerUser.getId();
        }
    }

    protected void createDifferentCustomer() throws Exception {
        loginTenantAdmin();

        Customer customer = new Customer();
        customer.setTitle("Different customer");
        savedDifferentCustomer = doPost("/api/customer", customer, Customer.class);
        Assert.assertNotNull(savedDifferentCustomer);
        differentCustomerId = savedDifferentCustomer.getId();

        resetTokens();
    }

    protected void createDifferentTenantCustomer() throws Exception {
        loginDifferentTenant();

        Customer customer = new Customer();
        customer.setTitle("Different tenant customer");
        savedDifferentTenantCustomer = doPost("/api/customer", customer, Customer.class);
        Assert.assertNotNull(savedDifferentTenantCustomer);
        differentTenantCustomerId = savedDifferentTenantCustomer.getId();

        resetTokens();
    }

    protected void deleteDifferentTenant() throws Exception {
        if (savedDifferentTenant != null) {
            loginSysAdmin();
            deleteTenant(savedDifferentTenant.getId());
            savedDifferentTenant = null;
        }
    }

    protected User createUserAndLogin(User user, String password) throws Exception {
        User savedUser = doPost("/api/user", user, User.class);
        resetTokens();
        JsonNode activateRequest = getActivateRequest(password);
        JsonNode tokenInfo = readResponse(doPost("/api/noauth/activate", activateRequest).andExpect(status().isOk()), JsonNode.class);
        validateAndSetJwtToken(tokenInfo, user.getEmail());
        return savedUser;
    }

    protected User createUser(User user, String password) throws Exception {
        User savedUser = doPost("/api/user", user, User.class);
        JsonNode activateRequest = getActivateRequest(password);
        ResultActions resultActions = doPost("/api/noauth/activate", activateRequest);
        resultActions.andExpect(status().isOk());
        return savedUser;
    }

    private JsonNode getActivateRequest(String password) throws Exception {
        doGet("/api/noauth/activate?activateToken={activateToken}", this.currentActivateToken)
                .andExpect(status().isSeeOther())
                .andExpect(header().string(HttpHeaders.LOCATION, "/login/createPassword?activateToken=" + this.currentActivateToken));
        return JacksonUtil.newObjectNode()
                .put("activateToken", this.currentActivateToken)
                .put("password", password);
    }

    protected void login(String username, String password) throws Exception {
        resetTokens();
        JsonNode tokenInfo = readResponse(doPost("/api/auth/login", new LoginRequest(username, password)).andExpect(status().isOk()), JsonNode.class);
        validateAndSetJwtToken(tokenInfo, username);
    }

    protected void refreshToken() throws Exception {
        this.token = null;
        JsonNode tokenInfo = readResponse(doPost("/api/auth/token", new RefreshTokenRequest(this.refreshToken)).andExpect(status().isOk()), JsonNode.class);
        validateAndSetJwtToken(tokenInfo, this.username);
    }

    protected void validateAndSetJwtToken(JsonNode tokenInfo, String username) {
        Assert.assertNotNull(tokenInfo);
        Assert.assertTrue(tokenInfo.has("token"));
        Assert.assertTrue(tokenInfo.has("refreshToken"));
        String token = tokenInfo.get("token").asText();
        String refreshToken = tokenInfo.get("refreshToken").asText();
        validateJwtToken(token, username);
        validateJwtToken(refreshToken, username);
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
    }

    protected void validateJwtToken(String token, String username) {
        Jws<Claims> jwsClaims = jwtTokenFactory.parseTokenClaims(token);
        Claims claims = jwsClaims.getPayload();
        String subject = claims.getSubject();
        Assert.assertEquals(username, subject);
    }

    protected void resetTokens() throws Exception {
        this.token = null;
        this.refreshToken = null;
        this.username = null;
    }

    protected void logout() throws Exception {
        doPost("/api/auth/logout").andExpect(status().isOk());
    }

    protected void setJwtToken(MockHttpServletRequestBuilder request) {
        if (this.token != null) {
            request.header(WinstarcloudSecurityConfiguration.JWT_TOKEN_HEADER_PARAM, "Bearer " + this.token);
        }
        if (this.mobileToken != null) {
            request.header(UserController.MOBILE_TOKEN_HEADER, this.mobileToken);
        }
    }

    protected DeviceProfile createDeviceProfile(String name) {
        return createDeviceProfile(name, null);
    }

    protected DeviceProfile createDeviceProfile(String name, DeviceProfileTransportConfiguration deviceProfileTransportConfiguration) {
        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setName(name);
        deviceProfile.setType(DeviceProfileType.DEFAULT);
        deviceProfile.setDescription(name + " Test");
        DeviceProfileData deviceProfileData = new DeviceProfileData();
        DefaultDeviceProfileConfiguration configuration = new DefaultDeviceProfileConfiguration();
        deviceProfileData.setConfiguration(configuration);
        if (deviceProfileTransportConfiguration != null) {
            deviceProfile.setTransportType(deviceProfileTransportConfiguration.getType());
            deviceProfileData.setTransportConfiguration(deviceProfileTransportConfiguration);
        } else {
            deviceProfile.setTransportType(DeviceTransportType.DEFAULT);
            deviceProfileData.setTransportConfiguration(new DefaultDeviceProfileTransportConfiguration());
        }
        deviceProfile.setProfileData(deviceProfileData);
        deviceProfile.setDefault(false);
        deviceProfile.setDefaultRuleChainId(null);
        return deviceProfile;
    }

    protected AssetProfile createAssetProfile(String name) {
        AssetProfile assetProfile = new AssetProfile();
        assetProfile.setName(name);
        assetProfile.setDescription(name + " Test");
        assetProfile.setDefault(false);
        assetProfile.setDefaultRuleChainId(null);
        return assetProfile;
    }

    protected Device createDevice(String name, String accessToken) throws Exception {
        Device device = new Device();
        device.setName(name);
        device.setType("default");
        DeviceData deviceData = new DeviceData();
        deviceData.setTransportConfiguration(new DefaultDeviceTransportConfiguration());
        deviceData.setConfiguration(new DefaultDeviceConfiguration());
        device.setDeviceData(deviceData);
        return doPost("/api/device?accessToken=" + accessToken, device, Device.class);
    }

    protected MqttDeviceProfileTransportConfiguration createMqttDeviceProfileTransportConfiguration(TransportPayloadTypeConfiguration transportPayloadTypeConfiguration, boolean sendAckOnValidationException) {
        MqttDeviceProfileTransportConfiguration mqttDeviceProfileTransportConfiguration = new MqttDeviceProfileTransportConfiguration();
        mqttDeviceProfileTransportConfiguration.setDeviceTelemetryTopic(MqttTopics.DEVICE_TELEMETRY_TOPIC);
        mqttDeviceProfileTransportConfiguration.setDeviceAttributesTopic(MqttTopics.DEVICE_ATTRIBUTES_TOPIC);
        mqttDeviceProfileTransportConfiguration.setDeviceAttributesSubscribeTopic(MqttTopics.DEVICE_ATTRIBUTES_TOPIC);
        mqttDeviceProfileTransportConfiguration.setSendAckOnValidationException(sendAckOnValidationException);
        mqttDeviceProfileTransportConfiguration.setTransportPayloadTypeConfiguration(transportPayloadTypeConfiguration);
        return mqttDeviceProfileTransportConfiguration;
    }

    protected MqttDeviceProfileTransportConfiguration createMqttDeviceProfileTransportConfiguration(TransportPayloadTypeConfiguration transportPayloadTypeConfiguration, boolean sendAckOnValidationException,
                                                                                                    String telemetryTopic, String attributesPublishTopic, String attributesSubscribeTopic) {
        MqttDeviceProfileTransportConfiguration mqttDeviceProfileTransportConfiguration = new MqttDeviceProfileTransportConfiguration();
        mqttDeviceProfileTransportConfiguration.setDeviceTelemetryTopic(telemetryTopic);
        mqttDeviceProfileTransportConfiguration.setDeviceAttributesTopic(attributesPublishTopic);
        mqttDeviceProfileTransportConfiguration.setDeviceAttributesSubscribeTopic(attributesSubscribeTopic);
        mqttDeviceProfileTransportConfiguration.setSendAckOnValidationException(sendAckOnValidationException);
        mqttDeviceProfileTransportConfiguration.setTransportPayloadTypeConfiguration(transportPayloadTypeConfiguration);
        return mqttDeviceProfileTransportConfiguration;
    }

    protected ProtoTransportPayloadConfiguration createProtoTransportPayloadConfiguration(String attributesProtoSchema, String telemetryProtoSchema, String rpcRequestProtoSchema, String rpcResponseProtoSchema) {
        ProtoTransportPayloadConfiguration protoTransportPayloadConfiguration = new ProtoTransportPayloadConfiguration();
        protoTransportPayloadConfiguration.setDeviceAttributesProtoSchema(attributesProtoSchema);
        protoTransportPayloadConfiguration.setDeviceTelemetryProtoSchema(telemetryProtoSchema);
        protoTransportPayloadConfiguration.setDeviceRpcRequestProtoSchema(rpcRequestProtoSchema);
        protoTransportPayloadConfiguration.setDeviceRpcResponseProtoSchema(rpcResponseProtoSchema);
        return protoTransportPayloadConfiguration;
    }

    protected Device createDevice(String deviceName, String type, String accessToken) throws Exception {
        Device device = new Device();
        device.setName(deviceName);
        device.setType(type);

        DeviceCredentials credentials = new DeviceCredentials();
        credentials.setCredentialsType(DeviceCredentialsType.ACCESS_TOKEN);
        credentials.setCredentialsId(accessToken);

        SaveDeviceWithCredentialsRequest request = new SaveDeviceWithCredentialsRequest(device, credentials);
        return doPost("/api/device-with-credentials", request, Device.class);
    }

    protected ResultActions doGet(String urlTemplate, Object... urlVariables) throws Exception {
        MockHttpServletRequestBuilder getRequest = get(urlTemplate, urlVariables);
        setJwtToken(getRequest);
        return mockMvc.perform(getRequest);
    }

    protected ResultActions doGet(String urlTemplate, HttpHeaders httpHeaders, Object... urlVariables) throws Exception {
        MockHttpServletRequestBuilder getRequest = get(urlTemplate, urlVariables);
        getRequest.headers(httpHeaders);
        setJwtToken(getRequest);
        return mockMvc.perform(getRequest);
    }

    protected <T> T doGet(String urlTemplate, Class<T> responseClass, Object... urlVariables) throws Exception {
        return readResponse(doGet(urlTemplate, urlVariables).andExpect(status().isOk()), responseClass);
    }

    protected <T> T doGet(String urlTemplate, Class<T> responseClass, ResultMatcher resultMatcher, Object... urlVariables) throws Exception {
        return readResponse(doGet(urlTemplate, urlVariables).andExpect(resultMatcher), responseClass);
    }

    protected <T> T doGetAsync(String urlTemplate, Class<T> responseClass, Object... urlVariables) throws Exception {
        return readResponse(doGetAsync(urlTemplate, urlVariables).andExpect(status().isOk()), responseClass);
    }

    protected <T> T doGetAsyncTyped(String urlTemplate, TypeReference<T> responseType, Object... urlVariables) throws Exception {
        return readResponse(doGetAsync(urlTemplate, urlVariables).andExpect(status().isOk()), responseType);
    }

    protected ResultActions doGetAsync(String urlTemplate, Object... urlVariables) throws Exception {
        MockHttpServletRequestBuilder getRequest;
        getRequest = get(urlTemplate, urlVariables);
        setJwtToken(getRequest);
        return mockMvc.perform(asyncDispatch(mockMvc.perform(getRequest).andExpect(request().asyncStarted()).andReturn()));
    }

    protected <T> T doGetTyped(String urlTemplate, TypeReference<T> responseType, Object... urlVariables) throws Exception {
        return readResponse(doGet(urlTemplate, urlVariables).andExpect(status().isOk()), responseType);
    }

    protected <T> T doGetTypedWithPageLink(String urlTemplate, TypeReference<T> responseType,
                                           PageLink pageLink,
                                           Object... urlVariables) throws Exception {
        List<Object> pageLinkVariables = new ArrayList<>();
        urlTemplate += "pageSize={pageSize}&page={page}";
        pageLinkVariables.add(pageLink.getPageSize());
        pageLinkVariables.add(pageLink.getPage());
        if (StringUtils.isNotEmpty(pageLink.getTextSearch())) {
            urlTemplate += "&textSearch={textSearch}";
            pageLinkVariables.add(pageLink.getTextSearch());
        }
        if (pageLink.getSortOrder() != null) {
            urlTemplate += "&sortProperty={sortProperty}&sortOrder={sortOrder}";
            pageLinkVariables.add(pageLink.getSortOrder().getProperty());
            pageLinkVariables.add(pageLink.getSortOrder().getDirection().name());
        }

        Object[] vars = new Object[urlVariables.length + pageLinkVariables.size()];
        System.arraycopy(urlVariables, 0, vars, 0, urlVariables.length);
        System.arraycopy(pageLinkVariables.toArray(), 0, vars, urlVariables.length, pageLinkVariables.size());

        return readResponse(doGet(urlTemplate, vars).andExpect(status().isOk()), responseType);
    }

    protected <T> T doGetTypedWithTimePageLink(String urlTemplate, TypeReference<T> responseType,
                                               TimePageLink pageLink,
                                               Object... urlVariables) throws Exception {
        List<Object> pageLinkVariables = new ArrayList<>();
        urlTemplate += "pageSize={pageSize}&page={page}";
        pageLinkVariables.add(pageLink.getPageSize());
        pageLinkVariables.add(pageLink.getPage());
        if (pageLink.getStartTime() != null) {
            urlTemplate += "&startTime={startTime}";
            pageLinkVariables.add(pageLink.getStartTime());
        }
        if (pageLink.getEndTime() != null) {
            urlTemplate += "&endTime={endTime}";
            pageLinkVariables.add(pageLink.getEndTime());
        }
        if (StringUtils.isNotEmpty(pageLink.getTextSearch())) {
            urlTemplate += "&textSearch={textSearch}";
            pageLinkVariables.add(pageLink.getTextSearch());
        }
        if (pageLink.getSortOrder() != null) {
            urlTemplate += "&sortProperty={sortProperty}&sortOrder={sortOrder}";
            pageLinkVariables.add(pageLink.getSortOrder().getProperty());
            pageLinkVariables.add(pageLink.getSortOrder().getDirection().name());
        }
        Object[] vars = new Object[urlVariables.length + pageLinkVariables.size()];
        System.arraycopy(urlVariables, 0, vars, 0, urlVariables.length);
        System.arraycopy(pageLinkVariables.toArray(), 0, vars, urlVariables.length, pageLinkVariables.size());

        return readResponse(doGet(urlTemplate, vars).andExpect(status().isOk()), responseType);
    }

    protected <T> T doPost(String urlTemplate, Class<T> responseClass, String... params) {
        try {
            return readResponse(doPost(urlTemplate, params).andExpect(status().isOk()), responseClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T doPost(String urlTemplate, T content, Class<T> responseClass, ResultMatcher resultMatcher, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(resultMatcher), responseClass);
    }

    protected <T, R> R doPost(String urlTemplate, T content, Class<R> responseClass, String... params) {
        try {
            return readResponse(doPost(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T, R> R doPostWithResponse(String urlTemplate, T content, Class<R> responseClass, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
    }

    protected <T, R> R doPostWithTypedResponse(String urlTemplate, T content, TypeReference<R> responseType, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(status().isOk()), responseType);
    }

    protected <T, R> R doPostWithTypedResponse(String urlTemplate, T content, TypeReference<R> responseType, ResultMatcher resultMatcher, String... params) throws Exception {
        return readResponse(doPost(urlTemplate, content, params).andExpect(resultMatcher), responseType);
    }

    protected <T, R> R doPostAsync(String urlTemplate, T content, Class<R> responseClass, ResultMatcher resultMatcher, String... params) throws Exception {
        return readResponse(doPostAsync(urlTemplate, content, DEFAULT_TIMEOUT, params).andExpect(resultMatcher), responseClass);
    }

    protected <T> T doPostAsync(String urlTemplate, T content, Class<T> responseClass, ResultMatcher resultMatcher, Long timeout, String... params) throws Exception {
        return readResponse(doPostAsync(urlTemplate, content, timeout, params).andExpect(resultMatcher), responseClass);
    }

    protected <T> T doPostClaimAsync(String urlTemplate, Object content, Class<T> responseClass, ResultMatcher resultMatcher, String... params) throws Exception {
        return readResponse(doPostAsync(urlTemplate, content, DEFAULT_TIMEOUT, params).andExpect(resultMatcher), responseClass);
    }

    protected <T> T doPut(String urlTemplate, Object content, Class<T> responseClass, String... params) {
        try {
            return readResponse(doPut(urlTemplate, content, params).andExpect(status().isOk()), responseClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> ResultActions doPut(String urlTemplate, T content, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = put(urlTemplate, params);
        setJwtToken(postRequest);
        String json = json(content);
        postRequest.contentType(contentType).content(json);
        return mockMvc.perform(postRequest);
    }

    protected <T> T doDelete(String urlTemplate, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doDelete(urlTemplate, params).andExpect(status().isOk()), responseClass);
    }

    protected <T> T doDeleteAsync(String urlTemplate, Class<T> responseClass, String... params) throws Exception {
        return readResponse(doDeleteAsync(urlTemplate, DEFAULT_TIMEOUT, params).andExpect(status().isOk()), responseClass);
    }

    protected ResultActions doPost(String urlTemplate, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = post(urlTemplate);
        setJwtToken(postRequest);
        populateParams(postRequest, params);
        return mockMvc.perform(postRequest);
    }

    protected <T> ResultActions doPost(String urlTemplate, T content, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = post(urlTemplate, params);
        setJwtToken(postRequest);
        String json = json(content);
        postRequest.contentType(contentType).content(json);
        return mockMvc.perform(postRequest);
    }

    protected <T> ResultActions doPostAsync(String urlTemplate, T content, Long timeout, String... params) throws Exception {
        MockHttpServletRequestBuilder postRequest = post(urlTemplate, params);
        setJwtToken(postRequest);
        String json = json(content);
        postRequest.contentType(contentType).content(json);
        MvcResult result = mockMvc.perform(postRequest).andReturn();
        result.getAsyncResult(timeout);
        return mockMvc.perform(asyncDispatch(result));
    }

    protected ResultActions doDelete(String urlTemplate, String... params) throws Exception {
        MockHttpServletRequestBuilder deleteRequest = delete(urlTemplate);
        setJwtToken(deleteRequest);
        populateParams(deleteRequest, params);
        return mockMvc.perform(deleteRequest);
    }

    protected ResultActions doDeleteAsync(String urlTemplate, Long timeout, String... params) throws Exception {
        MockHttpServletRequestBuilder deleteRequest = delete(urlTemplate, params);
        setJwtToken(deleteRequest);
//        populateParams(deleteRequest, params);
        MvcResult result = mockMvc.perform(deleteRequest).andReturn();
        result.getAsyncResult(timeout);
        return mockMvc.perform(asyncDispatch(result));
    }

    protected void populateParams(MockHttpServletRequestBuilder request, String... params) {
        if (params != null && params.length > 0) {
            Assert.assertEquals(0, params.length % 2);
            MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
            for (int i = 0; i < params.length; i += 2) {
                paramsMap.add(params[i], params[i + 1]);
            }
            request.params(paramsMap);
        }
    }

    @SuppressWarnings("unchecked")
    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();

        HttpMessageConverter converter = o instanceof String ? stringHttpMessageConverter : mappingJackson2HttpMessageConverter;
        converter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    @SuppressWarnings("unchecked")
    protected <T> T readResponse(ResultActions result, Class<T> responseClass) throws Exception {
        byte[] content = result.andReturn().getResponse().getContentAsByteArray();
        MockHttpInputMessage mockHttpInputMessage = new MockHttpInputMessage(content);
        HttpMessageConverter converter = responseClass.equals(String.class) ? stringHttpMessageConverter : mappingJackson2HttpMessageConverter;
        return (T) converter.read(responseClass, mockHttpInputMessage);
    }

    protected <T> T readResponse(ResultActions result, TypeReference<T> type) throws Exception {
        return readResponse(result.andReturn(), type);
    }

    protected <T> T readResponse(MvcResult result, TypeReference<T> type) throws Exception {
        byte[] content = result.getResponse().getContentAsByteArray();
        return JacksonUtil.OBJECT_MAPPER.readerFor(type).readValue(content);
    }

    protected String getErrorMessage(ResultActions result) throws Exception {
        return readResponse(result, JsonNode.class).get("message").asText();
    }

    public class IdComparator<D extends HasId> implements Comparator<D> {

        @Override
        public int compare(D o1, D o2) {
            return o1.getId().getId().compareTo(o2.getId().getId());
        }

    }

    public class EntityIdComparator<D extends EntityId> implements Comparator<D> {

        @Override
        public int compare(D o1, D o2) {
            return o1.getId().compareTo(o2.getId());
        }

    }

    protected static <T> ResultMatcher statusReason(Matcher<T> matcher) {
        return jsonPath("$.message", matcher);
    }

    protected Edge constructEdge(String name, String type) {
        return constructEdge(tenantId, name, type, StringUtils.randomAlphanumeric(20), StringUtils.randomAlphanumeric(20));
    }

    protected Edge constructEdge(TenantId tenantId, String name, String type, String routingKey, String secret) {
        Edge edge = new Edge();
        edge.setTenantId(tenantId);
        edge.setName(name);
        edge.setType(type);
        edge.setRoutingKey(routingKey);
        edge.setSecret(secret);
        return edge;
    }

    protected <T extends HasId<? extends UUIDBased>> ListenableFuture<List<ResultActions>> deleteEntitiesAsync(String urlTemplate, List<T> entities, ListeningExecutorService executor) {
        List<ListenableFuture<ResultActions>> futures = new ArrayList<>(entities.size());
        for (T entity : entities) {
            futures.add(executor.submit(() ->
                    doDelete(urlTemplate + entity.getId().getId())
                            .andExpect(status().isOk())));
        }
        return Futures.allAsList(futures);
    }

    protected void testEntityDaoWithRelationsOk(EntityId entityIdFrom, EntityId entityTo, String urlDelete) throws Exception {
        createEntityRelation(entityIdFrom, entityTo, "TEST_TYPE");
        assertThat(findRelationsByTo(entityTo)).hasSize(1);

        doDelete(urlDelete).andExpect(status().isOk());

        assertThat(findRelationsByTo(entityTo)).hasSize(0);
    }

    protected <T> void testEntityDaoWithRelationsTransactionalException(Dao<T> dao, EntityId entityIdFrom, EntityId entityTo,
                                                                        String urlDelete) throws Exception {
        Mockito.doThrow(new ConstraintViolationException("mock message", new SQLException(), "MOCK_CONSTRAINT")).when(dao).removeById(any(), any());
        try {
            createEntityRelation(entityIdFrom, entityTo, "TEST_TRANSACTIONAL_TYPE");
            assertThat(findRelationsByTo(entityTo)).hasSize(1);

            doDelete(urlDelete)
                    .andExpect(status().isInternalServerError());

            assertThat(findRelationsByTo(entityTo)).hasSize(1);
        } finally {
            Mockito.reset(dao);
        }
    }

    protected void createEntityRelation(EntityId entityIdFrom, EntityId entityIdTo, String typeRelation) throws Exception {
        EntityRelation relation = new EntityRelation(entityIdFrom, entityIdTo, typeRelation);
        doPost("/api/relation", relation);
    }

    protected List<EntityRelation> findRelationsByTo(EntityId entityId) throws Exception {
        String url = String.format("/api/relations?toId=%s&toType=%s", entityId.getId(), entityId.getEntityType().name());
        MvcResult mvcResult = doGet(url).andReturn();

        switch (mvcResult.getResponse().getStatus()) {
            case 200:
                return readResponse(mvcResult, new TypeReference<>() {
                });
            case 404:
                return Collections.emptyList();
        }
        throw new AssertionError("Unexpected status " + mvcResult.getResponse().getStatus());
    }

    protected static <T> T getFieldValue(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    protected static void setStaticFieldValue(Class<?> targetCls, String fieldName, Object value) throws Exception {
        Field field = targetCls.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    protected static void setStaticFinalFieldValue(Class<?> targetCls, String fieldName, Object value) throws Exception {
        Field field = targetCls.getDeclaredField(fieldName);
        field.setAccessible(true);
        // Get the VarHandle for the 'modifiers' field in the Field class
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
        VarHandle modifiersHandle = lookup.findVarHandle(Field.class, "modifiers", int.class);

        // Remove the final modifier from the field
        int currentModifiers = field.getModifiers();
        modifiersHandle.set(field, currentModifiers & ~Modifier.FINAL);

        // Set the new value
        field.set(null, value);
    }

    protected int getDeviceActorSubscriptionCount(DeviceId deviceId, FeatureType featureType) {
        DeviceActorMessageProcessor processor = getDeviceActorProcessor(deviceId);
        Map<UUID, SessionInfo> subscriptions = (Map<UUID, SessionInfo>) ReflectionTestUtils.getField(processor, getMapName(featureType));
        return subscriptions.size();
    }

    protected void awaitForDeviceActorToReceiveSubscription(DeviceId deviceId, FeatureType featureType, int subscriptionCount) {
        DeviceActorMessageProcessor processor = getDeviceActorProcessor(deviceId);
        Map<UUID, SessionInfo> subscriptions = (Map<UUID, SessionInfo>) ReflectionTestUtils.getField(processor, getMapName(featureType));
        Awaitility.await("Device actor received subscription command from the transport").atMost(5, TimeUnit.SECONDS).until(() -> {
            log.warn("device {}, subscriptions.size() == {}", deviceId, subscriptions.size());
            return subscriptions.size() == subscriptionCount;
        });
    }

    protected void awaitForDeviceActorToProcessAllRpcResponses(DeviceId deviceId) {
        DeviceActorMessageProcessor processor = getDeviceActorProcessor(deviceId);
        Map<Integer, ToDeviceRpcRequestMetadata> toDeviceRpcPendingMap = (Map<Integer, ToDeviceRpcRequestMetadata>) ReflectionTestUtils.getField(processor, "toDeviceRpcPendingMap");
        Awaitility.await("Device actor pending map is empty").atMost(5, TimeUnit.SECONDS).until(() -> {
            log.warn("device {}, toDeviceRpcPendingMap.size() == {}", deviceId, toDeviceRpcPendingMap.size());
            return toDeviceRpcPendingMap.isEmpty();
        });
    }

    protected void awaitForClaimingInfoToBeRegistered(DeviceId deviceId) {
        CacheManager cacheManager = (CacheManager) ReflectionTestUtils.getField(claimDevicesService, "cacheManager");
        Cache cache = cacheManager.getCache(CLAIM_DEVICES_CACHE);
        Awaitility.await("Claiming request from the transport was registered").atMost(5, TimeUnit.SECONDS).until(() -> {
            Cache.ValueWrapper value = cache.get(List.of(deviceId));
            log.warn("device {}, claimingRequest registered: {}", deviceId, value);
            return value != null;
        });
    }

    protected static String getMapName(FeatureType featureType) {
        switch (featureType) {
            case ATTRIBUTES:
                return "attributeSubscriptions";
            case RPC:
                return "rpcSubscriptions";
            default:
                throw new RuntimeException("Not supported feature " + featureType + "!");
        }
    }

    protected DeviceActorMessageProcessor getDeviceActorProcessor(DeviceId deviceId) {
        DefaultTbActorSystem actorSystem = (DefaultTbActorSystem) ReflectionTestUtils.getField(actorService, "system");
        ConcurrentMap<TbActorId, TbActorMailbox> actors = (ConcurrentMap<TbActorId, TbActorMailbox>) ReflectionTestUtils.getField(actorSystem, "actors");
        Awaitility.await("Device actor was created").atMost(TIMEOUT, TimeUnit.SECONDS)
                .until(() -> actors.containsKey(new TbEntityActorId(deviceId)));
        TbActorMailbox actorMailbox = actors.get(new TbEntityActorId(deviceId));
        DeviceActor actor = (DeviceActor) ReflectionTestUtils.getField(actorMailbox, "actor");
        return (DeviceActorMessageProcessor) ReflectionTestUtils.getField(actor, "processor");
    }

    protected void updateDefaultTenantProfileConfig(Consumer<DefaultTenantProfileConfiguration> updater) throws WinstarcloudException {
        updateDefaultTenantProfile(tenantProfile -> {
            TenantProfileData profileData = tenantProfile.getProfileData();
            DefaultTenantProfileConfiguration profileConfiguration = (DefaultTenantProfileConfiguration) profileData.getConfiguration();
            updater.accept(profileConfiguration);
            tenantProfile.setProfileData(profileData);
        });
    }

    protected void updateDefaultTenantProfile(Consumer<TenantProfile> updater) throws WinstarcloudException {
        TenantProfile oldTenantProfile = tenantProfileService.findDefaultTenantProfile(TenantId.SYS_TENANT_ID);
        TenantProfile tenantProfile = JacksonUtil.clone(oldTenantProfile);
        updater.accept(tenantProfile);
        tbTenantProfileService.save(TenantId.SYS_TENANT_ID, tenantProfile, oldTenantProfile);
    }

}
