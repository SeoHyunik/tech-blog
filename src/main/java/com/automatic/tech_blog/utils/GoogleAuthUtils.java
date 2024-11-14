package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.dto.service.OAuthCredentials;
import com.automatic.tech_blog.enums.SecuritySpecs;
import com.fasterxml.jackson.databind.JsonNode;
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
public class GoogleAuthUtils {

  public GoogleCredentials getGoogleCredentials(GoogleAuthInfo authInfo) {
    try {
      // 1. Decrypt credentials to obtain OAuthCredentials
      OAuthCredentials oauthCredentials = SecurityUtils.decryptCredentials(SecuritySpecs.CREDENTIAL_FILE_PATH.getValue());

      // 2. Compare client ID from authInfo and decrypted OAuthCredentials
      if (!oauthCredentials.web().client_id().equals(authInfo.clientId())) {
        throw new IllegalArgumentException("Provided client ID does not match the credentials.");
      }

      // 3. Set up GoogleClientSecrets with decrypted data
      GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
      GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
          .setClientId(oauthCredentials.web().client_id())
          .setClientSecret(oauthCredentials.web().client_secret())
          .setAuthUri(oauthCredentials.web().auth_uri())
          .setTokenUri(oauthCredentials.web().token_uri())
          .setRedirectUris(oauthCredentials.web().redirect_uris());
      clientSecrets.setInstalled(details);

      // 4. Set up authorization flow
      GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
          GoogleNetHttpTransport.newTrustedTransport(),
          GsonFactory.getDefaultInstance(),
          clientSecrets,
          Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY))
          .setDataStoreFactory(new FileDataStoreFactory(new File(SecuritySpecs.TOKENS_DIRECTORY_PATH.getValue())))
          .setAccessType("offline")
          .setApprovalPrompt("force")
          .build();


      // 5. Check for existing credential
      Credential credential = flow.loadCredential("user");
      if (credential == null || credential.getRefreshToken() == null) {
        // If no existing credential or refresh token, delete StoredCredential and proceed with authorization
        File storedCredentialFile = new File(SecuritySpecs.TOKENS_DIRECTORY_PATH.getValue() + SecuritySpecs.TOKEN_FILE_NAME.getValue());
        if (storedCredentialFile.exists()) {
          storedCredentialFile.delete();
          log.info("StoredCredential file deleted");
        }

        // Proceed with new authorization
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // Validate that we received a refresh token
        if (credential.getRefreshToken() == null) {
          throw new IllegalStateException("Refresh token is null. Please ensure offline access is enabled.");
        }
      } else {
        log.info("Existing credential with refresh token found");
      }

      // 6. Convert Credential to GoogleCredentials (supporting automatic refresh)
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

  public AccessToken refreshAccessToken(GoogleCredentials credentials, GoogleAuthInfo authInfo) throws Exception {

    // 1. Check if credentials are an instance of UserCredentials
    String refreshToken;
    if (!(credentials instanceof UserCredentials)) {
      credentials = getGoogleCredentials(new GoogleAuthInfo(authInfo.clientId()));
    }
    refreshToken = ((UserCredentials) credentials).getRefreshToken();

    // 2. Validate that a refresh token is available
    if (refreshToken == null) {
      throw new IllegalStateException("Refresh token is missing. Ensure offline access is enabled and credentials are refreshed.");
    }

    // 3. Decrypt OAuth credentials to obtain OAuth client details
    OAuthCredentials oauthCredentials = SecurityUtils.decryptCredentials(SecuritySpecs.CREDENTIAL_FILE_PATH.getValue());

    // 4. Set up GoogleClientSecrets with decrypted client details
    GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
    GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
        .setClientId(oauthCredentials.web().client_id())
        .setClientSecret(oauthCredentials.web().client_secret())
        .setAuthUri(oauthCredentials.web().auth_uri())
        .setTokenUri(oauthCredentials.web().token_uri())
        .setRedirectUris(oauthCredentials.web().redirect_uris());
    clientSecrets.setInstalled(details);

    // 5. Prepare POST request parameters for the token endpoint
    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
    GenericUrl tokenUrl = new GenericUrl(clientSecrets.getDetails().getTokenUri());

    Map<String, String> params = new HashMap<>();
    params.put("client_id", clientSecrets.getDetails().getClientId());
    params.put("client_secret", clientSecrets.getDetails().getClientSecret());
    params.put("refresh_token", refreshToken);
    params.put("grant_type", "refresh_token");

    // 6. Build and execute the POST request to retrieve a new access token
    HttpRequest request = requestFactory.buildPostRequest(tokenUrl, new UrlEncodedContent(params));
    HttpResponse response = request.execute();

    // 7. Parse the response to extract the new access token
    if (response.getStatusCode() == 200) {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonResponse = objectMapper.readTree(response.getContent());

      // Extract access token from JSON response
      String newAccessToken = jsonResponse.get("access_token").asText();
      return new AccessToken(newAccessToken, null);
    } else {
      throw new IOException("Failed to refresh access token. Status: " + response.getStatusCode());
    }
  }
}