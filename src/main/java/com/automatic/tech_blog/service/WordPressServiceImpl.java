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
import com.automatic.tech_blog.utils.WordPressUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordPressServiceImpl implements WordPressService{
  private final ExternalApiUtils apiUtils;
  private final WordPressUtils wordPressUtils;
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
    String token = wordPressUtils.getWordPressToken(password);
    wordPressUtils.validateWordPressToken(token);

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

          // 3. Parse response to extract id and slug (name)
          try {
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
        ExternalUrls.WORD_PRESS_KIWIJAM_V2.getUrl() + "/posts",
        body
    );
  }
}
