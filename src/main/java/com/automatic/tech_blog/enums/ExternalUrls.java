package com.automatic.tech_blog.enums;

public enum ExternalUrls {
  GOOGLE_AUTHORIZATION_URI("https://accounts.google.com/o/oauth2/auth"),
  GOOGLE_TOKEN_URI("https://oauth2.googleapis.com/token"),
  GOOGLE_USER_INFO_URI("https://www.googleapis.com/oauth2/v3/userinfo"),
  GOOGLE_DRIVE_METADATA_READONLY("https://www.googleapis.com/auth/drive.metadata.readonly");

  private final String url;

  ExternalUrls(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
