package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GoogleServiceIntegrationTest {

  @Autowired
  private GoogleService googleService;

  @Test
  void testGoogleScanFilesIntegration() {
    // Given: Google 인증 정보를 생성합니다.
    GoogleAuthInfo authInfo = new GoogleAuthInfo(
        "1071785103707-ajckhp2k9d1am8uk43j1p64g3blita6l.apps.googleusercontent.com",
        "xx"
    );

    // When: 실제 Google Drive API와 통신합니다.
    MdFileLists fileLists = googleService.scanFiles(authInfo);

    // Then: 파일 리스트가 정상적으로 반환되었는지 확인합니다.
    assertThat(fileLists).isNotNull();

    // 응답 본문 출력
    fileLists.mdFileLists().forEach(file ->
        System.out.printf("File Name: %s, File Id: %s%n", file.fileName(), file.directory())
    );
  }
}