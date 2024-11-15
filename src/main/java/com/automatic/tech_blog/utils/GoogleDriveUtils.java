package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.MdFileInfo;
import com.automatic.tech_blog.enums.ExternalUrls;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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

  /**
  * Create GoogleCredentials for Google Drive service
  */
  public Drive createDriveService(GoogleAuthInfo authInfo, String applicationName) throws Exception {
    // Initialize transport and JSON factory
    HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    // Load credentials and refresh token
    GoogleCredentials credentials = authUtils.getGoogleCredentials(authInfo);
    if (credentials.createScopedRequired()) {
      credentials = credentials.createScoped(Collections.singletonList(ExternalUrls.GOOGLE_DRIVE_METADATA_READONLY.getUrl()));
    }
    credentials.refreshIfExpired();

    // Build and return the Drive service
    return new Drive.Builder(transport, jsonFactory, new HttpCredentialsAdapter(credentials))
        .setApplicationName(applicationName)
        .build();
  }

  /**
   * Finds the folder ID by name in Google Drive.
   */
  public String findFolderIdByName(Drive driveService, String folderName) throws IOException {
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
  public void findMdFilesInDirectory(Drive driveService, String directoryId,
      List<MdFileInfo> mdFileInfos, String parentFolderName) throws IOException {

    FileList result = driveService.files().list()
        .setQ("'" + directoryId + "' in parents and mimeType != 'application/vnd.google-apps.folder'")
        .setFields("files(id, name, createdTime, modifiedTime)")  // Include createdTime and modifiedTime
        .execute();

    List<File> files = result.getFiles();
    if (files != null && !files.isEmpty()) {
      for (File file : files) {
        if (file.getName().endsWith(".md")) {
          String fileName = new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
          Date createdAt = FunctionUtils.convertGoogleDateTimeToDate(file.getCreatedTime());
          Date modifiedAt = FunctionUtils.convertGoogleDateTimeToDate(file.getModifiedTime());

          // Add file details including folder name, createdTime, and modifiedTime
          mdFileInfos.add(new MdFileInfo(fileName, file.getId(), parentFolderName, createdAt, modifiedAt, null));
        }
      }
    }
  }

  /**
   * Retrieves the content of a file from Google Drive.
   */
  public String getFileContent(Drive driveService, String fileId) {
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      // Execute file content retrieval
      driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
      return outputStream.toString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Error retrieving content for file ID: {}", fileId, e);
      return null;
    }
  }

}
