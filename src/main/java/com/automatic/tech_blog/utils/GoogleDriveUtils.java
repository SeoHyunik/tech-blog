package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.FileInfo;
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

  public Drive createDriveService(GoogleAuthInfo authInfo, String applicationName)
      throws Exception {
    // 1. Initialize transport and JSON factory
    HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    // 2. Load credentials and refresh token
    GoogleCredentials credentials = authUtils.getGoogleCredentials(authInfo);
    if (credentials.createScopedRequired())
      credentials =
          credentials.createScoped(
              Collections.singletonList(ExternalUrls.GOOGLE_DRIVE_AUTH_READONLY.getUrl()));
    credentials.refreshIfExpired();

    // 3. Build and return the Drive service
    return new Drive.Builder(transport, jsonFactory, new HttpCredentialsAdapter(credentials))
        .setApplicationName(applicationName)
        .build();
  }

  public String findFolderIdByName(Drive driveService, String folderName) throws IOException {
    // 1. Search for folders with the given name
    FileList result =
        driveService
            .files()
            .list()
            .setQ("mimeType = 'application/vnd.google-apps.folder' and name = '" + folderName + "'")
            .setFields("files(id, name)")
            .execute();

    // 2. Return the ID of the first matched folder
    return (result.getFiles() != null && !result.getFiles().isEmpty())
        ? result.getFiles().get(0).getId()
        : null;
  }

  public String findFilePathById(Drive driveService, String fileId) throws IOException {
    // 1. Initialize a StringBuilder to construct the file path
    StringBuilder filePath = new StringBuilder();

    // 2. Start with the current file's metadata
    String currentFileId = fileId;

    while (currentFileId != null) {
      // 3. Fetch the file metadata, including its name and parent ID(s)
      com.google.api.services.drive.model.File file =
          driveService.files().get(currentFileId).setFields("id, name, parents").execute();

      // 4. Prepend the current file/folder name to the path
      filePath.insert(0, "/" + file.getName());

      // 5. Get the parent ID to continue traversing up the hierarchy
      List<String> parents = file.getParents();

      // 6. If no parents exist, we've reached the root
      currentFileId = (parents != null && !parents.isEmpty()) ? parents.get(0) : null;
    }

    return filePath.toString();
  }

  public void findMdFilesInDirectory(
      Drive driveService, String directoryId, List<FileInfo> fileInfos, String parentFolderName)
      throws IOException {
    // 1. Search for all files in the directory
    FileList result =
        driveService
            .files()
            .list()
            .setQ("'" + directoryId + "' in parents")
            .setFields(
                "files(id, name, mimeType, createdTime, modifiedTime)") // Include all file metadata
            .execute();

    List<File> files = result.getFiles();
    if (files != null && !files.isEmpty()) {
      for (File file : files) {
        if ("application/vnd.google-apps.folder".equals(file.getMimeType())) {
          // 2. If the file is a folder, recursively scan it
          findMdFilesInDirectory(driveService, file.getId(), fileInfos, file.getName());
        } else if (file.getName().endsWith(".md") || file.getName().contains("Pasted image")) {
          // 3. If the file is an .md file, add it to the list
          String fileName =
              new String(file.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
          Date createdAt = FunctionUtils.convertGoogleDateTimeToDate(file.getCreatedTime());
          Date modifiedAt = FunctionUtils.convertGoogleDateTimeToDate(file.getModifiedTime());

          fileInfos.add(
              new FileInfo(fileName, file.getId(), parentFolderName, createdAt, modifiedAt, null));
        }
      }
    }
  }

  public String getFileContent(Drive driveService, String fileId, GoogleAuthInfo googleAuthInfo) {
    try {
      // 1. Get GoogleCredentials
      GoogleCredentials credentials = authUtils.getGoogleCredentials(googleAuthInfo);

      // 2. Ensure AccessToken is valid
      if (credentials.getAccessToken() == null
          || credentials.getAccessToken().getTokenValue() == null)
        credentials.refreshIfExpired(); // Refresh the token if expired

      // 3. Inject AccessToken into request
      HttpRequestFactory requestFactory = driveService.getRequestFactory();
      HttpRequest request =
          requestFactory.buildGetRequest(
              new GenericUrl(
                  ExternalUrls.GOOGLE_DRIVE_FILE_METADATA.getUrl().replace("{FILE_ID}", fileId)));
      request
          .getHeaders()
          .setAuthorization("Bearer " + credentials.getAccessToken().getTokenValue());

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      request.execute().download(outputStream);

      return outputStream.toString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("Error retrieving content for file ID: {}", fileId, e);
      return null;
    }
  }

  public boolean isMdFile(String fileName) {
    return fileName.endsWith(".md")
        && !(fileName.contains("Pasted image") || fileName.contains("Exported image"));
  }
}
