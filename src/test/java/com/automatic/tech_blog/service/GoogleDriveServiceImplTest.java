package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.dto.service.ProcessedDataList;
import com.automatic.tech_blog.utils.GoogleAuthUtils;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class GoogleDriveServiceImplTest {

  @MockBean
  private WebClient webClient;
  @Mock
  private GoogleAuthUtils externalApi;

  @InjectMocks
  @Autowired
  private GoogleDriveServiceImpl googleService;

  @InjectMocks
  @Autowired
  private OpenAiServiceImpl openAiService;

  private GoogleAuthInfo authInfo;

  @BeforeEach
  public void setUp() {
    MDC.put("logUUID", UUID.randomUUID().toString());
    MockitoAnnotations.openMocks(this);
    authInfo = new GoogleAuthInfo("1071785103707-pasovui05jidkkls9lp1g7j390f6pr0l.apps.googleusercontent.com"); // 실제 GoogleAuthInfo 값 입력
  }

  @AfterEach
  void tearDown() {
    // 테스트 후 MDC 초기화
    MDC.clear();
  }

  @Test
  void testScanAndUploadFiles() {
    // Arrange
    when(externalApi.getGoogleCredentials(authInfo)).thenCallRealMethod();

    // Act - Scan files from Google Drive
    MdFileLists mdFileLists = googleService.scanFiles(authInfo);

    // Assert - Check if the file list is not null and contains files
    assertNotNull(mdFileLists, "File list should not be null");
    mdFileLists.mdFileLists().forEach(file -> System.out.println("File Name: " + file.fileName() + ", File Id: " + file.id() + ", Directory: " + file.directory()+ ", Created At: "+ file.createdAt()+ ", Modified At: "+ file.modifiedAt()));

    // Act - Upload scanned files to the database
    List<ProcessedDataList> result = googleService.uploadFiles(mdFileLists);

    // Assert - Check if the upload was successful
    assertNotNull(result, "Upload result should not be null");
    System.out.println("Upload Result: " + result);
  }
  @Test
  void testGetNewFiles() {
    // Act - Retrieve new or modified files
    MdFileLists newFiles = googleService.getNewFiles();

    // Assert - Check if the returned file list is not null and contains expected files
    assertNotNull(newFiles, "New files list should not be null");
    newFiles.mdFileLists().forEach(file -> {
      System.out.println("New File Name: " + file.fileName() +
          ", File Id: " + file.id() +
          ", Directory: " + file.directory() +
          ", Created At: " + file.createdAt() +
          ", Modified At: " + file.modifiedAt());
    });

    // Optional: Additional checks for file properties or specific conditions
    assert (!newFiles.mdFileLists().isEmpty()) : "New files list should not be empty";

    List<ProcessedDataList> processedDataLists = openAiService.editTechNotes(newFiles, authInfo);

    System.out.println("Processed Data Lists: " + processedDataLists);
  }

}
