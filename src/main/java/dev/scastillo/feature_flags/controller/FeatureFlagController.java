package dev.scastillo.feature_flags.controller;

import dev.scastillo.feature_flags.dto.request.FeatureEvaluationRequest;
import dev.scastillo.feature_flags.dto.response.FeatureResponse;
import dev.scastillo.feature_flags.dto.response.FeatureValueResponse;
import dev.scastillo.feature_flags.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    /**
     * Get feature status by key
     * GET /api/features/{featureKey}
     */
    @GetMapping("/{featureKey}")
    public ResponseEntity<FeatureResponse> getFeature(@PathVariable String featureKey) {
        log.debug("GET /api/features/{}", featureKey);
        FeatureResponse response = featureFlagService.getFeature(featureKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Evaluate feature with user attributes
     * POST /api/features/{featureKey}/evaluate
     */
    @PostMapping("/{featureKey}/evaluate")
    public ResponseEntity<FeatureResponse> evaluateFeature(
            @PathVariable String featureKey,
            @RequestBody FeatureEvaluationRequest request) {
        log.debug("POST /api/features/{}/evaluate", featureKey);
        FeatureResponse response = featureFlagService.evaluateFeature(featureKey, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get string value for a feature
     * GET /api/features/{featureKey}/string?default=value
     */
    @GetMapping("/{featureKey}/string")
    public ResponseEntity<FeatureValueResponse<String>> getStringValue(
            @PathVariable String featureKey,
            @RequestParam(defaultValue = "") String defaultValue) {
        log.debug("GET /api/features/{}/string", featureKey);
        FeatureValueResponse<String> response = featureFlagService.getStringValue(featureKey, defaultValue);
        return ResponseEntity.ok(response);
    }

    /**
     * Get boolean value for a feature
     * GET /api/features/{featureKey}/boolean?default=false
     */
    @GetMapping("/{featureKey}/boolean")
    public ResponseEntity<FeatureValueResponse<Boolean>> getBooleanValue(
            @PathVariable String featureKey,
            @RequestParam(defaultValue = "false") Boolean defaultValue) {
        log.debug("GET /api/features/{}/boolean", featureKey);
        FeatureValueResponse<Boolean> response = featureFlagService.getBooleanValue(featureKey, defaultValue);
        return ResponseEntity.ok(response);
    }

    /**
     * Get integer value for a feature
     * GET /api/features/{featureKey}/integer?default=0
     */
    @GetMapping("/{featureKey}/integer")
    public ResponseEntity<FeatureValueResponse<Integer>> getIntegerValue(
            @PathVariable String featureKey,
            @RequestParam(defaultValue = "0") Integer defaultValue) {
        log.debug("GET /api/features/{}/integer", featureKey);
        FeatureValueResponse<Integer> response = featureFlagService.getIntegerValue(featureKey, defaultValue);
        return ResponseEntity.ok(response);
    }

    /**
     * Get double value for a feature
     * GET /api/features/{featureKey}/double?default=0.0
     */
    @GetMapping("/{featureKey}/double")
    public ResponseEntity<FeatureValueResponse<Double>> getDoubleValue(
            @PathVariable String featureKey,
            @RequestParam(defaultValue = "0.0") Double defaultValue) {
        log.debug("GET /api/features/{}/double", featureKey);
        FeatureValueResponse<Double> response = featureFlagService.getDoubleValue(featureKey, defaultValue);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh features from GrowthBook
     * POST /api/features/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshFeatures() {
        log.debug("POST /api/features/refresh");
        featureFlagService.refreshFeatures();
        return ResponseEntity.ok().build();
    }
}

