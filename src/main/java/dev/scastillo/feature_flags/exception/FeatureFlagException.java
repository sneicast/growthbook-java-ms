package dev.scastillo.feature_flags.exception;

public class FeatureFlagException extends RuntimeException {

    public FeatureFlagException(String message) {
        super(message);
    }

    public FeatureFlagException(String message, Throwable cause) {
        super(message, cause);
    }
}

