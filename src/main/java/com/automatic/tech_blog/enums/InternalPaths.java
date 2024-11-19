package com.automatic.tech_blog.enums;

public enum InternalPaths {

  EDITOR_ROLES("src/main/resources/ai_roles/editor_roles.yml"),
  HTML_SAVE_DIR("src/main/resources/converted-htmls/");
  private final String path;
  InternalPaths(String path) {
    this.path = path;
  }
  public String getPath() {
    return path;
  }
}
