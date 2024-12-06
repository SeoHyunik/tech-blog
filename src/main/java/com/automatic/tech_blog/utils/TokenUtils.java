package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import com.automatic.tech_blog.dto.response.OpenAiResponse;
import com.automatic.tech_blog.dto.service.TokenUsageInfo;
import com.automatic.tech_blog.enums.InternalPaths;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenUtils {

  private final ExternalApiUtils apiUtils;

  public TokenUsageInfo getTokenUsageInfo(OpenAiResponse openAiResponse, String fileId) {
    // 1. Load the token price policy
    JsonObject tokenPricePolicy = loadTokenPricePolicy();

    // 2. Extract model-specific prices from the policy
    JsonArray modelsArray = tokenPricePolicy.getAsJsonArray("models");
    JsonObject modelPolicy = null;

    // Iterate through the models array to find the matching model
    for (int i = 0; i < modelsArray.size(); i++) {
      JsonObject model = modelsArray.get(i).getAsJsonObject();
      if (model.get("model").getAsString().equals(openAiResponse.model())) {
        modelPolicy = model;
        break;
      }
    }

    if (modelPolicy == null)
      throw new IllegalArgumentException("Model not found in the policy");

    // Extract prices from the model's token price information
    BigDecimal inputPricePer1000Tokens = modelPolicy.getAsJsonObject("input_token_price")
        .get("per_1000_tokens").getAsBigDecimal();
    BigDecimal outputPricePer1000Tokens = modelPolicy.getAsJsonObject("output_token_price")
        .get("per_1000_tokens").getAsBigDecimal();

    // 3. Calculate USD for input and output tokens
    BigDecimal inputUsd = inputPricePer1000Tokens
        .multiply(new BigDecimal(openAiResponse.inputTokens()))
        .divide(new BigDecimal(1000));  // No roundingMode needed here

    BigDecimal outputUsd = outputPricePer1000Tokens
        .multiply(new BigDecimal(openAiResponse.outputTokens()))
        .divide(new BigDecimal(1000));  // No roundingMode needed here

    // 4. Total USD calculation
    BigDecimal totalUsd = inputUsd.add(outputUsd);

    // 5. Convert USD to KRW via external API (dummy conversion in this case)
    BigDecimal convertedKrw = convertUsdToKrw(totalUsd);

    // 6. Return the TokenUsageInfo record
    return new TokenUsageInfo(fileId, openAiResponse.inputTokens(), openAiResponse.outputTokens(), convertedKrw, openAiResponse.model());
  }

  private JsonObject loadTokenPricePolicy() {
    try {
      // 1. Load the JSON file from the given path
      Path path = Paths.get(InternalPaths.OPEN_AI_PRICE_POLICY.getPath());  // JSON 파일 경로 수정 필요
      String jsonString = Files.readString(path);

      // 2. Parse the JSON string to a JsonObject
      return JsonParser.parseString(jsonString).getAsJsonObject();
    } catch (IOException e) {
      log.error("Failed to load token price policy JSON", e);
      throw new IllegalStateException("Failed to load token price policy", e);
    } catch (Exception e) {
      log.error("Failed to parse token price policy JSON", e);
      throw new IllegalStateException("Invalid JSON format in token price policy", e);
    }
  }

  private BigDecimal convertUsdToKrw(BigDecimal usd) {
    try {
      // 1. Build the ExternalApiRequest to call the exchange rate API
      String url = "https://v6.exchangerate-api.com/v6/b459b3ea4a98482e611335ce/latest/USD";  // Your API key here

      ExternalApiRequest request =
          new ExternalApiRequest(
              HttpMethod.GET, // Using GET method for API call
              new HttpHeaders(), // Empty headers or add your headers if needed
              url,
              null // No request body
              );

      // 2. Call the API using the existing callAPI method
      ResponseEntity<String> response = apiUtils.callAPI(request);
      if(response == null || response.getBody() == null)
        throw new IllegalStateException("Exchange rate API response is null");

      // 3. Parse the response body to get the KRW conversion rate
      JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
      BigDecimal exchangeRate = jsonResponse.getAsJsonObject("conversion_rates").get("KRW").getAsBigDecimal();

      // 4. Convert USD to KRW
      return usd.multiply(exchangeRate);
    } catch (Exception e) {
      log.error("Error calling the exchange rate API: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to retrieve exchange rate", e);
    }
  }
}
