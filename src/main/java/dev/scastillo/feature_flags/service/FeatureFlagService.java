package dev.scastillo.feature_flags.service;

import dev.scastillo.feature_flags.client.GrowthBookClient;
import dev.scastillo.feature_flags.dto.request.FeatureEvaluationRequest;
import dev.scastillo.feature_flags.dto.response.FeatureResponse;
import dev.scastillo.feature_flags.dto.response.FeatureValueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final GrowthBookClient growthBookClient;

    /**
     * Get feature status (enabled/disabled)
     */
    public FeatureResponse getFeature(String featureKey) {
        log.info("Getting feature: {}", featureKey);
        
        boolean isEnabled = growthBookClient.isFeatureEnabled(featureKey);
        Object value = null;
        
        try {
            var rawValue = growthBookClient.getFeatureValueRaw(featureKey);
            if (rawValue != null && !rawValue.isJsonNull()) {
                value = parseJsonElement(rawValue);
            }
        } catch (Exception e) {
            log.debug("Could not get raw value for feature: {}", featureKey);
        }
        
        return FeatureResponse.builder()
                .featureKey(featureKey)
                .enabled(isEnabled)
                .value(value)
                .source("growthbook")
                .build();
    }

    /**
     * Evaluate feature with user attributes
     */
    public FeatureResponse evaluateFeature(String featureKey, FeatureEvaluationRequest request) {
        log.info("Evaluating feature '{}' with user attributes", featureKey);
        
        Map<String, Object> attributes = buildAttributes(request);
        boolean isEnabled = growthBookClient.isFeatureEnabledForUser(featureKey, attributes);
        
        return FeatureResponse.builder()
                .featureKey(featureKey)
                .enabled(isEnabled)
                .value(isEnabled)
                .source("growthbook")
                .build();
    }

    /**
     * Get string feature value
     */
    public FeatureValueResponse<String> getStringValue(String featureKey, String defaultValue) {
        log.info("Getting string value for feature: {}", featureKey);
        
        String value = growthBookClient.getFeatureValue(featureKey, defaultValue);
        boolean isDefault = value.equals(defaultValue);
        
        return FeatureValueResponse.<String>builder()
                .featureKey(featureKey)
                .value(value)
                .defaultValue(defaultValue)
                .isDefaultValue(isDefault)
                .build();
    }

    /**
     * Get boolean feature value
     */
    public FeatureValueResponse<Boolean> getBooleanValue(String featureKey, Boolean defaultValue) {
        log.info("Getting boolean value for feature: {}", featureKey);
        
        Boolean value = growthBookClient.getFeatureValueAsBoolean(featureKey, defaultValue);
        boolean isDefault = value.equals(defaultValue);
        
        return FeatureValueResponse.<Boolean>builder()
                .featureKey(featureKey)
                .value(value)
                .defaultValue(defaultValue)
                .isDefaultValue(isDefault)
                .build();
    }

    /**
     * Get integer feature value
     */
    public FeatureValueResponse<Integer> getIntegerValue(String featureKey, Integer defaultValue) {
        log.info("Getting integer value for feature: {}", featureKey);
        
        Integer value = growthBookClient.getFeatureValueAsInteger(featureKey, defaultValue);
        boolean isDefault = value.equals(defaultValue);
        
        return FeatureValueResponse.<Integer>builder()
                .featureKey(featureKey)
                .value(value)
                .defaultValue(defaultValue)
                .isDefaultValue(isDefault)
                .build();
    }

    /**
     * Get double feature value
     */
    public FeatureValueResponse<Double> getDoubleValue(String featureKey, Double defaultValue) {
        log.info("Getting double value for feature: {}", featureKey);
        
        Double value = growthBookClient.getFeatureValueAsDouble(featureKey, defaultValue);
        boolean isDefault = value.equals(defaultValue);
        
        return FeatureValueResponse.<Double>builder()
                .featureKey(featureKey)
                .value(value)
                .defaultValue(defaultValue)
                .isDefaultValue(isDefault)
                .build();
    }

    /**
     * Refresh features from GrowthBook
     */
    public void refreshFeatures() {
        log.info("Refreshing features from GrowthBook");
        growthBookClient.refreshFeatures();
    }

    private Map<String, Object> buildAttributes(FeatureEvaluationRequest request) {
        Map<String, Object> attributes = new HashMap<>();
        
        if (request.getUserId() != null) {
            attributes.put("id", request.getUserId());
        }
        if (request.getDeviceType() != null) {
            attributes.put("deviceType", request.getDeviceType());
        }
        if (request.getBrowser() != null) {
            attributes.put("browser", request.getBrowser());
        }
        if (request.getCountry() != null) {
            attributes.put("country", request.getCountry());
        }
        if (request.getCustomAttributes() != null) {
            attributes.putAll(request.getCustomAttributes());
        }
        
        return attributes;
    }

    private Object parseJsonElement(com.google.gson.JsonElement element) {
        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber()) {
                return primitive.getAsNumber();
            } else {
                return primitive.getAsString();
            }
        } else if (element.isJsonObject()) {
            return element.getAsJsonObject().toString();
        } else if (element.isJsonArray()) {
            return element.getAsJsonArray().toString();
        }
        return null;
    }
}

