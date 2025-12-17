package dev.scastillo.feature_flags.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureValueResponse<T> {

    private String featureKey;
    private T value;
    private T defaultValue;
    private boolean isDefaultValue;
}

