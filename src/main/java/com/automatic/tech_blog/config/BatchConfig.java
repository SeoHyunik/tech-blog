package com.automatic.tech_blog.config;

import com.automatic.tech_blog.dto.request.EditTechNotesRequest;
import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.FileInfo;
import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.automatic.tech_blog.service.GoogleDriveService;
import com.automatic.tech_blog.service.MdFileAndImageService;
import com.automatic.tech_blog.service.OpenAiService;
import com.automatic.tech_blog.service.WordPressService;
import com.automatic.tech_blog.utils.SecurityUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
  private final OpenAiService openAiService;
  private final MdFileAndImageService mdFileAndImageService;

  @Bean
  public Job techBlogJob() {
    return new JobBuilder("techBlogJob", jobRepository)
        .start(step1_scanFilesAndInsertIntoDb())
        .next(step2_uploadImagesToWordPressLibrary())
        .next(step3_editTechNotesAndUploadToWordPress())
        .build();
  }

  @Bean
  public Step step1_scanFilesAndInsertIntoDb() {
    return new StepBuilder("step1_scanFilesAndInsertIntoDb", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              try {
                // 1. Decrypt Google Auth Info
                GoogleAuthInfo authInfo =
                    new GoogleAuthInfo(
                        SecurityUtils.decryptAuthFile(
                            SecuritySpecs.GOOGLE_AUTH_FILE_PATH.getValue()));
                log.info("Decrypted Google Auth Info");

                // 2. Scan Google Drive Files
                FileLists fileLists = googleDriveService.scanFiles(authInfo);
                log.info("Scanned Files: {}", fileLists);

                // 3. Insert MD files info into DB
                List<ProcessedDataList> mdFileResults =
                    mdFileAndImageService.uploadFilesInfo(fileLists);
                log.info("Inserted MD files into DB: {}", mdFileResults);

                // 4. Insert Pasted Images info into DB
                List<ProcessedDataList> imageResults =
                    mdFileAndImageService.uploadImagesInfo(authInfo, fileLists);
                log.info("Inserted Pasted Images into DB: {}", imageResults);

              } catch (Exception e) {
                log.error("Error occurred in step1_scanFilesAndInsertIntoDb", e);
                throw new RuntimeException("Failed to complete step1_scanFilesAndInsertIntoDb", e);
              }

              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }

  @Bean
  public Step step2_uploadImagesToWordPressLibrary() {
    return new StepBuilder("step2_uploadImagesToWordPressLibrary", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              try {
                // 1. Get newly uploaded images from DB
                ImageLists imageLists = mdFileAndImageService.getNewImagesInfo();
                log.info("Retrieved imageLists from getNewImagesInfo: {}", imageLists);

                if (!imageLists.imageLists().isEmpty()) {
                  // 2. Upload images to WordPress
                  Flux<ProcessedDataList> uploadImagesResponse =
                      wordpressService.uploadImages(imageLists);

                  // 3. Update images info in DB
                  Mono<List<ProcessedDataList>> processedDataListMono =
                      uploadImagesResponse.collectList();
                  processedDataListMono
                      .blockOptional()
                      .ifPresent(
                          processedDataLists ->
                              processedDataLists.forEach(
                                  data -> {
                                    mdFileAndImageService.updateImageInfo(data.id(), data.name());
                                    log.info(
                                        "Updated image info in DB for image ID: {}", data.id());
                                  }));
                } else {
                  log.info("No images to be uploaded to WordPress.");
                }
              } catch (Exception e) {
                log.error("Error occurred during image upload to WordPress", e);
                throw new RuntimeException("Failed to upload images to WordPress", e);
              }
              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }

  @Bean
  public Step step3_editTechNotesAndUploadToWordPress() {
    return new StepBuilder("step3_editTechNotesAndUploadToWordPress", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              try {
                // 1. Get new files from Google Drive
                FileLists newFiles = mdFileAndImageService.getNewFilesInfo();
                log.info("Retrieved newFiles from getNewFilesInfo: {}", newFiles);

                if (!newFiles.fileLists().isEmpty()) {
                  // 2. Decrypt Google Auth Info
                  GoogleAuthInfo authInfo =
                      new GoogleAuthInfo(
                          SecurityUtils.decryptAuthFile(
                              SecuritySpecs.GOOGLE_AUTH_FILE_PATH.getValue()));
                  log.info("Decrypted Google Auth Info");

                  // 3. Edit tech notes
                  EditTechNotesRequest editTechNotesRequest =
                      new EditTechNotesRequest(authInfo, newFiles);
                  List<ProcessedDataList> processedDataLists =
                      Optional.ofNullable(
                              openAiService
                                  .editTechNotes(editTechNotesRequest)
                                  .collectList()
                                  .block())
                          .orElse(Collections.emptyList());

                  if (processedDataLists.isEmpty()) {
                    log.warn("No processed tech notes returned from OpenAI service.");
                    return RepeatStatus.FINISHED;
                  }

                  // 4. Compare id of FileLists and processedDataLists
                  Set<String> processedIds =
                      processedDataLists.stream()
                          .map(ProcessedDataList::id) // Get the ID of each ProcessedDataList
                          .collect(Collectors.toSet());

                  // 5. Filter newFiles to include only files that exist in processedDataLists
                  List<FileInfo> filteredFiles =
                      newFiles.fileLists().stream()
                          .filter(file -> processedIds.contains(file.id()))
                          .collect(Collectors.toList());
                  log.info("Filtered files for upload: {}", filteredFiles);

                  // 6. Post articles to WordPress
                  if (!filteredFiles.isEmpty()) {
                    FileLists filteredFileLists = new FileLists(filteredFiles);

                    Mono<List<ProcessedDataList>> uploadedFileListMono =
                        wordpressService.postArticlesToBlog(filteredFileLists).collectList();

                    uploadedFileListMono
                        .blockOptional()
                        .ifPresent(
                            uploadedFileLists ->
                                uploadedFileLists.forEach(
                                    data ->
                                        log.info(
                                            "Uploaded File -> ID: {}, Name: {}",
                                            data.id(),
                                            data.name())));
                  } else {
                    log.info("No files to be uploaded to WordPress.");
                    return RepeatStatus.FINISHED;
                  }
                }
              } catch (Exception e) {
                log.error("Error occurred during article upload to WordPress", e);
                throw new RuntimeException("Failed to upload articles to WordPress", e);
              }
              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }
}
