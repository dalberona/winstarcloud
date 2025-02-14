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
package org.winstarcloud.server.service.system;

import com.google.common.util.concurrent.FutureCallback;
import com.google.protobuf.ProtocolStringList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.common.util.WinstarCloudThreadFactory;
import org.winstarcloud.rule.engine.api.MailService;
import org.winstarcloud.rule.engine.api.SmsService;
import org.winstarcloud.server.common.data.AdminSettings;
import org.winstarcloud.server.common.data.ApiUsageState;
import org.winstarcloud.server.common.data.FeaturesInfo;
import org.winstarcloud.server.common.data.SystemInfo;
import org.winstarcloud.server.common.data.SystemInfoData;
import org.winstarcloud.server.common.data.id.TenantId;
import org.winstarcloud.server.common.data.kv.BasicTsKvEntry;
import org.winstarcloud.server.common.data.kv.BooleanDataEntry;
import org.winstarcloud.server.common.data.kv.JsonDataEntry;
import org.winstarcloud.server.common.data.kv.LongDataEntry;
import org.winstarcloud.server.common.data.kv.TsKvEntry;
import org.winstarcloud.server.common.msg.queue.ServiceType;
import org.winstarcloud.server.common.stats.TbApiUsageStateClient;
import org.winstarcloud.server.dao.oauth2.OAuth2Service;
import org.winstarcloud.server.dao.settings.AdminSettingsService;
import org.winstarcloud.server.gen.transport.TransportProtos.ServiceInfo;
import org.winstarcloud.server.queue.discovery.DiscoveryService;
import org.winstarcloud.server.queue.discovery.PartitionService;
import org.winstarcloud.server.queue.discovery.TbApplicationEventListener;
import org.winstarcloud.server.queue.discovery.TbServiceInfoProvider;
import org.winstarcloud.server.queue.discovery.event.PartitionChangeEvent;
import org.winstarcloud.server.queue.util.TbCoreComponent;
import org.winstarcloud.server.service.telemetry.TelemetrySubscriptionService;

import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.winstarcloud.common.util.SystemUtil.getCpuCount;
import static org.winstarcloud.common.util.SystemUtil.getCpuUsage;
import static org.winstarcloud.common.util.SystemUtil.getDiscSpaceUsage;
import static org.winstarcloud.common.util.SystemUtil.getMemoryUsage;
import static org.winstarcloud.common.util.SystemUtil.getTotalDiscSpace;
import static org.winstarcloud.common.util.SystemUtil.getTotalMemory;

