package com.automatic.tech_blog.config;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import com.automatic.tech_blog.dto.request.UploadImageInfoRequest;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.enums.InternalUrls;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.automatic.tech_blog.utils.ExternalApiUtils;
import com.automatic.tech_blog.utils.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final ExternalApiUtils externalApiUtils;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private final String LOCALHOST = "http://localhost:8080";

  @Bean
  public Job techBlogJob() {
    return new JobBuilder("techBlogJob", jobRepository)
        .start(step1_scanFilesAndInsertIntoDb())
        .next(step2_uploadImagesToWordPressLibrary())
        .build();
  }

  @Bean
  public Step step1_scanFilesAndInsertIntoDb() {
    return new StepBuilder("step1_scanFilesAndInsertIntoDb", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              // 1. Call scanFiles API
              FileLists fileLists = scanFiles();

              log.info("Retrieved fileLists from scanFiles: {}", fileLists);

              // 2. Insert MD files into DB
              if(!insertMdFilesIntoDb(fileLists)) throw new RuntimeException("Failed to insert MD files into DB");

              // 3. Insert Pasted Images into DB
              if(!insertPastedImagesIntoDb(fileLists)) throw new RuntimeException("Failed to insert Pasted Images into DB");

              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }

  private FileLists scanFiles(){
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.add(
          "Authorization",
          SecurityUtils.decryptAuthFile(SecuritySpecs.GOOGLE_AUTH_FILE_PATH.getValue()));
      HttpMethod method = InternalUrls.SCAN_FILES.getMethod();
      String apiUrl = LOCALHOST + InternalUrls.SCAN_FILES.getUrl();

      ApiResponse response = callApi(headers, method, apiUrl, null);
      log.info("Response from scanFiles: {}", limitLogLength(response.toString()));

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());

      String dataJson = response.data().toString();
      JsonNode rootNode = objectMapper.readTree(dataJson);
      String fileListsJson = rootNode.get("data").toString();

      return objectMapper.readValue(fileListsJson, FileLists.class);
    } catch (IOException e) {
      log.error("Error occurred while parsing JSON response", e);
      throw new RuntimeException("Failed to parse FileLists from response", e);
    } catch (Exception e) {
      log.error("Unexpected error occurred", e);
      throw new RuntimeException("An unexpected error occurred", e);
    }
  }

  private boolean insertMdFilesIntoDb(FileLists fileLists) {
    HttpHeaders headers = new HttpHeaders();
    HttpMethod method = InternalUrls.UPLOAD_FILES.getMethod();
    String apiUrl = LOCALHOST + InternalUrls.UPLOAD_FILES.getUrl();

    ApiResponse response = callApi(headers, method, apiUrl, fileLists);
    log.info("Response from insertMdFilesIntoDb: {}", response);
    return response.isSuccess();
  }

  private boolean insertPastedImagesIntoDb(FileLists fileLists) {
    HttpHeaders headers = new HttpHeaders();
    HttpMethod method = InternalUrls.UPLOAD_PASTED_IMAGES.getMethod();
    String apiUrl = LOCALHOST + InternalUrls.UPLOAD_PASTED_IMAGES.getUrl();

    ApiResponse response = callApi(headers, method, apiUrl, fileLists);
    return response.isSuccess();
  }


  @Bean
  public Step step2_uploadImagesToWordPressLibrary() {
    return new StepBuilder("step2_uploadImagesToWordPressLibrary", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          // 1. Call getNewImages API
          ImageLists imageLists = getNewImages();
          log.info("Retrieved imageLists from getNewImages: {}", imageLists);

          // 2. Upload images to WordPress
          Mono<ApiResponse> uploadImagesResponse = uploadImagesToWordPress(imageLists);

          // Check ApiResponse's isSuccess is true
          uploadImagesResponse.doOnNext(apiResponse -> {
            if (!apiResponse.isSuccess()) {
              throw new RuntimeException("Failed to upload images to WordPress");
            }
          }).block();

          // 3. Update image info in DB
          updateImageInfoFromDb(uploadImagesResponse);

          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }


  private ImageLists getNewImages() {
    try {
      HttpHeaders headers = new HttpHeaders();
      HttpMethod method = InternalUrls.GET_NEW_IMAGES.getMethod();
      String apiUrl = LOCALHOST + InternalUrls.GET_NEW_IMAGES.getUrl();

      ApiResponse response = callApi(headers, method, apiUrl, null);
      log.info("Response from getNewImages: {}", limitLogLength(response.toString()));

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());

      String dataJson = response.data().toString();
      JsonNode rootNode = objectMapper.readTree(dataJson);
      String imageListsJson = rootNode.get("data").toString();

      return objectMapper.readValue(imageListsJson, ImageLists.class);
    } catch (IOException e) {
      log.error("Error occurred while parsing JSON response", e);
      throw new RuntimeException("Failed to parse ImageLists from response", e);
    } catch (Exception e) {
      log.error("Unexpected error occurred", e);
      throw new RuntimeException("An unexpected error occurred", e);
    }
  }

  private Mono<ApiResponse> uploadImagesToWordPress(ImageLists imageLists) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpMethod method = InternalUrls.UPLOAD_IMAGES.getMethod();
      String apiUrl = LOCALHOST + InternalUrls.UPLOAD_IMAGES.getUrl();

      // Call external API and handle the synchronous ResponseEntity<String> response
      ResponseEntity<String> responseEntity = externalApiUtils.callAPI(
          new ExternalApiRequest(method, headers, apiUrl, imageLists)
      );

      // Map the ResponseEntity to Mono<ApiResponse>
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());

      String body = responseEntity.getBody();
      if (body == null)
        return Mono.error(new RuntimeException("Empty response body from API call"));

      JsonNode rootNode = objectMapper.readTree(body);
      ApiResponse apiResponse = objectMapper.treeToValue(rootNode, ApiResponse.class);
      return Mono.just(apiResponse);
    } catch (Exception e) {
      log.error("Error occurred during uploadImagesToWordPress execution", e);
      return Mono.error(new RuntimeException("Unexpected error occurred in uploadImagesToWordPress"));
    }
  }

  private void updateImageInfoFromDb(Mono<ApiResponse> uploadImagesResponse) {
    try {
      // 1. Mono<ApiResponse>에서 ApiResponse를 동기적으로 추출
      ApiResponse apiResponse = uploadImagesResponse.block();

      // 2. ApiResponse에서 데이터 추출 및 변환
      if (apiResponse == null || apiResponse.data() == null)
        throw new RuntimeException("No data found in API response");

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());

      // JSON 데이터 파싱 및 List<ProcessedDataList>로 변환
      JsonNode dataNode = objectMapper.readTree(apiResponse.data().toString());
      List<ProcessedDataList> processedDataLists = objectMapper.treeToValue(dataNode, new TypeReference<List<ProcessedDataList>>() {});

      // 3. 처리된 데이터를 업데이트 요청
      HttpHeaders headers = new HttpHeaders();
      HttpMethod method = InternalUrls.UPDATE_IMAGE_INFO.getMethod();
      String apiUrl = LOCALHOST + InternalUrls.UPDATE_IMAGE_INFO.getUrl();

      for (ProcessedDataList processedData : processedDataLists) {
        UploadImageInfoRequest request = new UploadImageInfoRequest(processedData.id(), processedData.name());
        ApiResponse response = callApi(headers, method, apiUrl, request);

        if (response.isSuccess()) {
          log.info("Successfully updated image info: {}", processedData);
        } else {
          log.error("Failed to update image info: {}", processedData);
        }
      }
    } catch (Exception e) {
      log.error("Error processing uploaded images", e);
      throw new RuntimeException("Failed to process uploaded images", e);
    }
  }

  private ApiResponse callApi(HttpHeaders headers, HttpMethod method, String apiUrl, Object body) {
    ExternalApiRequest request = new ExternalApiRequest(method, headers, apiUrl, body);
    ResponseEntity<String> response = externalApiUtils.callAPI(request);
    return new ApiResponse(response.getStatusCode().value(), response.getBody(), new Date(), response.getStatusCode().is2xxSuccessful());
  }

  private String limitLogLength(String logMessage) {
    if (logMessage.length() > 300) {
      return logMessage.substring(0, 300) + "...";
    }
    return logMessage;
  }
}
