package com.automatic.tech_blog.utils;

import com.automatic.tech_blog.dto.request.GoogleAuthInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalApiUtils {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public OAuth2AccessToken getAccessToken(GoogleAuthInfo authInfo) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            throw new IllegalStateException("No authenticated user found. Ensure the user is logged in.");


        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authInfo.clientId(),
                authentication.getName()
        );

        if (authorizedClient == null)
            throw new IllegalStateException("Authorized client not found. Ensure OAuth2 login has been completed.");


        return authorizedClient.getAccessToken();
    }
}
