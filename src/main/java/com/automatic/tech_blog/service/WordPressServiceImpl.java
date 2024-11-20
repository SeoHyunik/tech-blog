package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import com.automatic.tech_blog.dto.request.WordPressRequest;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.automatic.tech_blog.utils.ExternalApiUtils;
import com.automatic.tech_blog.utils.FileUtils;
import com.automatic.tech_blog.utils.SecurityUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordPressServiceImpl implements WordPressService{

  private final ExternalApiUtils apiUtils;

  private final String USER_NAME = "hos0917@gmail.com";
  private String password;

  @Override
  public Flux<ProcessedDataList> postArticlesToBlog(MdFileLists mdFileLists) {
    // 1. Scan the local directory for existing HTML files (Sync Job)
    Set<String> existingHtmlFiles = FileUtils.getExistingHtmlFiles();
    try {
      password = SecurityUtils.decryptAuthFile(SecuritySpecs.KIWIJAM_PW_FILE_PATH.getValue());
    } catch (Exception e) {
      log.error("Error getting WordPress Password: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to get WordPress Password", e);
    }

    // 2. Get the WordPress token
    String token = getWordPressToken(password);

    // 3. Return the Flux of ProcessedDataList
    return Flux.fromIterable(mdFileLists.mdFileLists())
        .filter(mdFileInfo -> !existingHtmlFiles.contains(mdFileInfo.fileName().replace(".md", ".html")))
        .flatMap(mdFileInfo -> postArticles(mdFileInfo, token));
  }

  private Mono<ProcessedDataList> postArticles(MdFileInfo mdFileInfo, String token) {
    // 1. Make API request to WordPress
    ExternalApiRequest apiRequest = buildApiRequest(mdFileInfo, token);

    // 2. Call the API and process the response
    return Mono.fromCallable(() -> apiUtils.callAPI(apiRequest))
        .flatMap(response -> {
          if (response == null || response.getBody() == null) {
            log.warn("Response is null for file ID: {}", mdFileInfo.id());
            return Mono.empty();
          }

          try {
            // 3. Parse response to extract id and slug (name)
            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
            String id = jsonResponse.get("id").getAsString();
            String name = jsonResponse.get("slug").getAsString();

            return Mono.just(new ProcessedDataList(id, name));
          } catch (Exception e) {
            log.error("Error parsing response for file ID: {}", mdFileInfo.id(), e);
            return Mono.error(new IllegalStateException("Failed to process response"));
          }
        });
  }

  private String getWordPressToken(String password) {
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
          null // No body for GET requests
      );

      // 3. Call the API and parse the response to get jwt_token
      ResponseEntity<String> tokenResponse = apiUtils.callAPI(tokenRequest);
      if(tokenResponse == null || tokenResponse.getBody() == null)
        throw new IllegalStateException("Token response is null");

      JsonObject tokenJson = JsonParser.parseString(tokenResponse.getBody()).getAsJsonObject();
      if (!tokenJson.has("jwt_token"))
        throw new IllegalStateException("Token not found in response");

      String jwtToken = tokenJson.get("jwt_token").getAsString();

      // 4. Validate the token
      validateWordPressToken(jwtToken);

      // 5. Return the validated token
      return jwtToken;
    } catch (Exception e) {
      log.error("Failed to retrieve or validate WordPress token", e);
      throw new IllegalStateException("Error while retrieving WordPress token", e);
    }
  }

  private void validateWordPressToken(String jwtToken) {
    // 1. Build WordPress API request for token validation
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + jwtToken);

    ExternalApiRequest validateRequest = new ExternalApiRequest(
        HttpMethod.GET,
        headers,
        ExternalUrls.WORD_PRESS_KIWIJAM_V1.getUrl() + "/token-validate",
        null // No body for GET request
    );

    // 2. Call the API and validate the response
    ResponseEntity<String>  validateResponse = apiUtils.callAPI(validateRequest);
    if(validateResponse == null || validateResponse.getBody() == null)
      throw new IllegalStateException("Token validation response is null");

    JsonObject validateJson = JsonParser.parseString(validateResponse.getBody()).getAsJsonObject();
    if (!"TRUE".equals(validateJson.get("status").getAsString()) ||
        !"VALID_TOKEN".equals(validateJson.get("message").getAsString())) {
      throw new IllegalStateException("Invalid WordPress token");
    }

    log.info("WordPress token validated successfully");
  }

  private ExternalApiRequest buildApiRequest(MdFileInfo mdFileInfo, String token) {
    // 1. Set HTTP headers
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);
    headers.add("Content-Type", "application/json");

    // 2. Create WordPressRequest object
    WordPressRequest request = new WordPressRequest(
        mdFileInfo.directory(),
        mdFileInfo.id(),
        "open",
        "publish",
        "3",
        "1"
    );

    // 3. Convert WordPressRequest to JSON using Gson
    Gson gson = new Gson();
    String body = gson.toJson(request);

    // 4. Return ApiRequest
    return new ExternalApiRequest(
        HttpMethod.POST,
        headers,
        ExternalUrls.OPEN_AI_COMPLETION_URI.getUrl(),
        body
    );
  }

}
