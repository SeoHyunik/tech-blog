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

  /**
   * Sends an API request using WebClient and handles the response.
   *
   * @param request The API request details.
   * @return The response as a ResponseEntity<String>.
   */
  public ResponseEntity<String> callAPI(ApiRequest request) {
    validateRequest(request);

    try {
      // Log request details
      logRequest(request);

      // Execute WebClient call
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

  /**
   * Validates the API request.
   *
   * @param request The API request to validate.
   */
  private void validateRequest(ApiRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("ApiRequest cannot be null.");
    }
    if (request.method() == null) {
      throw new IllegalArgumentException("HTTP method cannot be null.");
    }
    if (request.url() == null || request.url().isBlank()) {
      throw new IllegalArgumentException("URL cannot be null or empty.");
    }
  }

  /**
   * Logs the API request details.
   *
   * @param request The API request to log.
   */
  private void logRequest(ApiRequest request) {
    log.info(
        "API Request: method={}, url={}, headers={}, body={}",
        request.method(),
        request.url(),
        request.headers(),
        request.body()
    );
  }

  /**
   * Logs the error details for WebClientResponseException.
   *
   * @param e The WebClientResponseException instance.
   */
  private void logErrorResponse(WebClientResponseException e) {
    log.error(
        "HTTP error during API call: status={}, responseBody={}, headers={}",
        e.getStatusCode(),
        e.getResponseBodyAsString(),
        e.getHeaders()
    );
  }
}
