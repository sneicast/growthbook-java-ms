package dev.scastillo.feature_flags.client;

import com.google.gson.JsonElement;
import dev.scastillo.feature_flags.config.GrowthBookProperties;
import dev.scastillo.feature_flags.exception.FeatureFlagException;
import growthbook.sdk.java.GBContext;
import growthbook.sdk.java.GrowthBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrowthBookClient {

    private final GrowthBook growthBook;
    private final GrowthBookProperties properties;
    private final HttpClient httpClient;

    /**
     * Check if a feature is enabled (on/off)
     */
    public boolean isFeatureEnabled(String featureKey) {
        try {
            log.debug("Checking if feature '{}' is enabled", featureKey);
            return growthBook.isOn(featureKey);
        } catch (Exception e) {
            log.error("Error checking feature '{}': {}", featureKey, e.getMessage());
            throw new FeatureFlagException("Error checking feature: " + featureKey, e);
        }
    }

    /**
     * Get feature value as String with fallback
     */
    public String getFeatureValue(String featureKey, String fallback) {
        try {
            log.debug("Getting string value for feature '{}'", featureKey);
            return growthBook.getFeatureValue(featureKey, fallback);
        } catch (Exception e) {
            log.error("Error getting feature value '{}': {}", featureKey, e.getMessage());
            return fallback;
        }
    }

    /**
     * Get feature value as Integer with fallback
     */
    public Integer getFeatureValueAsInteger(String featureKey, Integer fallback) {
        try {
            log.debug("Getting integer value for feature '{}'", featureKey);
            return growthBook.getFeatureValue(featureKey, fallback);
        } catch (Exception e) {
            log.error("Error getting feature value '{}': {}", featureKey, e.getMessage());
            return fallback;
        }
    }

    /**
     * Get feature value as Boolean with fallback
     */
    public Boolean getFeatureValueAsBoolean(String featureKey, Boolean fallback) {
        try {
            log.debug("Getting boolean value for feature '{}'", featureKey);
            return growthBook.getFeatureValue(featureKey, fallback);
        } catch (Exception e) {
            log.error("Error getting feature value '{}': {}", featureKey, e.getMessage());
            return fallback;
        }
    }

    /**
     * Get feature value as Double with fallback
     */
    public Double getFeatureValueAsDouble(String featureKey, Double fallback) {
        try {
            log.debug("Getting double value for feature '{}'", featureKey);
            return growthBook.getFeatureValue(featureKey, fallback);
        } catch (Exception e) {
            log.error("Error getting feature value '{}': {}", featureKey, e.getMessage());
            return fallback;
        }
    }

    /**
     * Get raw feature value as JsonElement
     */
    public JsonElement getFeatureValueRaw(String featureKey) {
        try {
            log.debug("Getting raw value for feature '{}'", featureKey);
            var result = growthBook.evalFeature(featureKey, Object.class);
            Object value = result.getValue();
            if (value == null) {
                return null;
            }
            return com.google.gson.JsonParser.parseString(
                new com.google.gson.Gson().toJson(value)
            );
        } catch (Exception e) {
            log.error("Error getting raw feature value '{}': {}", featureKey, e.getMessage());
            throw new FeatureFlagException("Error getting raw feature value: " + featureKey, e);
        }
    }

    /**
     * Evaluate feature with custom user attributes
     */
    public boolean isFeatureEnabledForUser(String featureKey, Map<String, Object> userAttributes) {
        try {
            log.debug("Evaluating feature '{}' for user with attributes", featureKey);
            
            // Fetch fresh features
            String featuresJson = fetchFeatures();
            
            // Create new context with user attributes
            GBContext context = GBContext.builder()
                    .featuresJson(featuresJson)
                    .attributesJson(mapToJson(userAttributes))
                    .enabled(properties.isEnabled())
                    .build();
            
            GrowthBook userGrowthBook = new GrowthBook(context);
            boolean result = userGrowthBook.isOn(featureKey);
            userGrowthBook.destroy();
            
            return result;
        } catch (Exception e) {
            log.error("Error evaluating feature '{}' for user: {}", featureKey, e.getMessage());
            throw new FeatureFlagException("Error evaluating feature for user: " + featureKey, e);
        }
    }

    /**
     * Get feature value for specific user with attributes
     */
    @SuppressWarnings("unchecked")
    public <T> T getFeatureValueForUser(String featureKey, T fallback, Class<T> valueType, Map<String, Object> userAttributes) {
        try {
            log.debug("Getting feature value '{}' for user with attributes", featureKey);
            
            // Fetch fresh features
            String featuresJson = fetchFeatures();
            
            // Create new context with user attributes
            GBContext context = GBContext.builder()
                    .featuresJson(featuresJson)
                    .attributesJson(mapToJson(userAttributes))
                    .enabled(properties.isEnabled())
                    .build();
            
            GrowthBook userGrowthBook = new GrowthBook(context);
            var result = userGrowthBook.evalFeature(featureKey, valueType);
            T value = result.getValue() != null ? valueType.cast(result.getValue()) : fallback;
            userGrowthBook.destroy();
            
            return value;
        } catch (Exception e) {
            log.error("Error getting feature value '{}' for user: {}", featureKey, e.getMessage());
            return fallback;
        }
    }

    /**
     * Refresh features from GrowthBook API
     */
    public void refreshFeatures() {
        try {
            log.info("Refreshing GrowthBook features");
            String featuresJson = fetchFeatures();
            growthBook.setFeatures(featuresJson);
            log.info("Successfully refreshed GrowthBook features");
        } catch (Exception e) {
            log.error("Error refreshing features: {}", e.getMessage());
            throw new FeatureFlagException("Error refreshing features", e);
        }
    }

    private String fetchFeatures() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(properties.getFeaturesEndpoint()))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return extractFeatures(response.body());
            }
            throw new FeatureFlagException("Failed to fetch features. Status: " + response.statusCode());
        } catch (FeatureFlagException e) {
            throw e;
        } catch (Exception e) {
            throw new FeatureFlagException("Error fetching features", e);
        }
    }

    /**
     * Extracts the "features" object from the GrowthBook API response.
     */
    private String extractFeatures(String apiResponse) {
        try {
            com.google.gson.JsonObject responseObj = com.google.gson.JsonParser
                    .parseString(apiResponse)
                    .getAsJsonObject();
            if (responseObj.has("features")) {
                return responseObj.get("features").toString();
            }
            return apiResponse;
        } catch (Exception e) {
            log.error("Error parsing features response: {}", e.getMessage());
            return "{}";
        }
    }

    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else if (value == null) {
                json.append("null");
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
        }
        
        json.append("}");
        return json.toString();
    }
}

