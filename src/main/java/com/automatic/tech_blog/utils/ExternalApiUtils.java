package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalApiUtils {
  private final WebClient.Builder webClientBuilder;

  public ResponseEntity<String> callAPI(ExternalApiRequest request) {
    try {
      // 1. Validate the API request
      validateRequest(request);

      // 2. Log request details
      logRequest(request);

      // 3. Check if the request body is a file
      if (isFileUpload(request.body()))
        return handleFileUpload(request);

      // 4. Execute WebClient call for standard API requests
      return webClientBuilder.build()
          .method(request.method())
          .uri(request.url())
          .headers(headers -> {
            if (request.headers() != null) {
              headers.addAll(request.headers());
            }
          })
          .bodyValue(request.body() != null ? request.body() : "")
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

  private boolean isFileUpload(Object body) {
    return body instanceof File || body instanceof FileSystemResource;
  }

  private ResponseEntity<String> handleFileUpload(ExternalApiRequest request) {
    try {
      // 1. Prepare MultiValueMap for multipart/form-data
      MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
      if (request.body() instanceof File file) {
        formData.add("file", new FileSystemResource(file));
      } else if (request.body() instanceof FileSystemResource fileResource) {
        formData.add("file", fileResource);
      } else {
        throw new IllegalArgumentException("Invalid file type for upload.");
      }

      // 2. Build WebClient for file upload
      return webClientBuilder.build()
          .method(request.method())
          .uri(request.url())
          .headers(headers -> {
            if (request.headers() != null) {
              headers.addAll(request.headers());
            }
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
          })
          .bodyValue(formData)
          .retrieve()
          .toEntity(String.class)
          .block(); // Block to get synchronous response

    } catch (Exception e) {
      log.error("Error during file upload: {}", e.getMessage(), e);
      throw new RuntimeException("File upload failed", e);
    }
  }

  private void validateRequest(ExternalApiRequest request) {
    if (request == null)
      throw new IllegalArgumentException("ApiRequest cannot be null.");

    if (request.method() == null)
      throw new IllegalArgumentException("HTTP method cannot be null.");

    if (request.url() == null || request.url().isBlank())
      throw new IllegalArgumentException("URL cannot be null or empty.");
  }

  private void logRequest(ExternalApiRequest request) {
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
