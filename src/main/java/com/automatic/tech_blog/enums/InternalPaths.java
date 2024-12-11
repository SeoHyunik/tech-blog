package com.automatic.tech_blog.enums;

public enum InternalPaths {
  EDITOR_ROLES("src/main/resources/ai_roles/editor_roles.json"),
  HTML_SAVE_DIR("src/main/resources/converted-htmls/"),
  OPEN_AI_PRICE_POLICY("src/main/resources/policy/openai_price_policy.json");
  private final String path;
  InternalPaths(String path) {
    this.path = path;
  }
  public String getPath() {
    return path;
  }
}
