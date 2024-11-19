package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveUtils {
  private final GoogleAuthUtils authUtils;

  public Drive createDriveService(GoogleAuthInfo authInfo, String applicationName) throws Exception {
    // 1. Initialize transport and JSON factory
    HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    // 2. Load credentials and refresh token
    GoogleCredentials credentials = authUtils.getGoogleCredentials(authInfo);
    if (credentials.createScopedRequired())
      credentials = credentials.createScoped(Collections.singletonList(ExternalUrls.GOOGLE_DRIVE_READONLY.getUrl()));
    credentials.refreshIfExpired();

    // 3. Build and return the Drive service
    return new Drive.Builder(transport, jsonFactory, new HttpCredentialsAdapter(credentials))
        .setApplicationName(applicationName)
        .build();
  }

  public String findFolderIdByName(Drive driveService, String folderName) throws IOException {
    // 1. Search for folders with the given name
    FileList result = driveService.files().list()
        .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "'")
        .setFields("files(id, name)")
        .execute();

    // 2. Return the ID of the first matched folder
    return (result.getFiles() != null && !result.getFiles().isEmpty())
        ? result.getFiles().get(0).getId()
        : null;
  }

  public void findMdFilesInDirectory(Drive driveService, String directoryId,
      List<MdFileInfo> mdFileInfos, String parentFolderName) throws IOException {
    // 1. Search for .md files within the directory
    FileList result = driveService.files().list()
        .setQ("'" + directoryId + "' in parents and mimeType != 'application/vnd.google-apps.folder'")
        .setFields("files(id, name, createdTime, modifiedTime)")  // Include createdTime and modifiedTime
        .execute();

    // 2. Add file details to the result list
    List<File> files = result.getFiles();
    if (files != null && !files.isEmpty()) {
      for (File file : files) {
        if (file.getName().endsWith(".md")) {
          String fileName = new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
          Date createdAt = FunctionUtils.convertGoogleDateTimeToDate(file.getCreatedTime());
          Date modifiedAt = FunctionUtils.convertGoogleDateTimeToDate(file.getModifiedTime());

          // 3. Add file details including folder name, createdTime, and modifiedTime
          mdFileInfos.add(new MdFileInfo(fileName, file.getId(), parentFolderName, createdAt, modifiedAt, null));
        }
      }
    }
  }

  public String getFileContent(Drive driveService, String fileId, GoogleAuthInfo googleAuthInfo) {
    try {
      // 1. Get GoogleCredentials
      GoogleCredentials credentials = authUtils.getGoogleCredentials(googleAuthInfo);

      // 2. Ensure AccessToken is valid
      if (credentials.getAccessToken() == null || credentials.getAccessToken().getTokenValue() == null)
        credentials.refreshIfExpired(); // Refresh the token if expired

      // 3. Inject AccessToken into request
      HttpRequestFactory requestFactory = driveService.getRequestFactory();
      HttpRequest request =
          requestFactory.buildGetRequest(
              new GenericUrl("https://www.googleapis.com/drive/v3/files/" + fileId + "?alt=media"));
      request.getHeaders().setAuthorization("Bearer " + credentials.getAccessToken().getTokenValue());

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      request.execute().download(outputStream);

      return outputStream.toString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Error retrieving content for file ID: {}", fileId, e);
      return null;
    }
  }
}
