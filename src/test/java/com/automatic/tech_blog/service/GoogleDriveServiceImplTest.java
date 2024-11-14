package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.utils.GoogleAuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class GoogleDriveServiceImplTest {

  @Mock
  private GoogleAuthUtils externalApi;

  @InjectMocks
  @Autowired
  private GoogleDriveServiceImpl googleService;

  private GoogleAuthInfo authInfo;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    authInfo = new GoogleAuthInfo("1071785103707-pasovui05jidkkls9lp1g7j390f6pr0l.apps.googleusercontent.com"); // 실제 GoogleAuthInfo 값 입력
  }

  @Test
  void testScanAndUploadFiles() {
    // Arrange
    when(externalApi.getGoogleCredentials(authInfo)).thenCallRealMethod();

    // Act - Scan files from Google Drive
    MdFileLists mdFileLists = googleService.scanFiles(authInfo);

    // Assert - Check if the file list is not null and contains files
    assertNotNull(mdFileLists, "File list should not be null");
    mdFileLists.mdFileLists().forEach(file -> {
      System.out.println("File Name: " + file.fileName() + ", File Id: " + file.id() + ", Directory: " + file.directory()+ ", Created At: "+ file.createdAt()+ ", Modified At: "+ file.modifiedAt());
    });

    // Act - Upload scanned files to the database
    String result = googleService.uploadFiles(mdFileLists);

    // Assert - Check if the upload was successful
    assertNotNull(result, "Upload result should not be null");
    System.out.println("Upload Result: " + result);
  }
}
