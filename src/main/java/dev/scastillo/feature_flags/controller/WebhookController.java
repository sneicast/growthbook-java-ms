package dev.scastillo.feature_flags.controller;

import dev.scastillo.feature_flags.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook controller for GrowthBook feature updates.
 * Configure this URL in GrowthBook: POST /api/webhooks/growthbook
 * 
 * This is more efficient than polling because GrowthBook only
 * calls this endpoint when features actually change.
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final FeatureFlagService featureFlagService;

    @Value("${growthbook.webhook.secret:}")
    private String webhookSecret;

    /**
     * Webhook endpoint for GrowthBook feature updates.
     * Configure this in GrowthBook Settings > Webhooks.
     * 
     * POST /api/webhooks/growthbook
     */
    @PostMapping("/growthbook")
    public ResponseEntity<String> handleGrowthBookWebhook(
            @RequestHeader(value = "X-GrowthBook-Signature", required = false) String signature,
            @RequestBody(required = false) String payload) {
        
        log.info("Received GrowthBook webhook notification");
        
        // Optional: Validate webhook signature if secret is configured
        if (!webhookSecret.isEmpty() && !validateSignature(signature, payload)) {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }
        
        try {
            featureFlagService.refreshFeatures();
            log.info("Features refreshed successfully via webhook");
            return ResponseEntity.ok("Features refreshed");
        } catch (Exception e) {
            log.error("Failed to refresh features via webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to refresh features");
        }
    }

    /**
     * Simple signature validation (optional security layer).
     * GrowthBook signs webhooks with HMAC-SHA256.
     */
    private boolean validateSignature(String signature, String payload) {
        if (signature == null || signature.isEmpty()) {
            // If no signature provided but secret is configured, reject
            return webhookSecret.isEmpty();
        }
        
        // TODO: Implement HMAC-SHA256 validation if you need it
        // For now, just check if signature header is present
        return true;
    }
}

