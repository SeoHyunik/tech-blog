package com.automatic.tech_blog.config;

import com.automatic.tech_blog.dto.request.ExternalApiRequest;
import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.request.UploadImageInfoRequest;
import com.automatic.tech_blog.dto.response.ApiResponse;
import com.automatic.tech_blog.dto.service.FileInfo;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.enums.InternalUrls;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.automatic.tech_blog.service.GoogleDriveService;
import com.automatic.tech_blog.service.WordPressService;
import com.automatic.tech_blog.utils.ExternalApiUtils;
import com.automatic.tech_blog.utils.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
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
  private final GoogleDriveService googleDriveService;
  private final WordPressService wordpressService;


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
              // 1. 인증 정보 복호화
              String authInfo = SecurityUtils.decryptAuthFile(SecuritySpecs.GOOGLE_AUTH_FILE_PATH.getValue());
              log.info("Decrypted Google Auth Info");

              // 2. 파일 스캔
              FileLists fileLists = googleDriveService.scanFiles(new GoogleAuthInfo(authInfo));
              log.info("Scanned Files: {}", fileLists);

              // 3. MD 파일 DB 삽입
              List<ProcessedDataList> mdFileResults = googleDriveService.uploadFiles(fileLists);
              log.info("Inserted MD files into DB: {}", mdFileResults);

              // 4. 붙여넣기 이미지 DB 삽입
              List<ProcessedDataList> imageResults = googleDriveService.uploadPastedImages(new GoogleAuthInfo(authInfo), fileLists);
              log.info("Inserted Pasted Images into DB: {}", imageResults);

              return RepeatStatus.FINISHED;
            },
            transactionManager
        )
        .build();
  }



  @Bean
  public Step step2_uploadImagesToWordPressLibrary() {
    return new StepBuilder("step2_uploadImagesToWordPressLibrary", jobRepository)
        .tasklet((contribution, chunkContext) -> {
          try {
            // 1. 새 이미지를 가져옴
            ImageLists imageLists = googleDriveService.getNewImages();
            log.info("Retrieved imageLists from getNewImages: {}", imageLists);

            // 2. 워드프레스에 이미지 업로드
            if (!imageLists.imageLists().isEmpty()) {
              Flux<ProcessedDataList> uploadImagesResponse = wordpressService.uploadImages(imageLists);

              // 3. 업로드된 이미지 정보를 DB에 업데이트
              Mono<List<ProcessedDataList>> processedDataListMono = uploadImagesResponse.collectList();
              processedDataListMono.blockOptional().ifPresent(processedDataLists -> {
                processedDataLists.forEach(data -> {
                  wordpressService.updateImageInfo(data.id(), data.name());
                  log.info("Updated image info in DB for image ID: {}", data.id());
                });
              });
            } else {
              log.info("No new images to upload to WordPress.");
            }
          } catch (Exception e) {
            log.error("Error occurred during image upload to WordPress", e);
            throw new RuntimeException("Failed to upload images to WordPress", e);
          }
          return RepeatStatus.FINISHED;
        }, transactionManager)
        .build();
  }
}
