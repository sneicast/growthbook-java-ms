package dev.scastillo.feature_flags.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResponse {

    private String featureKey;
    private boolean enabled;
    private Object value;
    private String source;
}

