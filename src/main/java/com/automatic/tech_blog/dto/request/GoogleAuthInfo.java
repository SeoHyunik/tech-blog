package com.automatic.tech_blog.dto.request;

import jakarta.validation.constraints.NotNull;

public record GoogleAuthInfo(@NotNull(message = "Google Client ID is required") String clientId, @NotNull(message = "Google Client Secret is required") String clientSecret) {
}
