package com.automatic.tech_blog.dto.service;

import java.math.BigDecimal;

public record TokenUsageInfo(String fileId, int inputTokens, int outputTokens, BigDecimal convertedKrw, String model) {}
