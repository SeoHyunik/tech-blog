package com.automatic.tech_blog.dto.service;

import java.util.List;

public record OAuthCredentials(Web web) {
  public record Web(
      String client_id,
      String project_id,
      String auth_uri,
      String token_uri,
      String auth_provider_x509_cert_url,
      String client_secret,
      List<String> redirect_uris
  ) {}
}