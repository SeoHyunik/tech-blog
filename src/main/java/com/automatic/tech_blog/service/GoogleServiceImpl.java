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
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.util.ArrayList;
import java.util.Collections;
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
    System.out.println("service init : " + authInfo.toString());

    try {
      // Update HttpTransport to match Google API library version
      HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
      JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

      // Adjust GoogleCredentials handling to be compatible with Drive API library version
      GoogleCredentials credentials = externalApi.getGoogleCredentials();
      if (credentials.createScopedRequired()) {
        credentials = credentials.createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive.metadata.readonly"));
      }
      HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

      // Ensure the correct version of Drive API library is being used
      Drive driveService = new Drive.Builder(transport, jsonFactory, requestInitializer)
          .setApplicationName("Tech Blog")
          .build();

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
