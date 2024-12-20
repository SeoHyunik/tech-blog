package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ImageLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class WordPressServiceImplTest {
  @Autowired private WordPressServiceImpl wordPressService; // WordPress 업로드 서비스
  @Autowired private MdFileAndImageServiceImpl mdFileAndImageService; // Md 파일과 이미지 서비스

  @Test
  void testPostArticlesToBlogWithGoogleDriveData() {
    // Step 1: Get new files from Google Drive
    FileLists newFiles = mdFileAndImageService.getNewFilesInfo();

    assertNotNull(newFiles, "New files list should not be null");
    assertFalse(newFiles.fileLists().isEmpty(), "New files list should not be empty");

    newFiles
        .fileLists()
        .forEach(
            file -> {
              System.out.println("File Name: " + file.fileName());
            });

    // Step 2: Call WordPressServiceImpl to post articles
    Flux<ProcessedDataList> processedDataFlux = wordPressService.postArticlesToBlog(newFiles);

    // Step 3: Verify and debug using Mono
    Mono<List<ProcessedDataList>> processedDataListMono = processedDataFlux.collectList();

    processedDataListMono.blockOptional().ifPresent(processedDataLists -> {
      assertNotNull(processedDataLists, "Processed data list should not be null");
      assertFalse(processedDataLists.isEmpty(), "Processed data list should not be empty");

      processedDataLists.forEach(data -> {
        assertNotNull(data.id(), "Processed data ID should not be null");
        assertNotNull(data.name(), "Processed data name should not be null");
        System.out.println("Processed Data -> ID: " + data.id() + ", Name: " + data.name());
      });
    });
  }

  @Test
  void testUploadImagesToWordPress() {
    // Step 1: Prepare test image data
    ImageLists imageLists = mdFileAndImageService.getNewImagesInfo();

    // Log input data for debugging
    imageLists.imageLists().forEach(imageInfo -> {
      System.out.println("Image ID: " + imageInfo.id() + ", Name: " + imageInfo.imageName());
    });

    // Step 2: Call the uploadImages method
    Flux<ProcessedDataList> processedDataFlux = wordPressService.uploadImages(imageLists);

    // Step 3: Verify and debug using Mono
    Mono<List<ProcessedDataList>> processedDataListMono = processedDataFlux.collectList();

    processedDataListMono.blockOptional().ifPresent(processedDataLists -> {
      // Verify processed data list is not null or empty
      assertNotNull(processedDataLists, "Processed data list should not be null");
      assertFalse(processedDataLists.isEmpty(), "Processed data list should not be empty");

      // Log processed data for debugging
      processedDataLists.forEach(data -> {
        mdFileAndImageService.updateImageInfo(data.id(), data.name());
        assertNotNull(data.id(), "Processed data ID should not be null");
        assertNotNull(data.name(), "Processed data url should not be null");
        System.out.println("Processed Data -> ID: " + data.id() + ", URL: " + data.name());
      });
    });
  }
}