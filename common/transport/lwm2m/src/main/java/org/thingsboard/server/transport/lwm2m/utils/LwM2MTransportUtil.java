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
package org.winstarcloud.server.transport.lwm2m.utils;

import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.util.Hex;
import org.winstarcloud.common.util.JacksonUtil;
import org.winstarcloud.server.common.data.DeviceProfile;
import org.winstarcloud.server.common.data.DeviceTransportType;
import org.winstarcloud.server.common.data.StringUtils;
import org.winstarcloud.server.common.data.device.profile.DeviceProfileTransportConfiguration;
import org.winstarcloud.server.common.data.device.profile.Lwm2mDeviceProfileTransportConfiguration;
import org.winstarcloud.server.common.data.device.profile.lwm2m.bootstrap.LwM2MBootstrapServerCredential;
import org.winstarcloud.server.common.data.ota.OtaPackageKey;
import org.winstarcloud.server.common.transport.util.JsonUtils;
import org.winstarcloud.server.transport.lwm2m.config.TbLwM2mVersion;
import org.winstarcloud.server.transport.lwm2m.server.LwM2mOtaConvert;
import org.winstarcloud.server.transport.lwm2m.server.client.LwM2mClient;
import org.winstarcloud.server.transport.lwm2m.server.client.ResourceValue;
import org.winstarcloud.server.transport.lwm2m.server.downlink.HasVersionedId;
import org.winstarcloud.server.transport.lwm2m.server.ota.firmware.FirmwareUpdateResult;
import org.winstarcloud.server.transport.lwm2m.server.ota.firmware.FirmwareUpdateState;
import org.winstarcloud.server.transport.lwm2m.server.ota.software.SoftwareUpdateResult;
import org.winstarcloud.server.transport.lwm2m.server.ota.software.SoftwareUpdateState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.californium.scandium.config.DtlsConfig.DTLS_CONNECTION_ID_LENGTH;
import static org.eclipse.californium.scandium.config.DtlsConfig.DTLS_CONNECTION_ID_NODE_ID;
import static org.eclipse.leshan.core.model.ResourceModel.Type.BOOLEAN;
import static org.eclipse.leshan.core.model.ResourceModel.Type.FLOAT;
import static org.eclipse.leshan.core.model.ResourceModel.Type.INTEGER;
import static org.eclipse.leshan.core.model.ResourceModel.Type.OBJLNK;
import static org.eclipse.leshan.core.model.ResourceModel.Type.OPAQUE;
import static org.eclipse.leshan.core.model.ResourceModel.Type.STRING;
import static org.eclipse.leshan.core.model.ResourceModel.Type.TIME;
import static org.winstarcloud.server.common.data.lwm2m.LwM2mConstants.LWM2M_SEPARATOR_KEY;
import static org.winstarcloud.server.common.data.lwm2m.LwM2mConstants.LWM2M_SEPARATOR_PATH;
import static org.winstarcloud.server.transport.lwm2m.server.ota.DefaultLwM2MOtaUpdateService.FW_RESULT_ID;
import static org.winstarcloud.server.transport.lwm2m.server.ota.DefaultLwM2MOtaUpdateService.FW_STATE_ID;
import static org.winstarcloud.server.transport.lwm2m.server.ota.DefaultLwM2MOtaUpdateService.SW_RESULT_ID;
import static org.winstarcloud.server.transport.lwm2m.server.ota.DefaultLwM2MOtaUpdateService.SW_STATE_ID;


@Slf4j
public class LwM2MTransportUtil {

    public static final String LWM2M_OBJECT_VERSION_DEFAULT = "1.0";

    public static final String LOG_LWM2M_TELEMETRY = "transportLog";
    public static final String LOG_LWM2M_INFO = "info";
    public static final String LOG_LWM2M_ERROR = "error";
    public static final String LOG_LWM2M_WARN = "warn";
    public static final int BOOTSTRAP_DEFAULT_SHORT_ID_0 = 0;

