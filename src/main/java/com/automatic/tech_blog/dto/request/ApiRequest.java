package com.automatic.tech_blog.dto.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public record ApiRequest(HttpMethod method, HttpHeaders headers, String url, String body) {
}