@TbCoreComponent
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultSystemInfoService extends TbApplicationEventListener<PartitionChangeEvent> implements SystemInfoService {

    public static final FutureCallback<Integer> CALLBACK = new FutureCallback<>() {
        @Override
        public void onSuccess(@Nullable Integer result) {
        }

        @Override
        public void onFailure(Throwable t) {
            log.warn("Failed to persist system info", t);
        }
    };

    private final TbServiceInfoProvider serviceInfoProvider;
    private final PartitionService partitionService;
    private final DiscoveryService discoveryService;
    private final TelemetrySubscriptionService telemetryService;
    private final TbApiUsageStateClient apiUsageStateClient;
    private final AdminSettingsService adminSettingsService;
    private final OAuth2Service oAuth2Service;
    private final MailService mailService;
    private final SmsService smsService;
    private volatile ScheduledExecutorService scheduler;

    @Value("${metrics.system_info.persist_frequency:60}")
    private int systemInfoPersistFrequencySeconds;
    @Value("#{${metrics.system_info.ttl:7} * 86400}")
    private int systemInfoTtlSeconds;

    @Override
    protected void onTbApplicationEvent(PartitionChangeEvent partitionChangeEvent) {
        if (ServiceType.TB_CORE.equals(partitionChangeEvent.getServiceType())) {
            boolean myPartition = partitionService.resolve(ServiceType.TB_CORE, TenantId.SYS_TENANT_ID, TenantId.SYS_TENANT_ID).isMyPartition();
            synchronized (this) {
                if (myPartition) {
                    if (scheduler == null) {
                        scheduler = Executors.newSingleThreadScheduledExecutor(WinstarCloudThreadFactory.forName("tb-system-info-scheduler"));
                        scheduler.scheduleWithFixedDelay(this::saveCurrentSystemInfo, 0, systemInfoPersistFrequencySeconds, TimeUnit.SECONDS);
                    }
                } else {
                    destroy();
                }
            }
        }
    }

    @Override
    public SystemInfo getSystemInfo() {
        SystemInfo systemInfo = new SystemInfo();

        if (discoveryService.isMonolith()) {
            systemInfo.setMonolith(true);
            systemInfo.setSystemData(Collections.singletonList(createSystemInfoData(serviceInfoProvider.generateNewServiceInfoWithCurrentSystemInfo())));
        } else {
            systemInfo.setSystemData(getSystemData(serviceInfoProvider.getServiceInfo()));
        }

        return systemInfo;
    }

    protected void saveCurrentSystemInfo() {
        if (discoveryService.isMonolith()) {
            saveCurrentMonolithSystemInfo();
        } else {
            saveCurrentClusterSystemInfo();
        }
    }

    @Override
    public FeaturesInfo getFeaturesInfo() {
        FeaturesInfo featuresInfo = new FeaturesInfo();
        featuresInfo.setEmailEnabled(isEmailEnabled());
        featuresInfo.setSmsEnabled(smsService.isConfigured(TenantId.SYS_TENANT_ID));
        featuresInfo.setOauthEnabled(oAuth2Service.findOAuth2Info().isEnabled());
        featuresInfo.setTwoFaEnabled(isTwoFaEnabled());
        featuresInfo.setNotificationEnabled(isSlackEnabled());
        return featuresInfo;
    }

    private boolean isEmailEnabled() {
        try {
            mailService.testConnection(TenantId.SYS_TENANT_ID);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTwoFaEnabled() {
        AdminSettings twoFaSettings = adminSettingsService.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, "twoFaSettings");
        if (twoFaSettings != null) {
            var providers = twoFaSettings.getJsonValue().get("providers");
            if (providers != null) {
                return providers.size() > 0;
            }
        }
        return false;
    }

    private boolean isSlackEnabled() {
        AdminSettings notifications = adminSettingsService.findAdminSettingsByKey(TenantId.SYS_TENANT_ID, "notifications");
        if (notifications != null) {
            return notifications.getJsonValue().get("deliveryMethodsConfigs").has("SLACK");
        }
        return false;
    }

    private void saveCurrentClusterSystemInfo() {
        long ts = System.currentTimeMillis();
        List<SystemInfoData> clusterSystemData = getSystemData(serviceInfoProvider.getServiceInfo());
        BasicTsKvEntry clusterDataKv = new BasicTsKvEntry(ts, new JsonDataEntry("clusterSystemData", JacksonUtil.toString(clusterSystemData)));
        doSave(Arrays.asList(new BasicTsKvEntry(ts, new BooleanDataEntry("clusterMode", true)), clusterDataKv));
    }

    private void saveCurrentMonolithSystemInfo() {
        long ts = System.currentTimeMillis();
        List<TsKvEntry> tsList = new ArrayList<>();
        tsList.add(new BasicTsKvEntry(ts, new BooleanDataEntry("clusterMode", false)));
        getCpuUsage().ifPresent(v -> tsList.add(new BasicTsKvEntry(ts, new LongDataEntry("cpuUsage", (long) v))));
        getMemoryUsage().ifPresent(v -> tsList.add(new BasicTsKvEntry(ts, new LongDataEntry("memoryUsage", (long) v))));
        getDiscSpaceUsage().ifPresent(v -> tsList.add(new BasicTsKvEntry(ts, new LongDataEntry("discUsage", (long) v))));

        getCpuCount().ifPresent(v -> tsList.add(new BasicTsKvEntry(ts, new LongDataEntry("cpuCount", (long) v))));
        getTotalMemory().ifPresent(v -> tsList.add(new BasicTsKvEntry(ts, new LongDataEntry("totalMemory", v))));
        getTotalDiscSpace().ifPresent(v -> tsList.add(new BasicTsKvEntry(ts, new LongDataEntry("totalDiscSpace", v))));

        doSave(tsList);
    }

    private void doSave(List<TsKvEntry> telemetry) {
        ApiUsageState apiUsageState = apiUsageStateClient.getApiUsageState(TenantId.SYS_TENANT_ID);
        telemetryService.saveAndNotifyInternal(TenantId.SYS_TENANT_ID, apiUsageState.getId(), telemetry, systemInfoTtlSeconds, CALLBACK);
    }

    private List<SystemInfoData> getSystemData(ServiceInfo serviceInfo) {
        List<SystemInfoData> clusterSystemData = new ArrayList<>();
        clusterSystemData.add(createSystemInfoData(serviceInfo));
        this.discoveryService.getOtherServers()
                .stream()
                .map(this::createSystemInfoData)
                .forEach(clusterSystemData::add);
        return clusterSystemData;
    }

    private SystemInfoData createSystemInfoData(ServiceInfo serviceInfo) {
        ProtocolStringList serviceTypes = serviceInfo.getServiceTypesList();
        SystemInfoData infoData = new SystemInfoData();
        infoData.setServiceId(serviceInfo.getServiceId());
        infoData.setServiceType(serviceTypes.size() > 1 ? "MONOLITH" : serviceTypes.get(0));

        infoData.setCpuUsage(serviceInfo.getSystemInfo().getCpuUsage());
        infoData.setMemoryUsage(serviceInfo.getSystemInfo().getMemoryUsage());
        infoData.setDiscUsage(serviceInfo.getSystemInfo().getDiskUsage());

        infoData.setCpuCount(serviceInfo.getSystemInfo().getCpuCount());
        infoData.setTotalMemory(serviceInfo.getSystemInfo().getTotalMemory());
        infoData.setTotalDiscSpace(serviceInfo.getSystemInfo().getTotalDiscSpace());

        return infoData;
    }

    @PreDestroy
    private void destroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}
