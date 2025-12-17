package dev.scastillo.feature_flags.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import growthbook.sdk.java.GBContext;
import growthbook.sdk.java.GrowthBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "growthbook.enabled", havingValue = "true", matchIfMissing = true)
public class GrowthBookConfig {

    private final GrowthBookProperties properties;
    private final Gson gson = new Gson();

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Bean
    public String featuresJson(HttpClient httpClient) {
        try {
            String endpoint = properties.getFeaturesEndpoint();
            log.info("Fetching GrowthBook features from: {}", endpoint);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Successfully fetched GrowthBook features");
                // Extract only the "features" object from the response
                String featuresOnly = extractFeatures(response.body());
                log.debug("Features JSON: {}", featuresOnly);
                return featuresOnly;
            } else {
                log.warn("Failed to fetch features. Status: {}", response.statusCode());
                return "{}";
            }
        } catch (Exception e) {
            log.error("Error fetching GrowthBook features: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Extracts the "features" object from the GrowthBook API response.
     * The API returns: {"status": 200, "features": {...}, "dateUpdated": "..."}
     * But the SDK expects only the features object: {...}
     */
    private String extractFeatures(String apiResponse) {
        try {
            JsonObject responseObj = gson.fromJson(apiResponse, JsonObject.class);
            if (responseObj.has("features")) {
                return responseObj.get("features").toString();
            }
            // If no "features" key, assume the response is already the features object
            return apiResponse;
        } catch (Exception e) {
            log.error("Error parsing features response: {}", e.getMessage());
            return "{}";
        }
    }

    @Bean
    public GBContext gbContext(String featuresJson) {
        return GBContext.builder()
                .featuresJson(featuresJson)
                .enabled(properties.isEnabled())
                .build();
    }

    @Bean
    public GrowthBook growthBook(GBContext gbContext) {
        log.info("Creating GrowthBook instance with enabled: {}", properties.isEnabled());
        return new GrowthBook(gbContext);
    }
}

