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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
      GoogleCredentials credentials = externalApi.getGoogleCredentials(authInfo);
      if (credentials.createScopedRequired()) {
        credentials = credentials.createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive.metadata.readonly"));
      }
      credentials.refreshIfExpired();

      AccessToken accessToken = credentials.getAccessToken();
      if (accessToken == null || accessToken.getExpirationTime().before(new Date())) {
        accessToken = externalApi.refreshAccessToken(credentials);
      }

      HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

      // 2. Build the Drive service
      Drive driveService = new Drive.Builder(transport, jsonFactory, requestInitializer)
          .setApplicationName("Tech Blog")
          .build();

      // 3. Find specific directories and scan for .md files in them
      List<String> targetFolders = Arrays.asList("Algorithm", "IT Knowledge", "JAVA-SPRING", "Notes");
      List<MdFileInfo> mdFileInfos = new ArrayList<>();

      for (String folderName : targetFolders) {
        // Find the target directory by name
        String folderId = findFolderIdByName(driveService, folderName);
        if (folderId != null) {
          // If folder is found, scan for .md files within it
          findMdFilesInDirectory(driveService, folderId, mdFileInfos, folderName);
        } else {
          System.out.println("Folder " + folderName + " not found.");
        }
      }

      return new MdFileLists(mdFileInfos);
    } catch (Exception e) {
      log.error("Error occurred while scanning files from Google Drive: {}", e.getMessage(), e);
      throw new IllegalStateException("Error occurred while scanning files from Google Drive", e);
    }
  }

  /**
   * Finds the folder ID by name in Google Drive.
   */
  private String findFolderIdByName(Drive driveService, String folderName) throws IOException {
    FileList result = driveService.files().list()
        .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "'")
        .setFields("files(id, name)")
        .execute();

    List<File> files = result.getFiles();
    if (files != null && !files.isEmpty()) {
      return files.get(0).getId();  // Return the ID of the first matched folder
    }
    return null;
  }

  /**
   * Scans for .md files within a given directory ID and adds them to the result list.
   */
  private void findMdFilesInDirectory(Drive driveService, String directoryId, List<MdFileInfo> mdFileInfos, String parentFolderName) throws IOException {
    FileList result = driveService.files().list()
        .setQ("'" + directoryId + "' in parents and mimeType != 'application/vnd.google-apps.folder'")
        .setFields("files(id, name)")
        .execute();

    List<File> files = result.getFiles();
    if (files != null && !files.isEmpty()) {
      for (File file : files) {
        if (file.getName().endsWith(".md")) {
          String fileName = file.getName(); // 별도 인코딩 변환 없이 그대로 사용
          mdFileInfos.add(new MdFileInfo(fileName, file.getId(), parentFolderName));  // Add folder name
        }
      }
    }
  }

}
