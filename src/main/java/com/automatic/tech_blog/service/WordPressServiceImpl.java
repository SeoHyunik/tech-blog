package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import com.automatic.tech_blog.dto.request.WordPressRequest;
import com.automatic.tech_blog.dto.service.FileInfo;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageInfo;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.automatic.tech_blog.enums.WpCategories;
import com.automatic.tech_blog.utils.ExternalApiUtils;
import com.automatic.tech_blog.utils.FileUtils;
import com.automatic.tech_blog.utils.SecurityUtils;
import com.automatic.tech_blog.utils.WordPressUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordPressServiceImpl implements WordPressService{
  private final ExternalApiUtils apiUtils;
  private final WordPressUtils wordPressUtils;

  @Override
  public Flux<ProcessedDataList> postArticlesToBlog(FileLists fileLists) {
    // 1. Scan the local directory for existing HTML files (Sync Job)
    Set<String> existingHtmlFiles = FileUtils.getExistingHtmlFiles();
    log.info("Existing HTML Files: {}", existingHtmlFiles);

    String password;
    try {
      password = SecurityUtils.decryptAuthFile(SecuritySpecs.KIWIJAM_PW_FILE_PATH.getValue());
    } catch (Exception e) {
      log.error("Error getting WordPress Password: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to get WordPress Password", e);
    }

    // 2. Get the WordPress token
    String token = wordPressUtils.getWordPressToken(password);
    wordPressUtils.validateWordPressToken(token);

    // 3. Get titles already posted to WordPress
    List<String> postedTitles = wordPressUtils.getPublishedArticleTitles(token);

    // 4. Return the Flux of ProcessedDataList
    return Flux.fromIterable(fileLists.fileLists())
        .doOnNext(mdFileInfo -> log.info("Processing File: {}", mdFileInfo.fileName()))
        .filter(mdFileInfo -> {
          // Check if the file is in the existing HTML files
          boolean isInHtmlFiles = existingHtmlFiles.contains(mdFileInfo.fileName().replace(".md", ".html"));
          // Check if the title is already posted to WordPress
          boolean isTitlePosted = postedTitles.contains(mdFileInfo.fileName().replace(".md", ""));

          log.info("File: {} -> in HTML list: {}, already posted: {}",
              mdFileInfo.fileName(), isInHtmlFiles, isTitlePosted);
          // Only process files that are not already posted
          return isInHtmlFiles && !isTitlePosted;
        })
        .switchIfEmpty(Mono.fromRunnable(() -> log.info("No files passed the filter")))
        .flatMap(mdFileInfo -> postArticles(mdFileInfo, token));
  }

  private Mono<ProcessedDataList> postArticles(FileInfo fileInfo, String token) {
    // 1. Create WordPressRequest object
    WordPressRequest request = new WordPressRequest(
        fileInfo.fileName().replace(".md", ""),
        FileUtils.getHtmlContent(fileInfo.fileName().replace(".md", ".html")),
        "open",
        "publish",
        WpCategories.findCategoryId(fileInfo.directory()),
        ""
    );

    // 2. Build HTTP headers
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    // 3. Build API request using the common buildPostApiRequest method
    ExternalApiRequest apiRequest = buildPostApiRequest(
        headers,
        new Gson().toJson(request), // JSON body
        ExternalUrls.WORD_PRESS_KIWIJAM_V2.getUrl() + "/posts"
    );

    log.info("API Request: {}", apiRequest);

    // 4. Call the API and process the response
    return Mono.fromCallable(() -> apiUtils.callAPI(apiRequest))
        .flatMap(response -> {
          if (response == null || response.getBody() == null) {
            log.warn("Response is null for file ID: {}", fileInfo.id());
            return Mono.empty();
          }

          try {
            // 5. Parse the response
            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
            String id = jsonResponse.get("id").getAsString();
            String name = jsonResponse.get("slug").getAsString();
            log.info("Parsed Response -> ID: {}, Name: {}", id, name);
            return Mono.just(new ProcessedDataList(id, name));
          } catch (Exception e) {
            log.error("Error parsing response for file ID: {}", fileInfo.id(), e);
            return Mono.error(new IllegalStateException("Failed to process response"));
          }
        });
  }


  @Override
  public Flux<ProcessedDataList> uploadImages(ImageLists imageLists) {
    // 1. Decrypt the WordPress password
    String password;
    try {
      password = SecurityUtils.decryptAuthFile(SecuritySpecs.KIWIJAM_PW_FILE_PATH.getValue());
    } catch (Exception e) {
      log.error("Error getting WordPress Password: {}", e.getMessage(), e);
      throw new IllegalStateException("Failed to get WordPress Password", e);
    }

    // 2. Get the WordPress token
    String token = wordPressUtils.getWordPressToken(password);
    wordPressUtils.validateWordPressToken(token);

    // 3. Process and upload images
    return Flux.fromIterable(imageLists.imageLists())
        .doOnNext(imageInfo -> log.info("Processing Image: {}", imageInfo.imageName()))
        .flatMap(imageInfo -> uploadImage(imageInfo, token)
            .flatMap(renderedUrl -> {
              log.info("Image uploaded successfully: {}", renderedUrl);

              // 4.Create a ProcessedDataList entry for the uploaded image
              return Mono.just(new ProcessedDataList(
                  imageInfo.id(),
                  renderedUrl
              ));
            })
            .onErrorResume(e -> {
              log.error("Failed to upload image: {}", imageInfo.imageName(), e);
              return Mono.empty(); // Skip failed uploads
            })
        )
        .switchIfEmpty(Mono.fromRunnable(() -> log.info("No images to upload")));
  }

  public Mono<String> uploadImage(ImageInfo imageInfo, String token) {
    try {
      // 1. Read the image file
      File file = new File(imageInfo.imageFilePath());
      if (!file.exists()) {
        log.error("File not found: {}", imageInfo.imageFilePath());
        return Mono.error(new IllegalStateException("File not found: " + imageInfo.imageFilePath()));
      }

      // 2. Build HTTP headers
      HttpHeaders headers = new HttpHeaders();
      headers.add("Authorization", "Bearer " + token);

      // 3. Build API request using the common buildPostApiRequest method
      ExternalApiRequest apiRequest = buildPostApiRequest(
          headers,
          new FileSystemResource(file), // File as the body
          ExternalUrls.WORD_PRESS_KIWIJAM_V2.getUrl() + "/media"
      );

      log.info("Uploading image: {}", file.getName());

      // 4. Call the API
      return Mono.fromCallable(() -> apiUtils.callAPI(apiRequest))
          .flatMap(response -> {
            if (response == null || response.getBody() == null) {
              log.warn("Response is null for file: {}", file.getName());
              return Mono.empty();
            }

            // 5. Parse the response to get `guid.rendered`
            try {
              JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
              if (jsonResponse.has("guid") && jsonResponse.getAsJsonObject("guid").has("rendered")) {
                String renderedUrl = jsonResponse.getAsJsonObject("guid").get("rendered").getAsString();
                log.info("Uploaded Image URL: {}", renderedUrl);
                return Mono.just(renderedUrl);
              } else {
                log.warn("`guid.rendered` not found in response for file: {}", file.getName());
                return Mono.empty();
              }
            } catch (Exception e) {
              log.error("Error parsing response for file: {}", file.getName(), e);
              return Mono.error(new IllegalStateException("Failed to parse response"));
            }
          });
    } catch (Exception e) {
      log.error("Error uploading image: {}", imageInfo.imageFilePath(), e);
      return Mono.error(new IllegalStateException("Failed to upload image", e));
    }
  }

  private ExternalApiRequest buildPostApiRequest(HttpHeaders headers, Object body,String url) {
    // 1. Set default headers if not provided
    if (headers == null)
      headers = new HttpHeaders();

    // 2. Determine Content-Type based on body type
    if (body instanceof FileSystemResource) {
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    } else if (body instanceof String) {
      headers.setContentType(MediaType.APPLICATION_JSON);
    } else {
      throw new IllegalArgumentException("Unsupported body type for API request");
    }

    // 3. Log API request details
    log.info("Building API request: method={}, url={}, headers={}, body={}", HttpMethod.POST, url, headers, body);

    // 4. Return ExternalApiRequest
    return new ExternalApiRequest(
        HttpMethod.POST,
        headers,
        url,
        body
    );
  }

}
