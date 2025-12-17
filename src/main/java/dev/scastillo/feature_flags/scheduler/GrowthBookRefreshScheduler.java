package dev.scastillo.feature_flags.scheduler;

import dev.scastillo.feature_flags.client.GrowthBookClient;
import dev.scastillo.feature_flags.config.GrowthBookProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that automatically refreshes GrowthBook features
 * based on the configured TTL (cache.ttl-seconds).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "growthbook.cache.enabled", havingValue = "true", matchIfMissing = true)
public class GrowthBookRefreshScheduler {

    private final GrowthBookClient growthBookClient;
    private final GrowthBookProperties properties;

    /**
     * Refreshes features automatically.
     * The fixed delay is calculated from growthbook.cache.ttl-seconds (in milliseconds).
     * Default: 60 seconds.
     */
    @Scheduled(fixedDelayString = "${growthbook.cache.ttl-seconds:60}000")
    public void refreshFeatures() {
        if (!properties.isEnabled()) {
            log.debug("GrowthBook is disabled, skipping feature refresh");
            return;
        }

        try {
            log.debug("Auto-refreshing GrowthBook features (TTL: {}s)", properties.getCache().getTtlSeconds());
            growthBookClient.refreshFeatures();
        } catch (Exception e) {
            log.warn("Failed to auto-refresh GrowthBook features: {}", e.getMessage());
            // Don't throw - we don't want to break the scheduler
        }
    }
}

