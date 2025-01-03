package com.automatic.tech_blog.enums;

public enum ExternalUrls {
  GOOGLE_AUTHORIZATION_URI("https://accounts.google.com/o/oauth2/auth"),
  GOOGLE_TOKEN_URI("https://oauth2.googleapis.com/token"),
  GOOGLE_USER_INFO_URI("https://www.googleapis.com/oauth2/v3/userinfo"),
  GOOGLE_DRIVE_AUTH_READONLY("https://www.googleapis.com/auth/drive"),
  GOOGLE_DRIVE_FILE_METADATA("https://www.googleapis.com/drive/v3/files/{FILE_ID}?alt=media"),
  OPEN_AI_COMPLETION_URI("https://api.openai.com/v1/chat/completions"),
  WORD_PRESS_KIWIJAM_V1("https://kiwijam.kr/wp-json/api/v1"),
  WORD_PRESS_KIWIJAM_V2("https://kiwijam.kr/wp-json/wp/v2"),
  EXCHANGE_RATE_URI("https://v6.exchangerate-api.com/v6/{API_KEY}/latest/USD");

  private final String url;

  ExternalUrls(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
