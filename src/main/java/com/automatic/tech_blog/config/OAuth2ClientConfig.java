package com.automatic.tech_blog.config;

import com.automatic.tech_blog.enums.ExternalUrls;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@Configuration
public class OAuth2ClientConfig {

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository() {
    return new InMemoryClientRegistrationRepository(googleClientRegistration());
  }

  private ClientRegistration googleClientRegistration() {
    return ClientRegistration.withRegistrationId("google")
        .clientId("${spring.security.oauth2.client.registration.google.client-id}")
        .clientSecret("${spring.security.oauth2.client.registration.google.client-secret}")
        .scope("openid", "profile", "email", ExternalUrls.GOOGLE_DRIVE_AUTH_READONLY.getUrl())
        .authorizationUri(ExternalUrls.GOOGLE_AUTHORIZATION_URI.getUrl())
        .tokenUri(ExternalUrls.GOOGLE_TOKEN_URI.getUrl())
        .userInfoUri(ExternalUrls.GOOGLE_USER_INFO_URI.getUrl())
        .redirectUri("http://localhost:9000/oauth2/google")
        .clientName("Google")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .build();
  }

  @Bean
  public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
    return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
  }
}
