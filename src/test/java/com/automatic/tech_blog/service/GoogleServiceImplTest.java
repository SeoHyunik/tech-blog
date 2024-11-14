package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.utils.ExternalApiUtils;
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
class GoogleServiceImplTest {

  @Mock
  private ExternalApiUtils externalApi;

  @InjectMocks
  @Autowired
  private GoogleServiceImpl googleService;

  private GoogleAuthInfo authInfo;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    authInfo = new GoogleAuthInfo("1071785103707-pasovui05jidkkls9lp1g7j390f6pr0l.apps.googleusercontent.com"); // 실제 GoogleAuthInfo 값 입력
  }

  @Test
  void testScanFiles() {
    // Arrange
    when(externalApi.getGoogleCredentials(authInfo)).thenCallRealMethod();

    // Act
    MdFileLists mdFileLists = googleService.scanFiles(authInfo);

    // Assert
    assertNotNull(mdFileLists, "File list should not be null");
    mdFileLists.mdFileLists().forEach(file -> {
      System.out.println("File Name: " + file.fileName() + ", File Id: " + file.id() + ", Directory: " + file.directory());
    });
  }
}
