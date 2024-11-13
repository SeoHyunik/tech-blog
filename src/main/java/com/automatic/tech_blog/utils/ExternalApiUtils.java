package com.automatic.tech_blog.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalApiUtils {

  private static final String CREDENTIALS_FILE_PATH = "/etc/credentials.json";
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  public GoogleCredentials getGoogleCredentials() {
    try {
      System.out.println("getGoogleCredentials init");
      InputStream in = this.getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
      if (in == null) {
        throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
      }

      System.out.println("inputStream : " + in);
      GoogleClientSecrets clientSecrets =
          GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(in));

      System.out.println("clientSecrets : " + clientSecrets);

      GoogleAuthorizationCodeFlow flow =
          new GoogleAuthorizationCodeFlow.Builder(
              GoogleNetHttpTransport.newTrustedTransport(),
              GsonFactory.getDefaultInstance(),
              clientSecrets,
              Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY))
              .setDataStoreFactory(
                  new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
              .setAccessType("offline")
              .build();

      System.out.println("flow : " + flow);

      // 기존의 Credential 획득 코드
      LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
      Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

      System.out.println("credential : " + credential);

      // Credential 객체를 GoogleCredentials로 변환 (자동 갱신 지원)
      if (credential.getRefreshToken() == null) {
        throw new IllegalStateException("Refresh token is null. Please ensure you have offline access enabled.");
      }

      return UserCredentials.newBuilder()
          .setClientId(clientSecrets.getDetails().getClientId())
          .setClientSecret(clientSecrets.getDetails().getClientSecret())
          .setRefreshToken(credential.getRefreshToken())
          .setAccessToken(new AccessToken(credential.getAccessToken(), new Date(credential.getExpirationTimeMilliseconds())))
          .build();
    } catch (Exception e) {
      log.error("Error creating Google Credentials: {}", e.getMessage());
      throw new IllegalStateException("Failed to create Google Credentials", e);
    }
  }
}
