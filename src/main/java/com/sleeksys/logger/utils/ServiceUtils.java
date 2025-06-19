package com.sleeksys.logger.utils;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ServiceUtils {

    /**
     * Map access token from request header authorization.
     * @throws IllegalArgumentException In case of invalid Bearer token.
     * */
    public static String mapAccessToken(@NotNull String authorization) {
        if (!authorization.contains("Bearer ")) {
            throw new IllegalArgumentException("Authorization header must start with 'Bearer '");
        }
        return authorization.replace("Bearer ", "");
    }

    /** Generates a unique tracking ID with a UUID or similar method with length 16. */
    public static String generateTrackingId() {
        return UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();
    }
}
