package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import com.automatic.tech_blog.dto.response.OpenAiResponse;
import com.automatic.tech_blog.dto.service.TokenUsageInfo;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.automatic.tech_blog.enums.InternalPaths;
import com.automatic.tech_blog.enums.SecuritySpecs;
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
import java.math.*;
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
    JsonArray tokenPricePolicy = loadTokenPricePolicy();

    // 2. Extract model-specific prices from the policy
    JsonObject modelPolicy = null;

    log.info("openAiResponse.model() : {}", openAiResponse.model());

    // Iterate through the models array to find the matching model
    for (int i = 0; i < tokenPricePolicy.size(); i++) {
      JsonObject model = tokenPricePolicy.get(i).getAsJsonObject();
      log.info(model.toString());
      if (openAiResponse.model().contains(model.get("model").getAsString().toLowerCase())) {
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
    MathContext mc = new MathContext(4);
    BigDecimal inputUsd =
        inputPricePer1000Tokens
            .multiply(new BigDecimal(openAiResponse.inputTokens()))
            .divide(new BigDecimal(1000), mc);

    BigDecimal outputUsd =
        outputPricePer1000Tokens
            .multiply(new BigDecimal(openAiResponse.outputTokens()))
            .divide(new BigDecimal(1000), mc);

    // 4. Total USD calculation
    BigDecimal totalUsd = inputUsd.add(outputUsd);
    log.info("Input USD: {}, Output USD: {}, Total USD: {}", inputUsd, outputUsd, totalUsd);

    // 5. Convert USD to KRW via external API (real-time conversion)
    BigDecimal convertedKrw = convertUsdToKrw(totalUsd);
    log.info("Converted KRW: {}", convertedKrw);

    // 6. Return the TokenUsageInfo record
    return new TokenUsageInfo(fileId, openAiResponse.inputTokens(), openAiResponse.outputTokens(), convertedKrw, openAiResponse.model());
  }

  private JsonArray loadTokenPricePolicy() {
    try {
      // 1. Load the JSON file from the given path
      Path path = Paths.get(InternalPaths.OPEN_AI_PRICE_POLICY.getPath());
      String jsonString = Files.readString(path);

      // 2. Parse the JSON string to a JsonArray
      return JsonParser.parseString(jsonString).getAsJsonArray();
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
      // 1. Get the Exchange-rate API Key
      String apiKey;
      try {
        apiKey = SecurityUtils.decryptAuthFile(SecuritySpecs.EXCHANGE_RATE_API_KEY_FILE_PATH.getValue());
      } catch (Exception e) {
        log.error("Error getting Exchange-rate API Key: {}", e.getMessage(), e);
        throw new IllegalStateException("Failed to get Exchange-rate API Key", e);
      }
      // 2. Build the ExternalApiRequest to call the exchange rate API
      String url = ExternalUrls.EXCHANGE_RATE_URI.getUrl().replace("{API_KEY}", apiKey);

      ExternalApiRequest request =
          new ExternalApiRequest(
              HttpMethod.GET,
              new HttpHeaders(),
              url,
              null
          );

      // 3. Call the API using the existing callAPI method
      ResponseEntity<String> response = apiUtils.callAPI(request);
      if(response == null || response.getBody() == null)
        throw new IllegalStateException("Exchange rate API response is null");

      // 4. Parse the response body to get the KRW conversion rate
      JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
      BigDecimal exchangeRate = jsonResponse.getAsJsonObject("conversion_rates").get("KRW").getAsBigDecimal();
      log.info("Exchange rate from USD to KRW: {}", exchangeRate);

      // 5. Convert USD to KRW
      return usd.multiply(exchangeRate);
    } catch (Exception e) {
      log.error("Error calling the exchange rate API: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to retrieve exchange rate", e);
    }
  }
}
