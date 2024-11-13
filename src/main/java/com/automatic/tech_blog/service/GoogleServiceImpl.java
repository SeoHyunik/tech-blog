package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.dto.service.MdFileLists;
import com.automatic.tech_blog.utils.ExternalApiUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GoogleServiceImpl implements GoogleService {

  private final ExternalApiUtils externalApi;

  @Override
  public MdFileLists scanFiles(GoogleAuthInfo authInfo) {
    System.out.println("Service init: " + authInfo.toString());

    try {
      HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
      JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

      // 1. Load Google Credentials and obtain fresh access token
      GoogleCredentials credentials = externalApi.getGoogleCredentials();
      if (credentials.createScopedRequired()) {
        credentials = credentials.createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive.metadata.readonly"));
      }
      credentials.refreshIfExpired();

      AccessToken accessToken = credentials.getAccessToken();
      if (accessToken == null || accessToken.getExpirationTime().before(new Date())) {
        // 2. Manually refresh token if expired
        accessToken = externalApi.refreshAccessToken(credentials);
      }

      HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

      // 3. Build the Drive service
      Drive driveService = new Drive.Builder(transport, jsonFactory, requestInitializer)
          .setApplicationName("Tech Blog")
          .build();

      // 4. List files in Google Drive
      FileList result = driveService.files().list()
          .setPageSize(10)
          .setFields("files(id, name)")
          .execute();
      List<File> files = result.getFiles();
      List<MdFileInfo> mdFileInfos = new ArrayList<>();

      if (files == null || files.isEmpty()) {
        log.info("No files found.");
      } else {
        for (File file : files) {
          log.info("File Name: {}, File Id: {}", file.getName(), file.getId());
          mdFileInfos.add(new MdFileInfo(file.getName(), file.getId()));
        }
      }
      return new MdFileLists(mdFileInfos);
    } catch (Exception e) {
      log.error("Error occurred while scanning files from Google Drive: {}", e.getMessage(), e);
      throw new IllegalStateException("Error occurred while scanning files from Google Drive", e);
    }
  }
}
