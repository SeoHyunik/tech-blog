package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.ApiRequest;
import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.request.OpenAiRequest;
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
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleDriveUtils {
  private final GoogleAuthUtils authUtils;
  private final ExternalApiUtils apiUtils;

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
      credentials = credentials.createScoped(Collections.singletonList(ExternalUrls.GOOGLE_DRIVE_READONLY.getUrl()));
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
  public String getFileContent(Drive driveService, String fileId, GoogleAuthInfo googleAuthInfo) {
    try {
      // Get GoogleCredentials
      GoogleCredentials credentials = authUtils.getGoogleCredentials(googleAuthInfo);

      // Ensure AccessToken is valid
      if (credentials.getAccessToken() == null || credentials.getAccessToken().getTokenValue() == null) {
        credentials.refreshIfExpired(); // Refresh the token if expired
      }

      // Inject AccessToken into request
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


  private void addPermissionToFile(String fileId, String accessToken) throws IOException {
    try {
      String url = "https://www.googleapis.com/drive/v3/files/" + fileId + "/permissions";

      // Set HTTP headers
      HttpHeaders headers = new HttpHeaders();
      headers.add("Authorization", "Bearer " + accessToken);
      headers.add("Content-Type", "application/json");

      // Set request body
      Map<String, String> authType = Map.of(
          "role", "reader", // Authorize as reader
          "type", "anyone"  // Allow anyone to read
      );

      // Convert Map to JSON
      Gson gson = new Gson();
      String requestBody = gson.toJson(authType);

      // Prepare API request
      ApiRequest apiRequest = new ApiRequest(
          HttpMethod.POST,
          headers,
          url,
          requestBody
      );

      // Call API
      ResponseEntity<String> response = apiUtils.callAPI(apiRequest);

      // Log and check the response
      if (response == null) {
        throw new IllegalStateException("Failed to add permission to file. Response is null.");
      }

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new IllegalStateException("Failed to add permission to file. Response: " + response.getBody());
      }

      System.out.println("Permission added successfully: " + response.getBody());

    } catch (HttpClientErrorException | HttpServerErrorException e) {
      // Log the API error response
      System.err.println("Google Drive API 호출 중 HTTP 오류 발생: " + e.getResponseBodyAsString());
      throw new IllegalStateException("Google Drive API HTTP 오류", e);

    } catch (Exception e) {
      // Log unexpected exceptions
      System.err.println("예상치 못한 오류 발생: " + e.getMessage());
      throw new IllegalStateException("Failed to add permission to file", e);
    }
  }
}
