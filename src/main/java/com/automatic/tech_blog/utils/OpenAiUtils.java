package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ApiRequest;
import com.automatic.tech_blog.dto.request.OpenAiRequest;
import com.automatic.tech_blog.dto.response.OpenAiResponse;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
public class OpenAiUtils {

  private final ExternalApiUtils apiUtils; // Utility class for API calls

  public OpenAiResponse generateHtmlFromMarkdown(OpenAiRequest openAiRequest) {
    try {
      // 1. Build API request
      ApiRequest apiRequest = buildApiRequest(openAiRequest);

      // 2. Call OpenAI API and get the response
      ResponseEntity<String> response = apiUtils.callAPI(apiRequest);

      // 3. Validate response
      if (response.getBody() == null) {
        throw new IllegalStateException("API response body is null.");
      }

      // 4. Parse the response body
      JsonObject jsonObject = JsonParser.parseString(response.getBody()).getAsJsonObject();

      // 5. Extract relevant fields
      String id = jsonObject.has("id") ? jsonObject.get("id").getAsString() : null;
      JsonArray choices = jsonObject.getAsJsonArray("choices");
      if (choices == null || choices.size() == 0) {
        throw new IllegalStateException("API response does not contain choices.");
      }
      String content = choices
          .get(0)
          .getAsJsonObject()
          .getAsJsonObject("message")
          .get("content")
          .getAsString();
      int tokenUsage = jsonObject.has("usage")
          ? jsonObject.getAsJsonObject("usage").get("total_tokens").getAsInt()
          : 0;

      // Return the parsed response as OpenAiResponse
      return new OpenAiResponse(content, tokenUsage);

    } catch (JsonParseException e) {
      log.error("Error parsing JSON response: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to parse API response JSON", e);
    } catch (Exception e) {
      log.error("Error generating HTML from Markdown: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to generate HTML from Markdown", e);
    }
  }

  private ApiRequest buildApiRequest(OpenAiRequest openAiRequest) {
    // Set HTTP headers
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", openAiRequest.api_key());
    headers.add("Content-Type", "application/json");

    // Create and return ApiRequest
    return new ApiRequest(
        HttpMethod.POST,
        headers,
        ExternalUrls.OPEN_AI_COMPLETION_URI.getUrl(),
        openAiRequest.prompt());
  }


}
