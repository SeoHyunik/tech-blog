package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.service.FileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.utils.GoogleDriveUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class WordPressServiceImplTest {

  @Autowired private GoogleDriveServiceImpl googleService; // Google Drive에서 파일을 가져오는 서비스

  @Autowired private WordPressServiceImpl wordPressService; // WordPress 업로드 서비스

  @InjectMocks private GoogleDriveUtils googleDriveUtils; // Google Drive와 상호작용하는 유틸리티

  @Test
  void testPostArticlesToBlogWithGoogleDriveData() {
    // Step 1: Get new files from Google Drive
    FileLists newFiles = googleService.getNewFiles();

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
}