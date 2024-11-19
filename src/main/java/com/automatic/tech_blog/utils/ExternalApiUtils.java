package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalApiUtils {
  private final WebClient.Builder webClientBuilder;

  public ResponseEntity<String> callAPI(ApiRequest request) {
    try {
      // 1. Validate the API request
      validateRequest(request);

      // 2. Log request details
      logRequest(request);

      // 3. Execute WebClient call
      return webClientBuilder.build()
          .method(request.method())
          .uri(request.url())
          .headers(headers -> {
            if (request.headers() != null) {
              headers.addAll(request.headers());
            }
          })
          .bodyValue(Objects.toString(request.body(), ""))
          .retrieve()
          .toEntity(String.class)
          .block(); // Block to get synchronous response

    } catch (WebClientResponseException e) {
      // Handle HTTP response errors
      logErrorResponse(e);
      return ResponseEntity.status(e.getStatusCode())
          .headers(e.getHeaders())
          .body(e.getResponseBodyAsString());

    } catch (Exception e) {
      // Handle unexpected errors
      log.error("Unexpected error during API call: {}", e.getMessage(), e);
      throw new RuntimeException("API call failed", e);
    }
  }

  private void validateRequest(ApiRequest request) {
    if (request == null)
      throw new IllegalArgumentException("ApiRequest cannot be null.");

    if (request.method() == null)
      throw new IllegalArgumentException("HTTP method cannot be null.");

    if (request.url() == null || request.url().isBlank())
      throw new IllegalArgumentException("URL cannot be null or empty.");
  }

  private void logRequest(ApiRequest request) {
    log.info(
        "API Request: method={}, url={}, headers={}, body={}",
        request.method(),
        request.url(),
        request.headers(),
        request.body()
    );
  }

  private void logErrorResponse(WebClientResponseException e) {
    log.error(
        "HTTP error during API call: status={}, responseBody={}, headers={}",
        e.getStatusCode(),
        e.getResponseBodyAsString(),
        e.getHeaders()
    );
  }
}