    public static LwM2mOtaConvert convertOtaUpdateValueToString(String pathIdVer, Object value, ResourceModel.Type currentType) {
        String path = fromVersionedIdToObjectId(pathIdVer);
        LwM2mOtaConvert lwM2mOtaConvert = new LwM2mOtaConvert();
        if (path != null) {
            if (FW_STATE_ID.equals(path)) {
                lwM2mOtaConvert.setCurrentType(STRING);
                lwM2mOtaConvert.setValue(FirmwareUpdateState.fromStateFwByCode(((Long) value).intValue()).getType());
                return lwM2mOtaConvert;
            } else if (FW_RESULT_ID.equals(path)) {
                lwM2mOtaConvert.setCurrentType(STRING);
                lwM2mOtaConvert.setValue(FirmwareUpdateResult.fromUpdateResultFwByCode(((Long) value).intValue()).getType());
                return lwM2mOtaConvert;
            } else if (SW_STATE_ID.equals(path)) {
                lwM2mOtaConvert.setCurrentType(STRING);
                lwM2mOtaConvert.setValue(SoftwareUpdateState.fromUpdateStateSwByCode(((Long) value).intValue()).getType());
                return lwM2mOtaConvert;
            } else if (SW_RESULT_ID.equals(path)) {
                lwM2mOtaConvert.setCurrentType(STRING);
                lwM2mOtaConvert.setValue(SoftwareUpdateResult.fromUpdateResultSwByCode(((Long) value).intValue()).getType());
                return lwM2mOtaConvert;
            }
        }
        lwM2mOtaConvert.setCurrentType(currentType);
        lwM2mOtaConvert.setValue(value);
        return lwM2mOtaConvert;
    }

    public static Lwm2mDeviceProfileTransportConfiguration toLwM2MClientProfile(DeviceProfile deviceProfile) {
        DeviceProfileTransportConfiguration transportConfiguration = deviceProfile.getProfileData().getTransportConfiguration();
        if (transportConfiguration.getType().equals(DeviceTransportType.LWM2M)) {
            return (Lwm2mDeviceProfileTransportConfiguration) transportConfiguration;
        } else {
            log.info("[{}] Received profile with invalid transport configuration: {}", deviceProfile.getId(), deviceProfile.getProfileData().getTransportConfiguration());
            throw new IllegalArgumentException("Received profile with invalid transport configuration: " + transportConfiguration.getType());
        }
    }

    public static List<LwM2MBootstrapServerCredential> getBootstrapParametersFromWinstarcloud(DeviceProfile deviceProfile) {
        return toLwM2MClientProfile(deviceProfile).getBootstrap();
    }

