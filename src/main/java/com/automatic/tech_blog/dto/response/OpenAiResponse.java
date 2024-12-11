package com.automatic.tech_blog.dto.response;
public record OpenAiResponse(String content, String model, int inputTokens, int outputTokens) {}
