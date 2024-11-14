package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.OAuthCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ExternalApiUtils {

  public GoogleCredentials getGoogleCredentials(GoogleAuthInfo authInfo) {
    try {
      System.out.println("Step 1: getGoogleCredentials init");

      // Step 1: Decrypt credentials to obtain OAuthCredentials
      OAuthCredentials oauthCredentials = SecurityUtils.decryptCredentials(SecuritySpecs.CREDENTIAL_FILE_PATH.getValue());
      System.out.println("Step 2: Decrypted OAuthCredentials obtained");

      // Step 2: Compare client ID from authInfo and decrypted OAuthCredentials
      if (!oauthCredentials.web().client_id().equals(authInfo.clientId())) {
        throw new IllegalArgumentException("Provided client ID does not match the credentials.");
      }
      System.out.println("Step 3: Client ID matched");

      // Step 3: Set up GoogleClientSecrets with decrypted data
      GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
      GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
          .setClientId(oauthCredentials.web().client_id())
          .setClientSecret(oauthCredentials.web().client_secret())
          .setAuthUri(oauthCredentials.web().auth_uri())
          .setTokenUri(oauthCredentials.web().token_uri())
          .setRedirectUris(oauthCredentials.web().redirect_uris());
      clientSecrets.setInstalled(details);
      System.out.println("Step 4: GoogleClientSecrets setup complete");

      // Step 4: Set up authorization flow
      GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
          GoogleNetHttpTransport.newTrustedTransport(),
          GsonFactory.getDefaultInstance(),
          clientSecrets,
          Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY))
          .setDataStoreFactory(new FileDataStoreFactory(new File(SecuritySpecs.TOKENS_DIRECTORY_PATH.getValue())))
          .setAccessType("offline")
          .setApprovalPrompt("force")  // 추가된 부분
          .build();

      System.out.println("Step 5: GoogleAuthorizationCodeFlow setup complete");

      // Step 5: Check for existing credential
      Credential credential = flow.loadCredential("user");
      if (credential == null || credential.getRefreshToken() == null) {
        System.out.println("Step 6: No existing credential or refresh token, proceeding with new authorization");
        // If no existing credential or refresh token, delete StoredCredential and proceed with authorization
        File storedCredentialFile = new File(SecuritySpecs.TOKENS_DIRECTORY_PATH.getValue() + "/StoredCredential");
        if (storedCredentialFile.exists()) {
          storedCredentialFile.delete();
          System.out.println("StoredCredential file deleted");
        }

        // Proceed with new authorization
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        System.out.println("New authorization complete");

        // Validate that we received a refresh token
        if (credential.getRefreshToken() == null) {
          throw new IllegalStateException("Refresh token is null. Please ensure offline access is enabled.");
        }
      } else {
        System.out.println("Existing credential with refresh token found");
      }

      // Step 6: Convert Credential to GoogleCredentials (supporting automatic refresh)
      System.out.println("Step 7: Converting Credential to GoogleCredentials");
      return UserCredentials.newBuilder()
          .setClientId(oauthCredentials.web().client_id())
          .setClientSecret(oauthCredentials.web().client_secret())
          .setRefreshToken(credential.getRefreshToken())
          .setAccessToken(new AccessToken(credential.getAccessToken(), new Date(credential.getExpirationTimeMilliseconds())))
          .build();
    } catch (Exception e) {
      log.error("Error creating Google Credentials: {}", e.getMessage());
      throw new IllegalStateException("Failed to create Google Credentials", e);
    }
  }



  public AccessToken refreshAccessToken(GoogleCredentials credentials) throws Exception {
    // credentials가 UserCredentials로 변환 가능한지 확인 후 refresh token을 사용
    String refreshToken;
    if (credentials instanceof UserCredentials) {
      refreshToken = ((UserCredentials) credentials).getRefreshToken();
    } else {
      // refresh token이 없을 경우 새로운 자격 증명을 생성
      credentials = getGoogleCredentials(new GoogleAuthInfo("YOUR_CLIENT_ID"));
      refreshToken = ((UserCredentials) credentials).getRefreshToken();
    }

    if (refreshToken == null) {
      throw new IllegalStateException("Refresh token is missing. Ensure offline access is enabled and credentials are refreshed.");
    }

    // Step 1: Decrypt credentials to obtain OAuthCredentials
    OAuthCredentials oauthCredentials = SecurityUtils.decryptCredentials(SecuritySpecs.CREDENTIAL_FILE_PATH.getValue());

    // Step 2: Set up GoogleClientSecrets with decrypted data
    GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
    GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
        .setClientId(oauthCredentials.web().client_id())
        .setClientSecret(oauthCredentials.web().client_secret())
        .setAuthUri(oauthCredentials.web().auth_uri())
        .setTokenUri(oauthCredentials.web().token_uri())
        .setRedirectUris(oauthCredentials.web().redirect_uris());
    clientSecrets.setInstalled(details);

    // Step 3: Create a POST request to the token endpoint
    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
    GenericUrl tokenUrl = new GenericUrl(clientSecrets.getDetails().getTokenUri());

    Map<String, String> params = new HashMap<>();
    params.put("client_id", clientSecrets.getDetails().getClientId());
    params.put("client_secret", clientSecrets.getDetails().getClientSecret());
    params.put("refresh_token", refreshToken);
    params.put("grant_type", "refresh_token");

    HttpRequest request = requestFactory.buildPostRequest(tokenUrl, new UrlEncodedContent(params));
    HttpResponse response = request.execute();

    // Step 4: Handle the response and retrieve new access token
    if (response.getStatusCode() == 200) {
      Map<String, Object> responseBody = new ObjectMapper().readValue(response.getContent(), Map.class);
      String newAccessToken = (String) responseBody.get("access_token");
      return new AccessToken(newAccessToken, null);
    } else {
      throw new IOException("Failed to refresh access token. Status: " + response.getStatusCode());
    }
  }
}