    public static String fromVersionedIdToObjectId(String pathIdVer) {
        try {
            if (pathIdVer == null) {
                return null;
            }
            String[] keyArray = pathIdVer.split(LWM2M_SEPARATOR_PATH);
            if (keyArray.length > 1 && keyArray[1].split(LWM2M_SEPARATOR_KEY).length == 2) {
                keyArray[1] = keyArray[1].split(LWM2M_SEPARATOR_KEY)[0];
                return StringUtils.join(keyArray, LWM2M_SEPARATOR_PATH);
            } else {
                return pathIdVer;
            }
        } catch (Exception e) {
            log.debug("Issue converting path with version [{}] to path without version: ", pathIdVer, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param path - pathId or pathIdVer
     * @return
     */
    public static String getVerFromPathIdVerOrId(String path) {
        try {
            String[] keyArray = path.split(LWM2M_SEPARATOR_PATH);
            if (keyArray.length > 1) {
                String[] keyArrayVer = keyArray[1].split(LWM2M_SEPARATOR_KEY);
                return keyArrayVer.length == 2 ? keyArrayVer[1] : LWM2M_OBJECT_VERSION_DEFAULT;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static String convertObjectIdToVersionedId(String path, LwM2mClient lwM2MClient) {
        String ver = String.valueOf(lwM2MClient.getSupportedObjectVersion(new LwM2mPath(path).getObjectId()));
        return convertObjectIdToVerId(path, ver);
    }
    public static String convertObjectIdToVerId(String path, String ver) {
        ver = ver != null ? ver : TbLwM2mVersion.VERSION_1_0.getVersion().toString();
        try {
            String[] keyArray = path.split(LWM2M_SEPARATOR_PATH);
            if (keyArray.length > 1) {
                keyArray[1] = keyArray[1] + LWM2M_SEPARATOR_KEY + ver;
                return StringUtils.join(keyArray, LWM2M_SEPARATOR_PATH);
            } else {
                return path;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * "UNSIGNED_INTEGER":  // Number -> Integer Example:
     * Alarm Timestamp [32-bit unsigned integer]
     * Short Server ID, Object ID, Object Instance ID, Resource ID, Resource Instance ID
     * "CORELINK": // String used in Attribute
     */
    public static ResourceModel.Type equalsResourceTypeGetSimpleName(Object value) {
        switch (value.getClass().getSimpleName()) {
            case "Double":
                return FLOAT;
            case "Integer":
                return INTEGER;
            case "String":
                return STRING;
            case "Boolean":
                return BOOLEAN;
            case "byte[]":
                return OPAQUE;
            case "Date":
                return TIME;
            case "ObjectLink":
                return OBJLNK;
            default:
                return null;
        }
    }

    public static void validateVersionedId(LwM2mClient client, HasVersionedId request) {
        String msgExceptionStr = "";
        if (request.getObjectId() == null) {
            msgExceptionStr = "Specified object id is null!";
        } else {
            msgExceptionStr = client.isValidObjectVersion(request.getVersionedId());
        }
        if (!msgExceptionStr.isEmpty()) {
            throw new IllegalArgumentException(msgExceptionStr);
        }
    }

    public static Map<Integer, Object> convertMultiResourceValuesFromRpcBody(Object value, ResourceModel.Type type, String versionedId) throws Exception {
            String valueJsonStr = JacksonUtil.toString(value);
            JsonElement element = JsonUtils.parse(valueJsonStr);
            return convertMultiResourceValuesFromJson(element, type, versionedId);
    }

    public static Map<Integer, Object> convertMultiResourceValuesFromJson(JsonElement newValProto, ResourceModel.Type type, String versionedId) {
        Map<Integer, Object> newValues = new HashMap<>();
        newValProto.getAsJsonObject().entrySet().forEach((obj) -> {
            newValues.put(Integer.valueOf(obj.getKey()), convertValueByTypeResource (obj.getValue().getAsString(), type,  versionedId));
        });
        return newValues;
    }

    public static Object convertValueByTypeResource (String value, ResourceModel.Type type,  String versionedId) {
        return LwM2mValueConverterImpl.getInstance().convertValue(value,
                STRING, type, new LwM2mPath(fromVersionedIdToObjectId(versionedId)));
    }

    /**
     * @param lwM2MClient -
     * @param path        -
     * @return - return value of Resource by idPath
     */
    public static LwM2mResource getResourceValueFromLwM2MClient(LwM2mClient lwM2MClient, String path) {
        LwM2mResource lwm2mResourceValue = null;
        ResourceValue resourceValue = lwM2MClient.getResources().get(path);
        if (resourceValue != null) {
            if (new LwM2mPath(fromVersionedIdToObjectId(path)).isResource()) {
                lwm2mResourceValue = lwM2MClient.getResources().get(path).getLwM2mResource();
            }
        }
        return lwm2mResourceValue;
    }

    @SuppressWarnings("unchecked")
    public static Optional<String> contentToString(Object content) {
        try {
            String value = null;
            LwM2mResource resource = null;
            String key = null;
            if (content instanceof Map) {
                Map<Object, Object> contentAsMap = (Map<Object, Object>) content;
                if (contentAsMap.size() == 1) {
                    for (Map.Entry<Object, Object> kv : contentAsMap.entrySet()) {
                        if (kv.getValue() instanceof LwM2mResource) {
                            key = kv.getKey().toString();
                            resource = (LwM2mResource) kv.getValue();
                        }
                    }
                }
            } else if (content instanceof LwM2mResource) {
                resource = (LwM2mResource) content;
            }
            if (resource != null && resource.getType() == OPAQUE) {
                value = opaqueResourceToString(resource, key);
            }
            value = value == null ? content.toString() : value;
            return Optional.of(value);
        } catch (Exception e) {
            log.debug("Failed to convert content " + content + " to string", e);
            return Optional.ofNullable(content != null ? content.toString() : null);
        }
    }

    private static String opaqueResourceToString(LwM2mResource resource, String key) {
        String value = null;
        StringBuilder builder = new StringBuilder();
        if (resource instanceof LwM2mSingleResource) {
            builder.append("LwM2mSingleResource");
            if (key == null) {
                builder.append(" id=").append(String.valueOf(resource.getId()));
            } else {
                builder.append(" key=").append(key);
            }
            builder.append(" value=").append(opaqueToString((byte[]) resource.getValue()));
            builder.append(" type=").append(OPAQUE.toString());
            value = builder.toString();
        } else if (resource instanceof LwM2mMultipleResource) {
            builder.append("LwM2mMultipleResource");
            if (key == null) {
                builder.append(" id=").append(String.valueOf(resource.getId()));
            } else {
                builder.append(" key=").append(key);
            }
            builder.append(" values={");
            if (resource.getInstances().size() > 0) {
                builder.append(multiInstanceOpaqueToString((LwM2mMultipleResource) resource));
            }
            builder.append("}");
            builder.append(" type=").append(OPAQUE.toString());
            value = builder.toString();
        }
        return value;
    }

    private static String multiInstanceOpaqueToString(LwM2mMultipleResource resource) {
        StringBuilder builder = new StringBuilder();
        resource.getInstances().values()
                .forEach(v -> builder.append(" id=").append(v.getId()).append(" value=").append(Hex.encodeHexString((byte[]) v.getValue())).append(", "));
        int startInd = builder.lastIndexOf(", ");
        if (startInd > 0) {
            builder.delete(startInd, startInd + 2);
        }
        return builder.toString();
    }

    private static String opaqueToString(byte[] value) {
        String opaque = Hex.encodeHexString(value);
        return opaque.length() > 1024 ? opaque.substring(0, 1024) : opaque;
    }

    public static LwM2mModel createModelsDefault() {
        return new StaticModel(ObjectLoader.loadDefault());
    }

    public static boolean compareAttNameKeyOta(String attrName) {
        for (OtaPackageKey value : OtaPackageKey.values()) {
            if (attrName.contains(value.getValue())) return true;
        }
        return false;
    }

    public static boolean valueEquals(Object newValue, Object oldValue) {
        String newValueStr;
        String oldValueStr;
        if (oldValue instanceof byte[]) {
            oldValueStr = Hex.encodeHexString((byte[]) oldValue);
        } else {
            oldValueStr = oldValue.toString();
        }
        if (newValue instanceof byte[]) {
            newValueStr = Hex.encodeHexString((byte[]) newValue);
        } else {
            newValueStr = newValue.toString();
        }
        return newValueStr.equals(oldValueStr);
    }

    public static void setDtlsConnectorConfigCidLength(Configuration serverCoapConfig, Integer cIdLength) {
        serverCoapConfig.setTransient(DTLS_CONNECTION_ID_LENGTH);
        serverCoapConfig.setTransient(DTLS_CONNECTION_ID_NODE_ID);
        serverCoapConfig.set(DTLS_CONNECTION_ID_LENGTH, cIdLength);
        if ( cIdLength > 4) {
            serverCoapConfig.set(DTLS_CONNECTION_ID_NODE_ID, 0);
        } else {
            serverCoapConfig.set(DTLS_CONNECTION_ID_NODE_ID, null);
        }
    }
}
