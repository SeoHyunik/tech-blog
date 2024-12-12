package com.automatic.tech_blog.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;

@Getter
@RequiredArgsConstructor
public enum InternalUrls {

  // Google Drive 관련 엔드포인트
  SCAN_FILES("/blog/api/v1/google/scan-files", HttpMethod.GET),
  UPLOAD_FILES("/blog/api/v1/google/upload-files", HttpMethod.POST),
  GET_NEW_FILES("/blog/api/v1/google/new-files", HttpMethod.GET),
  GET_NEW_IMAGES("/blog/api/v1/google/new-images", HttpMethod.GET),
  UPLOAD_PASTED_IMAGES("/blog/api/v1/google/upload-pasted-images", HttpMethod.POST),

  // OpenAI 관련 엔드포인트
  EDIT_TECH_NOTES("/blog/api/v1/open-ai/edit-tech-notes", HttpMethod.POST),

  // WordPress 관련 엔드포인트
  POST_ARTICLES("/blog/api/v1/wp/post-articles", HttpMethod.POST),
  UPLOAD_IMAGES("/blog/api/v1/wp/upload-images", HttpMethod.POST),
  UPDATE_IMAGE_INFO("/blog/api/v1/wp/update-image-info", HttpMethod.PUT);

  private final String url;
  private final HttpMethod method;
}
