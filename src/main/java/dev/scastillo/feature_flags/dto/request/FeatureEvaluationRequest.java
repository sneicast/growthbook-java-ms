package dev.scastillo.feature_flags.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureEvaluationRequest {

    private String userId;
    private String deviceType;
    private String browser;
    private String country;
    private Map<String, Object> customAttributes;
}

