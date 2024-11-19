package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ApiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalApiUtils {
  private final WebClient webClient;

  public ResponseEntity<String> callAPI(ApiRequest request) {
    try {
      // Logging request details
      log.info(
          "Request: method={}, url={}, headers={}, body={}",
          request.method(),
          request.url(),
          request.headers(),
          request.body());

      // Validate HTTP method
      if (request.method() == null) {
        throw new IllegalArgumentException("HTTP method cannot be null.");
      }

      // WebClient call
      Mono<ResponseEntity<String>> responseMono =
          webClient
              .method(request.method())
              .uri(request.url())
              .headers(
                  headers -> {
                    if (request.headers() != null) {
                      headers.addAll(request.headers());
                    }
                  })
              .bodyValue(request.body() != null ? request.body() : "")
              .retrieve()
              .toEntity(String.class);

      // Synchronous block for response
      return responseMono.block();

    } catch (WebClientResponseException e) {
      log.error(
          "HTTP error: status={}, responseBody={}", e.getStatusCode(), e.getResponseBodyAsString());
      return ResponseEntity.status(e.getStatusCode())
          .headers(e.getHeaders())
          .body(e.getResponseBodyAsString());

    } catch (Exception e) {
      log.error("Unexpected error during API call: {}", e.getMessage(), e);
      throw new RuntimeException("API call failed", e);
    }
  }
}