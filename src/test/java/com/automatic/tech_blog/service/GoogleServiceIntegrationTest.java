package com.automatic.tech_blog.service;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import com.automatic.tech_blog.utils.GoogleAuthUtils;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GoogleAuthUtilsTest {

  @InjectMocks
  private GoogleAuthUtils googleAuthUtils;

  private static final String TEST_CLIENT_ID = "1071785103707-pasovui05jidkkls9lp1g7j390f6pr0l.apps.googleusercontent.com";

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetGoogleCredentials() {
    GoogleAuthInfo authInfo = new GoogleAuthInfo(TEST_CLIENT_ID);

    try {
      // Act: Call the method to get GoogleCredentials
      GoogleCredentials credentials = googleAuthUtils.getGoogleCredentials(authInfo);

      // Assert: Check if the returned credentials contain the expected client ID
      assertNotNull(credentials, "Credentials should not be null");

      // Assert: Check if credentials contain an access token
      AccessToken accessToken = credentials.getAccessToken();
      assertNotNull(accessToken, "Access token should not be null");
      assertNotNull(accessToken.getTokenValue(), "Access token value should not be null");

      System.out.println("Access Token: " + accessToken.getTokenValue());
    } catch (Exception e) {
      fail("Exception should not be thrown: " + e.getMessage());
    }
  }

  @Test
  void testRefreshAccessToken() {
    GoogleAuthInfo authInfo = new GoogleAuthInfo(TEST_CLIENT_ID);

    try {
      // Arrange: Get initial GoogleCredentials
      GoogleCredentials credentials = googleAuthUtils.getGoogleCredentials(authInfo);

      // Act: Refresh the access token
      AccessToken newAccessToken = googleAuthUtils.refreshAccessToken(credentials, authInfo);

      // Assert: Check if the new access token is obtained
      assertNotNull(newAccessToken, "New access token should not be null");
      assertNotNull(newAccessToken.getTokenValue(), "New access token value should not be null");

      System.out.println("New Access Token: " + newAccessToken.getTokenValue());
    } catch (IOException e) {
      fail("IOException should not be thrown: " + e.getMessage());
    } catch (Exception e) {
      fail("Exception should not be thrown: " + e.getMessage());
    }
  }
}
