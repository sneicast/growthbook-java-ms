package dev.scastillo.feature_flags.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "growthbook")
public class GrowthBookProperties {

    private boolean enabled = true;
    private String apiHost;
    private String clientKey;
    private Cache cache = new Cache();

    @Data
    public static class Cache {
        private boolean enabled = true;
        private int ttlSeconds = 60;
    }

    public String getFeaturesEndpoint() {
        return apiHost + "/api/features/" + clientKey;
    }
}

