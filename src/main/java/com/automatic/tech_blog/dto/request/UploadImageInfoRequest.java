package com.automatic.tech_blog.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UploadImageInfoRequest(
    @NotNull(message = "Image ID is required")
      String imageId,
    @NotNull(message = "Image URL is required")
    @Pattern(regexp = "^(http|https)://.*", message = "Invalid URL format")
      String imageUrl) {}

