package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.utils.ExternalApiUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GoogleServiceImpl implements GoogleService{

  private final ExternalApiUtils externalApi;
  @Override
  public MdFileLists scanFiles(GoogleAuthInfo authInfo) {
    OAuth2AccessToken accessToken = externalApi.getAccessToken(authInfo);

    try {
      HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
      JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
      Drive driveService = new Drive.Builder(transport, jsonFactory, new HttpCredentialsAdapter(credentials)).setApplicationName("Tech Blog").build();

      FileList result = driveService.files().list()
              .setPageSize(10)
              .setFields("files(id, name)")
              .execute();
      List<File> files = result.getFiles();
      List<MdFileInfo> mdFileInfos = null;
      if (files == null || files.isEmpty()) {
          log.info("No files found.");
      } else {
          log.info("Files:");
          for (File file : files) {
          log.info("File Name: {}, File Id: {}", file.getName(), file.getId());
            mdFileInfos.add(new MdFileInfo(file.getName(), file.getId())); // 두번째 인자는 디렉토리명을 가져와야 함
          }
      }
    } catch (Exception e) {
      log.error("Error occurred while scanning files from Google Drive: {}", e.getMessage());
      throw new IllegalStateException("Error occurred while scanning files from Google Drive");
    }
    return null;
  }
}
