package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordPressUtils {
  private final ExternalApiUtils apiUtils;
  private static final String USER_NAME = "hos0917@gmail.com";

  public String getWordPressToken(String password) {
    try {
      // 1. Build URL with query string
      String urlWithQuery = String.format(
          "%s/token?username=%s&password=%s",
          ExternalUrls.WORD_PRESS_KIWIJAM_V1.getUrl(),
          USER_NAME,
          password
      );

      // 2. Build WordPress API request with query string
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");

      ExternalApiRequest tokenRequest = new ExternalApiRequest(
          HttpMethod.POST,
          headers,
          urlWithQuery,
          null
      );

      // 3. Call the API and parse the response to get jwt_token
      ResponseEntity<String> tokenResponse = apiUtils.callAPI(tokenRequest);
      if(tokenResponse == null || tokenResponse.getBody() == null)
        throw new IllegalStateException("Token response is null");

      // 4. Parse the response and check if token is present
      JsonObject tokenJson = JsonParser.parseString(tokenResponse.getBody()).getAsJsonObject();
      if (!tokenJson.has("jwt_token"))
        throw new IllegalStateException("Token not found in response");

      // 5. Return the token
      return tokenJson.get("jwt_token").getAsString();
    } catch (Exception e) {
      log.error("Failed to retrieve or validate WordPress token", e);
      throw new IllegalStateException("Error while retrieving WordPress token", e);
    }
  }

  public void validateWordPressToken(String jwtToken) {
    // 1. Build WordPress API request for token validation
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtToken);

    ExternalApiRequest validateRequest = new ExternalApiRequest(
        HttpMethod.GET,
        headers,
        ExternalUrls.WORD_PRESS_KIWIJAM_V1.getUrl() + "/token-validate",
        null
    );

    // 2. Call the API and validate the response
    ResponseEntity<String>  validateResponse = apiUtils.callAPI(validateRequest);
    if(validateResponse == null || validateResponse.getBody() == null)
      throw new IllegalStateException("Token validation response is null");

    // 3. Parse the response and check status and message
    JsonObject validateJson = JsonParser.parseString(validateResponse.getBody()).getAsJsonObject();
    if (!"TRUE".equals(validateJson.get("status").getAsString()) ||
        !"VALID_TOKEN".equals(validateJson.get("message").getAsString()))
      throw new IllegalStateException("Invalid WordPress token");

    log.info("WordPress token validated successfully");
  }
}
