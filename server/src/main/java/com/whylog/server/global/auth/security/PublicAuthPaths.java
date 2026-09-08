package com.whylog.server.global.auth.security;

import java.util.Set;

public final class PublicAuthPaths {

    private static final Set<String> PATHS =
            Set.of(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/refresh-token",
                    "/api/auth/email-verifications",
                    "/api/auth/email-verifications/verify",
                    "/api/auth/withdrawal-recoveries/verify");

    private PublicAuthPaths() {}

    public static boolean contains(String path) {
        return PATHS.contains(path);
    }

    public static String[] asArray() {
        return PATHS.toArray(String[]::new);
    }
}
