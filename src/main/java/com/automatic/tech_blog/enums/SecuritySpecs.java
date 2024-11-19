package com.automatic.tech_blog.enums;

public enum SecuritySpecs {
  ALGORITHM("AES/CBC/PKCS5Padding"),
  SECRET_KEY_ALGORITHM("AES"),
  ENV_DIR_PATH("env"),
  ENV_KEY_NAME("SECRET_KEY_BASE64"),
  CREDENTIAL_FILE_PATH("src/main/resources/encrypted/encrypted_credentials.txt"),
  OPEN_AI_API_KEY_FILE_PATH("src/main/resources/encrypted/openai_api_key.txt"),
  OPEN_AI_SECRET_KEY_FILE_PATH("src/main/resources/encrypted/openai_secret_key.txt"),
  TOKENS_DIRECTORY_PATH("src/main/resources/token/"),
  TOKEN_FILE_NAME("/StoredCredential");

  private final String value;

  SecuritySpecs(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
